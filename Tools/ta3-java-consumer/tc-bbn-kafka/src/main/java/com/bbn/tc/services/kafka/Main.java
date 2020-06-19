/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.services.kafka;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.tool.DataFileGetSchemaTool;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.bbn.tc.schema.serialization.AvroConfig;
import com.bbn.tc.schema.serialization.Utils;
import com.bbn.tc.schema.serialization.kafka.KafkaAvroGenericSerializer;
import com.bbn.tc.schema.utils.RecordGenerator;

/**
 * Starts a kafka producer and/or consumer
 * Created by jkhoury on 7/13/15.
 */
public class Main {

    private static final Logger logger = Logger.getLogger("KafkaDemo");
    // Common properties
    protected static String topic;
       
    protected static boolean verbose = false;
    protected static boolean extraVerbose = false;
    protected static int duration = 300; //in seconds, 0 means forever
    protected static boolean consumeAll = false;

    // Producer properties
    protected static String kafkaServer = "localhost:9092";
    protected static String producerID = "demoProducer";
    protected static String filename = null;
    protected static boolean np = false;
    protected static boolean isasync = false;
    protected static int delay = 0; //in ms, 0 means no delay
    protected static boolean newConsumer = true;
    protected static String producerSchemaFilename;
    protected static int n;
    protected static boolean isSpecific=false;
    protected static boolean withJson=false;
    protected static boolean nullKey = false;
    
    protected static long maxMB = -1; // maximum number of megabytes records to produce or consume, -1 means no max
    protected static long maxRecords = -1; // maximum number of records to produce or consume, -1 means no max
    protected static boolean randomRecord = true; // Generate new random record data each time
    protected static boolean noavro = false; // Use avro, set to true to use the NoOpSerializer (just for performance tests)
    protected static String consumerAutoOffset = "latest"; // Consumer.AUTO_OFFSET_RESET_CONFIG: latest or earliest
   
    protected static String specificOffset = null; // Specific offset to start with for each partition, negative numbers are relative to the end
                                         // specific offset of -1 will consume the last item published in each partition
    protected static int recordSize = -1;  // Computed
    protected static int reportingPeriod = -1;

    // Consumer propoerties
    protected static  String groupId = "demo-group";
    protected static boolean nc = false;
    protected static String consumerSchemaFilename;
    protected static boolean isCDMConsumer = false;
    protected static boolean minimalWork = false;
    protected static boolean busyWork = false;
    protected static int pollPeriod = -1;
    
    protected static boolean metrics = true; // Use prometheus metric instrumentation

    protected static File file = null;

    // Security properties
    protected static String securityProtocol;
    protected static String truststoreLocation;
    protected static String truststorePassword;
    protected static String keystoreLocation;
    protected static String keystorePassword;
    protected static String sslKey;
    
