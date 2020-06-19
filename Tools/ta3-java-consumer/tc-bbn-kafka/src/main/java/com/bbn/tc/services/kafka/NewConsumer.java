/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.services.kafka;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.avro.generic.GenericContainer;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.record.TimestampType;
import org.apache.log4j.Logger;

import com.bbn.tc.schema.serialization.AvroConfig;
import com.bbn.tc.schema.serialization.AvroGenericDeserializer;
import com.bbn.tc.schema.serialization.AvroGenericSerializer;
import com.bbn.tc.schema.serialization.Utils;
import com.bbn.tc.schema.serialization.kafka.NoOpDeserializer;

/**
 * This consumer uses the new kafka API, with finer granularity
 * on offsets
 *
 * Created by jkhoury
 */
public class NewConsumer extends Thread{
    private final Logger logger = Logger.getLogger(this.getClass().getCanonicalName());
    
    protected final KafkaConsumer<String, GenericContainer> consumer;
    protected int duration = 0;
    protected boolean consumeAll = false;
    protected Utils.Stats latencyStats;
    protected long recordCounter=0;
    protected AtomicBoolean shutdown = new AtomicBoolean(false);
    protected int N = 100;
    protected String securityProtocol;
    protected String truststoreLocation;
    protected String truststorePassword;
    protected String keystoreLocation;
    protected String keystorePassword;
    protected String sslKey;

    // for file serialization
    protected final File file;
    protected AvroGenericDeserializer fileDeserializer;
    // for json visualization of the records
    protected AvroGenericSerializer<GenericContainer> jsonSerializer;

    private boolean setSpecificOffset = false;
    private long forcedOffset = -1;
    protected long maxRecords = -1;

    public int recordSize = -1;
    public Distribution computedRecordSize = new Distribution("Received Record Size");
    protected static boolean minimalWork = false;
    protected static boolean busyWork = false;
    public static int busyWorkInt;
    public int reportingPeriod = 10000; // Report every 10 secs
    static protected boolean Instrumented = true;
        
    public NewConsumer(String kafkaServer, String groupId, String topics, int duration, boolean consumeAll, boolean isSpecific,
            String consumerSchemaFilename, String producerSchemaFilename, String autoOffset, String specificOffset,
            boolean noavro, long maxRecords, int recordSize) {
        this(kafkaServer, groupId, topics, duration, consumeAll, isSpecific,
                consumerSchemaFilename, producerSchemaFilename, autoOffset, specificOffset,
                noavro, maxRecords, recordSize, null);
    }

    public NewConsumer(String kafkaServer, String groupId, String topics, int duration, boolean consumeAll, boolean isSpecific,
            String consumerSchemaFilename, String producerSchemaFilename, String autoOffset, String specificOffset,
            boolean noavro, long maxRecords, int recordSize, String securityProtocol, String truststoreLocation, String truststorePassword,
            String keystoreLocation, String keystorePassword, String sslKey) {
        this(kafkaServer, groupId, topics, duration, consumeAll, isSpecific,
                consumerSchemaFilename, producerSchemaFilename, autoOffset, specificOffset,
                noavro, maxRecords, recordSize, null, securityProtocol, truststoreLocation, truststorePassword,
                keystoreLocation, keystorePassword, sslKey);
    }

    public NewConsumer(String kafkaServer, String groupId, String topics, int duration, boolean consumeAll, boolean isSpecific,
            String consumerSchemaFilename, String producerSchemaFilename, String autoOffset, String specificOffset,
            boolean noavro, long maxRecords, int recordSize, File file) {
        this.duration = duration;
        this.consumeAll = consumeAll;
        this.recordSize = recordSize;
        this.maxRecords = maxRecords;
        this.file = file;

        //init the jsonSerializer
        try {
            jsonSerializer = new AvroGenericSerializer<>(consumerSchemaFilename, isSpecific, null);
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e);
        }
        