    public static void main(String [] args){
        if(!parseArgs(args)) {
            usage();
            System.exit(1);
        }

        printConfig();

        if (maxMB > 0) {
            if (recordSize == -1) {
                recordSize = calculateAverageRecordSize();
            } else {
                logger.info("Provided Average Record Size: "+recordSize);
            }
            logger.info("Serialized record size: "+recordSize);
            long maxRecordsByMB = (maxMB * 1024 * 1024) / recordSize;
            logger.info("Max Records by MB: "+maxRecordsByMB);
            if (maxRecords == -1) {
                maxRecords = maxRecordsByMB;
            } else {
                // We have both maxMB defined and maxRecords. We pick the min of the two
                if (maxRecords > maxRecordsByMB) {
                    maxRecords = maxRecordsByMB;
                }
            }
            logger.info("Max records: "+maxRecords);
        }


        final NewConsumer tconsumer;
        final Producer tproducer;

        if(!nc) {
            if(!checkFile(consumerSchemaFilename)) {
                if (topic.startsWith("file:/") || filename != null) {
                    ArrayList<String> avroargs = new ArrayList<String>();
                    if (topic.startsWith("file:/")) {
                        logger.info("No consumer schema file specified, reading it from topic "+topic);
                        avroargs.add(topic);
                    } else if (filename != null) {
                        logger.info("No consumer schema file specified, reading it from file "+filename);
                        avroargs.add(filename);
                    }
                    DataFileGetSchemaTool avroGetSchema = new DataFileGetSchemaTool();
                    File tempSchema = new File("currentAvroSchema.avsc");
                    try {
                        PrintStream pos = new PrintStream(new FileOutputStream(tempSchema));                    
                        avroGetSchema.run(System.in, pos, System.err, avroargs);
                        pos.close();
                        consumerSchemaFilename = tempSchema.getAbsolutePath();
                    } catch (Exception ex) {
                        logger.error(ex.toString(), ex);
                        ex.printStackTrace();
                    }
                }
            }
            if (!checkFile(consumerSchemaFilename)) {               
                logger.error("Consumer schema file does not exist " + consumerSchemaFilename);
                System.exit(1);
            }
            if(!checkFile(producerSchemaFilename)){
                producerSchemaFilename = consumerSchemaFilename;
            }

            //verify the file record exists
            if(filename != null) {
                file = new File(filename);
                if (!file.exists()) {
                    logger.error("File does not exist " + file.getAbsolutePath());
                    System.exit(1);
                }
            }

            if(isCDMConsumer)
                tconsumer = new NewCDMConsumer(kafkaServer, groupId, topic, duration, consumeAll,
                        isSpecific, consumerSchemaFilename, producerSchemaFilename, consumerAutoOffset,
                        specificOffset, noavro, maxRecords, recordSize, file, securityProtocol, truststoreLocation,
                        truststorePassword, keystoreLocation, keystorePassword, sslKey);
            else tconsumer = new NewConsumer(kafkaServer, groupId, topic, duration, consumeAll,
                    isSpecific, consumerSchemaFilename, producerSchemaFilename, consumerAutoOffset,
                    specificOffset, noavro, maxRecords, recordSize, file, securityProtocol, truststoreLocation,
                    truststorePassword, keystoreLocation, keystorePassword, sslKey);

            if (minimalWork) {
                tconsumer.setMinimalWork(true);
            }
            if (busyWork) {
                tconsumer.setBusyWork(true);
            }
            if (reportingPeriod > -1) {
                tconsumer.setReportingPeriod(reportingPeriod);
            }
            if (pollPeriod > -1) {
                tconsumer.setN(pollPeriod);
            }
            //start the consumer
            tconsumer.start();
        }else{
            tconsumer = null;
        }

        if(!np){

            if(!checkFile(producerSchemaFilename)){
                logger.error("Producer schema file does not exist " + producerSchemaFilename);
                System.exit(1);
            }
            /**
             * Verify that the file does not already exist
             */
            if(filename != null)
                if((file = new File(filename)).exists()){
                    logger.error("Output file already exists " + file.getAbsolutePath());
                    System.exit(1);
                }

            tproducer = new Producer(kafkaServer, producerID, topic, isasync, duration, maxRecords, delay,
                    isSpecific, file, producerSchemaFilename, n, recordSize, randomRecord, noavro, withJson,
                    securityProtocol, truststoreLocation, truststorePassword, keystoreLocation, keystorePassword, sslKey);
            if (nullKey) {
                tproducer.setNullKey(nullKey);
            }
            tproducer.start();
        }else{
            tproducer = null;
        }

        // add a shutdown hook to clean up the consumer
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                try {
                    if(tconsumer != null) {
                        logger.info("Shutting down consumer");
                        tconsumer.setShutdown();
                        tconsumer.join();
                    }
                    if(tproducer != null){
                        logger.info("Shutting down producer");
                        tproducer.setShutdown();
                        tproducer.join();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });


        try {
            if(tconsumer !=null && tconsumer.isAlive()) tconsumer.join();
            if(tproducer != null) tproducer.join();

        } catch (InterruptedException e) {
            logger.error(e);
        }
    }

    protected static boolean checkFile(String filename){
        if(filename == null || !(new File(filename)).exists() )
            return false;
        return true;
    }

    protected static boolean parseArgs(String[] args) {
        return parseArgs(args, true);
    }

    /**
     * Parse the argument list, doing some error checking for each specific argument type
     * If errorOnExtras is true, return false and abort processing if we get any unknown parameters
     * If errorOnExtras is false, allow unknown parameters by ignoring them.
     *  This is used for subclasses that want to call this then handle some extra parameters on their own
     */
    protected static boolean parseArgs(String [] args, boolean errorOnExtras){
        if(args.length < 1) {
            return false;
        }
        if(args[0].startsWith("-")){
            return false;
        }
        topic = args[0];
        if(topic == null || topic.isEmpty()) return false;
        if(topic.startsWith("file:"))
            filename = topic.replace("file:", "").trim();

        int index = 1;
        //parse the options
        while(index < args.length){
            String option = args[index].trim();
            if(option.equals("-nc")){
                nc = true;
            }else if(option.equals("-np")){
                np = true;
            }else if(option.equals("-wj")){
                withJson = true;
            }else if(option.equals("-as")){
                isasync = true;
            }else if(option.equals("-call")) {
                consumeAll = true;
            }else if(option.equals("-v")){
                /*
                 * Increase the log level for the kafka package logger to DEBUG.  We arbitrarily pick the
                 * Producer class to get the package name.
                 * TODO: if we run log4j 2, there is a different way to adjust the log level
                 */
                String kafkaPackage = Producer.class.getPackage().getName();
                Logger.getLogger(kafkaPackage).setLevel(Level.DEBUG);
                verbose = true;
            } else if (option.equals("-kv")) {
                // Extra verbose, producers and consumers will write the content they're producing or consuming to the log file
                // Also turn the Kafka logging to debug
                Logger.getLogger("org.apache.kafka").setLevel(Level.DEBUG);
                String kafkaPackage = Producer.class.getPackage().getName();
                Logger.getLogger(kafkaPackage).setLevel(Level.TRACE);
            } else if (option.equals("-vv")) {
                String kafkaPackage = Producer.class.getPackage().getName();
                Logger.getLogger(kafkaPackage).setLevel(Level.TRACE);
                extraVerbose = true;
	    } else if (option.equals("-q")) {
                String kafkaPackage = Producer.class.getPackage().getName();
                Logger.getLogger(kafkaPackage).setLevel(Level.WARN);
            } else if(option.equals("-c")){
                isSpecific = true;
            }else if(option.equals("-cdm")){
                isSpecific = true; //CDM records have to be specific
                isCDMConsumer = true;
            }else if(option.equals("-pid")){
                index++;
                producerID = args[index];
                if(producerID .startsWith("-")){
                    System.err.println("Bad producer id");
                    return false;
                }
            }else if(option.equals("-psf")){
                index++;
                producerSchemaFilename = args[index];
                if(producerSchemaFilename.startsWith("-")){
                    System.err.println("Bad producer schema file name");
                    return false;
                }
            }else if(option.equals("-csf")){
                index++;
                consumerSchemaFilename = args[index];
                if(consumerSchemaFilename.startsWith("-")){
                    System.err.println("Bad consumer schema file name");
                    return false;
                }
            }else if(option.equals("-g")){
                index++;
                groupId = args[index];
                if(groupId.startsWith("-")){
                    System.err.println("Bad group id");
                    return false;
                }
            }else if(option.equals("-ks")){
                index++;
                kafkaServer = args[index];
                if(kafkaServer.startsWith("-")){
                    System.err.println("Bad kafka server");
                    return false;
                }
            }/*else if(option.equals("-f")){
                index++;
                filename = args[index];
                if(filename.startsWith("-")){
                    System.err.println("Bad filename");
                    return false;
                }
            }*/else if(option.equals("-d")){
                index++;
                try {
                    duration = Integer.parseInt(args[index]);
                }catch (Exception e){
                    System.err.println("Bad duration");
                    return false;
                }
            }else if(option.equals("-delay")){
                index++;
                try {
                    delay = Integer.parseInt(args[index]);
                }catch (Exception e){
                    System.err.println("Bad delay parameter");
                    return false;
                }
            }else if(option.equals("-mb")){
                index++;
                try {
                    maxMB = Long.parseLong(args[index]);
                }catch (Exception e){
                    System.err.println("Bad Max MB parameter");
                    return false;
                }
            } else if (option.equals("-mr")) {
                index++;
                try {
                    maxRecords = Long.parseLong(args[index]);
                } catch (Exception e) {
                    System.err.println("Bad Max Records parameter");
                    return false;
                }
            } else if (option.equals("-sr")) {
                randomRecord = false;
            } else if (option.equals("-noavro")) {
                noavro = true;
            } else if (option.equals("-co")) {
                index++;
                consumerAutoOffset = args[index];
                if (!consumerAutoOffset.equalsIgnoreCase("earliest") &&
                        !consumerAutoOffset.equalsIgnoreCase("latest") &&
                        !consumerAutoOffset.equalsIgnoreCase("none")) {
                    System.err.println("Bad consumerAutoOffset parameter (earliest | latest | none): "+consumerAutoOffset);
                    return false;
                }
            } else if (option.equals("-offset")) {
                index++;
                specificOffset = args[index];
            } else if (option.equals("-rg")) {
                UUID gid = UUID.randomUUID();
                groupId = gid.toString();
            } else if (option.equals("-min")) {
                minimalWork = true;
	    } else if (option.equals("-busy")) {
	        busyWork = true;
            } else if(option.equals("-n")){
                index++;
                try {
                    n = Integer.parseInt(args[index]);
                } catch (Exception e) {
                    System.err.println("Bad n");
                    return false;
                }
            } else if(option.equals("-rsize")){
                index++;
                try {
                    recordSize = Integer.parseInt(args[index]);
                } catch (Exception e) {
                    System.err.println("Bad recordSize");
                    return false;
                }
            } else if(option.equals("-rperiod")){
                index++;
                try {
                    reportingPeriod = Integer.parseInt(args[index]);
                } catch (Exception e) {
                    System.err.println("Bad reportingPeriod");
                    return false;
                }
            } else if(option.equals("-pperiod")){
                index++;
                try {
                    pollPeriod = Integer.parseInt(args[index]);
                } catch (Exception e) {
                    System.err.println("Bad pollPeriod");
                    return false;
                }
            } else if (option.equals("-nometrics")) {
                metrics = false;
                NewConsumer.Instrumented = false;
                Producer.Instrumented = false;
            } else if (option.equals("-pullmetrics")) {
                metrics = true;
                Producer.Instrumented = true;
                NewConsumer.Instrumented = true;
                InstrumentationManager.InstrumentationPull = true;
            } else if (option.equals("-mport")) {
                index++;
                try {
                    InstrumentationManager.instrumentationServerPort = Integer.parseInt(args[index]);
                } catch (Exception e) {
                    System.err.println("Bad metrics port");
                    return false;
                }
            } else if (option.equals("-pushaddr")) {
                index++;
                InstrumentationManager.pushGatewayAddress = args[index];
            } else if (option.equals("-pushms")) {
                index++;
                try {
                    InstrumentationManager.pushPeriodMs = Integer.parseInt(args[index]);
                } catch (Exception e) {
                    System.err.println("Bad push gateway period");
                    return false;
                }
            } else if (option.equals("-pushlabel")) {
                index++;
                InstrumentationManager.pushLabel = args[index];
            } else if (option.equals("-secproto")) {
                index++;
                securityProtocol = args[index].trim();
            } else if (option.equals("-truststore")) { 
                index++;
                truststoreLocation = args[index].trim();
            } else if (option.equals("-trustpass")) {
                index++;
                truststorePassword = args[index].trim();
            } else if (option.equals("-keystore")) {
                index++;
                keystoreLocation = args[index].trim();
            } else if (option.equals("-keypass")) {
                index++;
                keystorePassword = args[index].trim();
            } else if (option.equals("-sslkey")) {
                index++;
                sslKey = args[index].trim();
            } else if (option.equals("-check")) {
                NewCDMConsumer.runCheckers = true;
            } else if (option.equals("-nk")) {
                nullKey = true;
            } else if (errorOnExtras) {
                System.err.println("Bad option " + option);
                return false;
            }
            index++;
        }
        return true;
    }