        if (this.file != null) {
            consumer = null;
            try {
                assert file.exists(); // we already verified this in Main
                fileDeserializer = new AvroGenericDeserializer(consumerSchemaFilename, producerSchemaFilename,
                        isSpecific, file);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e);
                try{
                    if(fileDeserializer != null) fileDeserializer.close();
                    fileDeserializer = null;
                }catch (Exception ee){}
            }
        }else {

            Properties properties = new Properties();
            properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
            //add some other properties
            properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
            properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 20000);
            properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "0"); //ensure no temporal batching
            properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
            properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffset);

            if (specificOffset != null) {
                // Did we set a specific offset?
                try {
                    forcedOffset = Long.parseLong(specificOffset);
                    setSpecificOffset = true;
                } catch (NumberFormatException ex) {
                    logger.error("Unable to understand autoOffset value of " + forcedOffset, ex);
                    setSpecificOffset = false;
                }
            }

            //serialization properties
            properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                    "org.apache.kafka.common.serialization.StringDeserializer");

            if (noavro) {
                properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                        NoOpDeserializer.class);
                logger.info("Using the NoOp Deserializer");
            } else {
                properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                        com.bbn.tc.schema.serialization.kafka.KafkaAvroGenericDeserializer.class);

                logger.info("Using the real Avro Deserializer");
            }

            properties.put(AvroConfig.SCHEMA_READER_FILE, consumerSchemaFilename);
            properties.put(AvroConfig.SCHEMA_WRITER_FILE, producerSchemaFilename);
            properties.put(AvroConfig.SCHEMA_SERDE_IS_SPECIFIC, isSpecific);
            consumer = new InstrumentedKafkaConsumer<>(properties);
            if (Instrumented) {
                InstrumentationManager.turnOnInstrumentationServer();
            }

            if (setSpecificOffset) {
                logger.info("Going to set specific offset for each partition to " + forcedOffset);
                consumer.subscribe(Arrays.asList(topics.split(",")), new ForceOffsetConsumerRebalanceListener());
            } else {
                consumer.subscribe(Arrays.asList(topics.split(",")));
            }

            // Unit tests check for this title pattern (End-to-End Latency), edit the tests if you change this
            latencyStats = new Utils.Stats(1, "End-to-End Latency (including Avro Serialization)", "ms");
        }
    }

    public NewConsumer(String kafkaServer, String groupId, String topics, int duration, boolean consumeAll, boolean isSpecific,
            String consumerSchemaFilename, String producerSchemaFilename, String autoOffset, String specificOffset,
            boolean noavro, long maxRecords, int recordSize, File file, String securityProtocol, String truststoreLocation,
            String truststorePassword, String keystoreLocation, String keystorePassword, String sslKey) {
        this.duration = duration;
        this.consumeAll = consumeAll;
        this.recordSize = recordSize;
        this.maxRecords = maxRecords;
        this.file = file;
        this.securityProtocol = securityProtocol;
        this.truststoreLocation = truststoreLocation;
        this.truststorePassword = truststorePassword;
        this.keystoreLocation = keystoreLocation;
        this.keystorePassword = keystorePassword;
        this.sslKey = sslKey;

        //init the jsonSerializer
        try {
            jsonSerializer = new AvroGenericSerializer<>(consumerSchemaFilename, isSpecific, null);
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e);
        }
        
        if (this.file != null) {
            consumer = null;
            try {
                assert file.exists(); // we already verified this in Main
                fileDeserializer = new AvroGenericDeserializer(consumerSchemaFilename, producerSchemaFilename,
                        isSpecific, file);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e);
                try{
                    if(fileDeserializer != null) fileDeserializer.close();
                    fileDeserializer = null;
                }catch (Exception ee){}
            }
        }else {

            Properties properties = new Properties();
            properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
            //add some other properties
            properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
            properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 20000);
            properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "0"); //ensure no temporal batching
            properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
            properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffset);

            if (securityProtocol != null) {
                // configure the following three settings for SSL Encryption
                properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
                properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststoreLocation);
                properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword);

                // configure the following three settings for SSL Authentication
                properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystoreLocation);
                properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePassword);
                properties.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, sslKey);
            }

            if (specificOffset != null) {
                // Did we set a specific offset?
                try {
                    forcedOffset = Long.parseLong(specificOffset);
                    setSpecificOffset = true;
                } catch (NumberFormatException ex) {
                    logger.error("Unable to understand autoOffset value of " + forcedOffset, ex);
                    setSpecificOffset = false;
                }
            }

            //serialization properties
            properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                    "org.apache.kafka.common.serialization.StringDeserializer");

            if (noavro) {
                properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                        NoOpDeserializer.class);
                logger.info("Using the NoOp Deserializer");
            } else {
                properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                        com.bbn.tc.schema.serialization.kafka.KafkaAvroGenericDeserializer.class);

                logger.info("Using the real Avro Deserializer");
            }

            properties.put(AvroConfig.SCHEMA_READER_FILE, consumerSchemaFilename);
            properties.put(AvroConfig.SCHEMA_WRITER_FILE, producerSchemaFilename);
            properties.put(AvroConfig.SCHEMA_SERDE_IS_SPECIFIC, isSpecific);
            consumer = new InstrumentedKafkaConsumer<>(properties);
            if (Instrumented) {
                InstrumentationManager.turnOnInstrumentationServer();
            }

            if (setSpecificOffset) {
                logger.info("Going to set specific offset for each partition to " + forcedOffset);
                consumer.subscribe(Arrays.asList(topics.split(",")), new ForceOffsetConsumerRebalanceListener());
            } else {
                consumer.subscribe(Arrays.asList(topics.split(",")));
            }

            // Unit tests check for this title pattern (End-to-End Latency), edit the tests if you change this
            latencyStats = new Utils.Stats(1, "End-to-End Latency (including Avro Serialization)", "ms");
        }
    }

    public void setShutdown(){
        this.shutdown.set(true);
    }

    public void setReportingPeriod(int ms) {
        this.reportingPeriod = ms;
    }

    public void setMinimalWork(boolean flag) {
        minimalWork = flag;
    }

    public void setBusyWork(boolean flag) {
        busyWork = flag;
    }

    public void setN(int newN) {
        N = newN;
    }

    @SuppressWarnings("unused")
    public void run(){
        logger.info("Started NewConsumer");
        recordCounter = 0;

        if(this.file != null && this.fileDeserializer == null)
            throw new IllegalStateException("Instance is not initialized properly, fileDeserialzer is null");

        long sentMillis=0;
        boolean receivedSomethingYet = false;
        GenericContainer tmpRecord = null;
        ConsumerRecords<String, GenericContainer> records = null;
        List<GenericContainer> fileRecords = null;

        long initDuration = duration;
        if (consumeAll) {
            if (duration == 0) {
                logger.info("ConsumeAll requires a duration, setting to 10 seconds, if there are no more records in 10 seconds, we're done");
                duration = 10;
            }
            initDuration = duration * 5;
        }

        long endTime = (duration == 0) ? Long.MAX_VALUE : System.currentTimeMillis() + initDuration*1000;
        long startTime = System.currentTimeMillis();
        long lastReport = startTime;
        long windowRecordCounter = 0;

        try{
            while (!shutdown.get() && System.currentTimeMillis() <= endTime
                    && (maxRecords == -1 || recordCounter < maxRecords)) {

                // Consuming from file
                if(fileDeserializer != null) {
                    // =================== <File consumer> ===================
                    fileRecords = fileDeserializer.deserializeNRecordsFromFile(N);
                    if(fileRecords == null || fileRecords.size() < N) setShutdown(); // we are done comsuming from file
                    // print something to show we started receiving records
                    if (!receivedSomethingYet && fileRecords != null && fileRecords.size() > 0) {
                        logger.info("Receiving file records, please wait...");
                        receivedSomethingYet = true;
                    } 
                    boolean atleastOne = false;
                    Distribution curRecordSize = new Distribution("cur");
                    for (GenericContainer record : fileRecords) {
                        processRecord("0", record); // set key to 0 (Doesnt matter)
                        recordCounter++;
                        atleastOne = true;
                        if (busyWork) {
                            curRecordSize.sample(busyWorkInt);
                        }
                        if (recordCounter == 1) {
                            // Start timing at 1, so we skip any initilization time
                            long ost = startTime;
                            startTime = System.currentTimeMillis();
                            long initTime = startTime - ost;
                            lastReport = startTime;
                            logger.info("Initialization Time: "+initTime);
                        }
                    }
                    if (atleastOne && consumeAll) {
                        // Reset the clock, we got another record
                        endTime = System.currentTimeMillis() + (duration*1000);
                        logger.debug("Updating the endTime clock by "+(duration*1000)+" since consumeAll is set");
                    }
                    // =================== <File consumer> ===================
                }
                // Consuming from kafka topic
                else {
                    // =================== <KAFKA consumer> ===================
                    records = consumer.poll(N);
                    // print something to show we started receiving records
                    if (!receivedSomethingYet && records != null && records.count() > 0) {
                        logger.info("Receiving kafka records, please wait...");
                        receivedSomethingYet = true;
                    }
                    boolean atleastOne = false;
                    Distribution curRecordSize = new Distribution("cur");
                    for (ConsumerRecord<String, GenericContainer> record : records) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Consumed key "+record.key()+" from partition "+record.partition());
                        }
                                                
                        if (!minimalWork) {
                            curRecordSize.sample(record.serializedValueSize());
                            processRecord(record.key(), record.value(), record.timestamp(), record.timestampType());
                            logger.debug("Record size: "+record.serializedValueSize());
                            if (Instrumented && InstrumentedKafkaConsumer.extraInstrumentation) {
                                ((InstrumentedKafkaConsumer<String, GenericContainer>)consumer).
                                recordCDMMetrics(record);
                            }
                        }
                        recordCounter++;

                        if (recordCounter == 1) {
                            // Start timing at 1, so we skip any initilization time
                            long ost = startTime;
                            startTime = System.currentTimeMillis();
                            lastReport = startTime;
                            long initTime = startTime - ost;
                            logger.info("Initialization Time: "+initTime);
                        }

                        atleastOne = true;
                        if (maxRecords > -1 && recordCounter >= maxRecords) {
                            logger.info("Received "+maxRecords+" records, stopping");
                            break;
                        }
                    }
                    
                    if (!minimalWork) {
                        computedRecordSize.merge(curRecordSize);
                    }
                    
                    if (atleastOne && consumeAll) {
                        // Reset the clock, we got another record
                        endTime = System.currentTimeMillis() + (duration*1000);
                        logger.debug("Updating the endTime clock by "+(duration*1000)+" since consumeAll is set");
                    }

                    if (atleastOne) {
                        logger.debug("Consumed "+recordCounter+" total records, polled for "+N);
                    }

                    long curTime = System.currentTimeMillis();
                    if ((curTime - lastReport) > reportingPeriod) {
                        if (computedRecordSize.count > 0) {
                            logger.info(computedRecordSize.toString());
                            recordSize = (int)Math.round(computedRecordSize.mean);
                        }
                        printStats(lastReport, curTime, (recordCounter - windowRecordCounter));
                        if ((recordCounter - windowRecordCounter) == 0) {
                            if (consumeAll || duration > 0) {
                                long remaining = (endTime - System.currentTimeMillis());
                                logger.info("Waiting for "+remaining+" ms for more records before exiting");
                            } else if (maxRecords > -1) {
                                logger.info("Waiting for "+(maxRecords - recordCounter)+" more records before exiting");
                            } else {
                                logger.info("Waiting indefinitely for more records");
                            }                                    
                        }
                        windowRecordCounter = recordCounter;	
                        lastReport = curTime;	    
                    }
                    // =================== </KAFKA consumer> ===================
                }
            }
            long finishedTime = System.currentTimeMillis();
            logger.info("Duration "+duration+" (ms) elapsed. Printing stats.");
            if (computedRecordSize.count > 0) {
                logger.info(computedRecordSize.toString());
                recordSize = (int)Math.round(computedRecordSize.mean);
            }
            printStats(startTime, finishedTime, recordCounter);

            if(latencyStats != null) logger.info(latencyStats.toString());
            closeConsumer();
            logger.info("Done.");

        } catch(Exception e){
            logger.error("Error while consuming", e);
            e.printStackTrace();
        }

    }

    protected void printStats(long start, long end, long count) {
        long elapsed = (end - start);
        logger.info("Elapsed time: "+elapsed+" Record Count: "+count);
        if (recordSize > -1) {
            long bytes = (count * recordSize);
            double mb = (double)bytes / (1024.0 * 1024.0);
            logger.info("Consumed MB: "+mb+" Consumed MB/s: "+(mb / ((double)elapsed / 1000.0)));
        } else {
            logger.warn("Unknown record size, turn minimal work off or provide the record size as a param");
        }
    }
    
    /**
     * process record with a kafka timestamp
     *   kafkaTimestamp is the time when the record was produced or stored in a kafka broker, TimestampType says which
     * The default implementation is to ignore these parameters and just call the old processRecord
     * Implemetations can override this if they want to access the kafkaTimestamp, or override the other if they don't
     * @param key
     * @param tmpRecord
     * @param kafkaTimestamp
     * @param kafkaTimestampType
     */
    protected void processRecord(String key, GenericContainer tmpRecord, long kafkaTimestamp, TimestampType kafkaTimestampType) throws Exception {
        processRecord(key, tmpRecord);
    }

    protected void processRecord(String key, GenericContainer tmpRecord) throws Exception {
        if (key != null) {
            try {
                long delta = System.currentTimeMillis() - Long.parseLong(key);
                if(delta > 0 && latencyStats != null) latencyStats.sample(delta);
            } catch (NumberFormatException ex) {
            }
        }

        if (tmpRecord == null) {
            if (logger.isDebugEnabled()) logger.debug("Received null record ");
            return;

        }
        if (tmpRecord.getSchema() == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Received record with unknown schema " + tmpRecord.toString());
            }
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Consumed record of type " + tmpRecord.getSchema().getFullName() +
                    " {" + recordCounter + " records}");
            if (logger.isTraceEnabled()) {
                // salt method checks for this log pattern, so don't change it without editing /srv/salt/_modules/kafka.py
                logger.trace("Consumed Record: " + toJsonString(tmpRecord));
            }
        }
        if (busyWork) {
            // Generate the json string
            String s = toJsonString(tmpRecord);
            // Do something with it so no one optimizes this away
            busyWorkInt = s.length();
        }
    }

    public String toJsonString(GenericContainer record) throws Exception{
        String jsonString = jsonSerializer != null ? jsonSerializer.serializeToJson(record, false)
                : record.toString();
        if(jsonString.startsWith("\n")) jsonString = jsonString.replaceFirst("\n", "");
        return jsonString;
    }

    protected void closeConsumer() {
        if(consumer != null) {
            logger.info("Closing consumer session ...");
            consumer.commitSync();
            logger.info("Committed");
            consumer.unsubscribe();
            logger.info("Unsubscribed");
            consumer.close();
            logger.info("Consumer session closed.");
        }
        InstrumentationManager.turnOffInstrumentationServer();
        if(fileDeserializer != null)
            fileDeserializer.close();
    }

    public class ForceOffsetConsumerRebalanceListener implements ConsumerRebalanceListener {

        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            // Only force the offsets to a specific value after the first assignment
            if (!setSpecificOffset) {
                return;
            }
            setSpecificOffset = false;

            Set<TopicPartition> consumer_assignment = consumer.assignment();
            logger.info("Setting a specific offset: "+forcedOffset+" for "+consumer_assignment.size()+" TopicPartitions");
            if (forcedOffset < 0) {
                logger.info("Seeing to the end of assigned partitions");
                consumer.seekToEnd(consumer_assignment);
            }
            for (TopicPartition partition : consumer_assignment) {
                if (forcedOffset < 0) {
                    long partitionOffset = consumer.position(partition);
                    logger.info("Last offset for "+partition+" is "+partitionOffset);
                    if (partitionOffset > 0) {
                        partitionOffset = partitionOffset + forcedOffset;
                        consumer.seek(partition, partitionOffset);
                        logger.info("Setting offset for "+partition+" to "+partitionOffset);
                    }
                } else {
                    consumer.seek(partition, forcedOffset);
                    logger.info("Setting offset for "+partition+" to "+forcedOffset);
                }
            }
        }

        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {}

    }

}