    protected static void usage() {
        logger.error("Main [<topics> | <filename>] <options> where options \n" +
                "   <topics>  is a comma separated list of kafka topics to publish avro records to (producer) \n" +
                "             or consume records from (consumer), \n" +
                "   <filename> filename to publish avro records to (producer) or consume records from (consumer) \n" +
                "              must have the format \"file:<path>\" \n" +
                "   <options>  include the following:\n" +
                //"     -old  use old consumer (default false)\n" +
                "     -nc   dont start a consumer (default false)\n" +
                "     -np   dont start a producer (default false)\n" +
                "     -ks   the kafka server needed by the consumer/producer for bootstrapping\n" +
                "     -pid  producer id (default demoProducer)\n" +
                "     -psf  producer/writer avro graph record schema file \n" +
                "     -csf  consumer/reader avro graph record schema file \n" +
                "     -as   asynchronous (default false))\n" +
                "     -g    consumer group id (default demo-group)\n" +
                "     -n    number of additional k/v pairs per published node (default 0)\n" +
                "     -v    verbose (default false) \n" +
                "     -vv   extra verbose (default false)\n" +
                "     -kv   kafka verbose (kafka logging to DEBUG)\n" + 
                "     -c    use compiled objects instead of generic records (default false, generic) \n" +
                "     -cdm  use CDMNewConsumer instead of plain NewConsumer to process CDM records (default false) \n" +
                "     -d    producer/consumer duration in sec (default 0 sec), 0=indefinite\n"+
                "     -call consume all records until there is nothing available, then stop after -d seconds\n"+
                "     -mb   max MB of data to publish and generate throughput statistics (default -1, no limit, use duration)\n" +
                "     -mr   max Records to publish (default -1, no limit)\n" +
                "     -sr   static record data, publish the same record data each time (default false, random record data)\n" +
                "     -noavro with static records, don't serialize each publish, use the NoOpSerializer (default false)\n" +
                "     -co   consumer property AUTO_OFFSET_RESET_CONFIG (default latest)\n"+
                "     -offset define a specific offset to start with for each partition (default none)\n" +
                "     -rg   random consumer group (default false, use -g value)\n"+
                "     -wj   serialize to json when serializing to file instead of to avro binary (default false)\n"+
                "     -nometrics  don't use prometheus monitoring\n" +
                "     -pullmetrics Have the metrics server pull from this process via a http server thread, default is push\n" +
                "     -mport Specify the port to use for the prometheus pull server\n" +
                "     -pushaddr Metrics Push Gateway address\n" +
                "     -pushms  Push Gateway period: push metrics every X ms\n" +
                "     -pushlabel Use this label for the push gateway metrics\n" +
                "     -delay    producer publish delay between sends in ms (default 1), 0=no delay\n" +
                "     -secproto security protocol to use (currently SSL supported)\n" +
                "     -truststore path to the Java truststore to use for SSL\n" +
                "     -trustpass password for the Java truststore\n" +
                "     -keystore path to the Java keystore to use for SSL\n" +
                "     -keypass password for the Java keystore\n" +
                "     -sslkey password for the private key in the keystore\n" +
                "     -nk use null for the key for the producer\n");
      
    }

    protected static void printConfig(){
        char separator = '=';
        char fseparator = ',';

        StringBuffer buffer = new StringBuffer();
        buffer.append("topic").append(separator).append(topic).append(fseparator)
        .append("old").append(separator).append(newConsumer).append(fseparator)
        .append("nc").append(separator).append(nc).append(fseparator)
        .append("np").append(separator).append(np).append(fseparator)
        .append("verbose").append(separator).append(verbose).append(fseparator)
        .append("extraVerbose").append(separator).append(extraVerbose).append(fseparator)
        .append("isSpecific").append(separator).append(isSpecific).append(fseparator)
        .append("isCDMConsumer").append(separator).append(isCDMConsumer).append(fseparator)
        .append("kserver").append(separator).append(kafkaServer).append(fseparator)
        .append("producerid").append(separator).append(producerID).append(fseparator)
        .append("addl k/v pairs").append(separator).append(n).append(fseparator)
        .append("producerSchema").append(separator).append(producerSchemaFilename).append(fseparator)
        .append("consumerSchema").append(separator).append(consumerSchemaFilename).append(fseparator)
        .append("async").append(separator).append(isasync).append(fseparator)
        .append("groupid").append(separator).append(groupId).append(fseparator)
        .append("duration").append(separator).append(duration).append(fseparator)
        .append("call").append(separator).append(consumeAll).append(fseparator)
        .append("delay").append(separator).append(delay).append(fseparator)
        .append("maxMB").append(separator).append(maxMB).append(fseparator)
        .append("maxRecords").append(separator).append(maxRecords).append(fseparator)
        .append("randomRecord").append(separator).append(randomRecord).append(fseparator)
        .append("consumerAutoOffset").append(separator).append(consumerAutoOffset).append(fseparator)
        .append("specificOffset").append(separator).append(specificOffset).append(fseparator)
        .append("withJson").append(separator).append(withJson).append(fseparator)                
        .append("noavro").append(separator).append(noavro).append(fseparator)
        .append("minimalWork").append(separator).append(minimalWork).append(fseparator)
        .append("busyWork").append(separator).append(busyWork).append(fseparator)
        .append("metrics").append(separator).append(metrics).append(fseparator)       
        .append(InstrumentationManager.getConfigStr(separator, fseparator))
        .append("filename").append(separator).append(filename)
        .append("nullKey").append(separator).append(nullKey);
 
        logger.info(buffer.toString());
    }

    protected static int calculateAverageRecordSize() {
        int recordSize = -1;
        // Calculating an estimate of the size of a record
        // Generate 10 records, take the average
        // Set the seed, so we generate the same record each run

        // Load the schema
        Schema writerSchema;
        try {
            writerSchema = Utils.loadSchema(producerSchemaFilename != null ? producerSchemaFilename
                    : consumerSchemaFilename);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        KafkaAvroGenericSerializer<GenericContainer> testSerializer = 
                new KafkaAvroGenericSerializer<GenericContainer>();
        Map<String, Object> props = new HashMap<String, Object>();
        // additional serialization properties needed
        props.put(AvroConfig.SCHEMA_WRITER_SCHEMA, writerSchema);
        props.put(AvroConfig.SCHEMA_SERDE_IS_SPECIFIC, true);
        testSerializer.configure(props, false);

        RecordGenerator.setRandomSeed(0l);
        for (int i=0; i<50; ++i) {
            // Get an estimate of record size
            try {
                GenericContainer edge = RecordGenerator.randomEdgeRecord(writerSchema, n, true);
                byte[] bytes = testSerializer.serialize("topic", edge);
                // logger.info("Serialized test edge to "+ Arrays.toString(bytes));
                logger.trace("Random record "+i+": size: "+bytes.length);
                recordSize += bytes.length;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        RecordGenerator.setRandomSeed(System.nanoTime());
        testSerializer.close();

        recordSize = Math.round((float)recordSize / 50.0f);
        logger.info("Serialized record size: "+recordSize);

        return recordSize;
    }
}
