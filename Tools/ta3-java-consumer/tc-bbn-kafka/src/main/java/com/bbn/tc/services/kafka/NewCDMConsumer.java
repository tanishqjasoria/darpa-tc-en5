/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.services.kafka;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.bbn.tc.schema.avro.cdm20.*;

import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericData;
import org.apache.kafka.common.record.TimestampType;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.bbn.tc.services.kafka.checker.CDMChecker;
import com.bbn.tc.services.kafka.checker.TimestampCDMChecker;

/**
 * This is an example Consumer that parses and processes the consumed records
 * @author jkhoury
 */
public class NewCDMConsumer extends NewConsumer {
    private static final Logger logger = Logger.getLogger(NewCDMConsumer.class);
    public static final String SEPARATOR = ", ";
    
    public static String topicStr = null;
    public static String[] topics = null;
    protected static boolean verbose = false;
    protected static String kafkaServer = "localhost:9092";
    protected static String producerSchemaFilename = "/opt/starc/avro/TCCDMDatum.avsc";
    protected static String consumerSchemaFilename = "/opt/starc/avro/TCCDMDatum.avsc";
    protected static String consumerGroupID = "NewCDMConsumer";
    protected static boolean consumeAll = false;
    protected static boolean minimalWork = false;
    protected static String autoOffset = "earliest";
    protected static String specificOffset = null;
    protected static String checkerPropertiesFile = "checker.properties";
    public static boolean runCheckers = false;
    
    protected static File consumeFromFile = null;
    
    protected static long maxRecords = -1;
    protected static int defaultDuration = 300;
    
    ArrayList<CDMChecker> cdmCheckers;
    ArrayList<TimestampCDMChecker> cdmCheckersTimestamp; // Subset of CDM checkers that implement the timestamp version of process record
    boolean hasTimestampCheckers = false;
    
    public NewCDMConsumer(String kafkaServer, String groupId, String topics, int duration, boolean consumeAll,
                          boolean isSpecific, String consumerSchemaFilename, String producerSchemaFilename,
                          String autoOffset, String specificOffset, boolean noavro, long maxRecords, int recordSize, 
                          File file, String securityProtocol, String truststoreLocation, String truststorePassword, 
                          String keystoreLocation, String keystorePassword, String sslKey) {
        super(kafkaServer, groupId, topics, duration, consumeAll, isSpecific, consumerSchemaFilename,
                producerSchemaFilename, autoOffset, specificOffset, noavro, maxRecords, recordSize, file,
                securityProtocol, truststoreLocation, truststorePassword, keystoreLocation, keystorePassword, sslKey);
        logger.info(getConfig());
        initializeCheckers();
    }

    public NewCDMConsumer() {
        this(kafkaServer, consumerGroupID, topicStr, defaultDuration, consumeAll, true, consumerSchemaFilename,
                producerSchemaFilename, autoOffset, specificOffset, false, maxRecords, -1, consumeFromFile);
    }
    
    public NewCDMConsumer(String kafkaServer, String groupId, String topics, int duration, boolean consumeAll,
                          boolean isSpecific, String consumerSchemaFilename, String producerSchemaFilename,
                          String autoOffset, String specificOffset, boolean noavro, long maxRecords, int recordSize, 
                          File file) {
        super(kafkaServer, groupId, topics, duration, consumeAll, isSpecific, consumerSchemaFilename,
                producerSchemaFilename, autoOffset, specificOffset, noavro, maxRecords, recordSize, file);
        logger.info(getConfig());
        initializeCheckers();
    }

    private void initializeCheckers() {
        cdmCheckers = new ArrayList<CDMChecker>();
        cdmCheckersTimestamp = new ArrayList<TimestampCDMChecker>();

        // Load a checkers properties file with the list of checkers in checkers.list, 
        //   and any other checker specific properties
        if (!runCheckers) {
            logger.info("Checkers disabled, run with -check to enable them");
            return;
        }
        Properties cProperties = new Properties();
        try {   
            URL url = this.getClass().getClassLoader().getResource(checkerPropertiesFile);
            if (url != null) {
                logger.info("Loading Checker properties from: "+url.toURI().getPath());
                InputStream pStream = url.openStream();
                cProperties.load(pStream);
            } else {
                File f = new File(checkerPropertiesFile);
                if (f.exists()) {
                    logger.info("Loading Checker properties from: "+f.getAbsolutePath());
                    cProperties.load(new FileReader(f));
                } else {
                    logger.error("Unable to find checker properties file: "+checkerPropertiesFile);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        String cList = cProperties.getProperty("checkers.list");
        if (cList != null) {
            String[] checkerList = cList.split(",");
            for (String cName : checkerList) {
                String cClassName = cName.trim();
                if (cClassName.indexOf(".") == -1) {
                    cClassName = "com.bbn.tc.services.kafka.checker."+cClassName;
                }

                try {
                    Class<?> checkerClass = Class.forName(cClassName);
                    CDMChecker checker = (CDMChecker)checkerClass.newInstance();
                    cdmCheckers.add(checker);
                    if (checker instanceof TimestampCDMChecker) {
                        cdmCheckersTimestamp.add((TimestampCDMChecker)checker);
                        hasTimestampCheckers = true;
                    }
                    logger.info(checker.description());
                    checker.initialize(cProperties, jsonSerializer);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    public void handleCheckers(String key, TCCDMDatum record) {
        for (CDMChecker checker : cdmCheckers) {
            try {
                checker.processRecord(key, record);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void handleCheckersTimestamp(String key, TCCDMDatum record, long timestamp, TimestampType tsType) {
        for (TimestampCDMChecker checker : cdmCheckersTimestamp) {
            try {
                checker.processRecordWithKafkaTimestamp(key, record, timestamp, tsType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void processRecord(String key, GenericContainer record, long timestamp, TimestampType tsType) throws Exception {
        if (minimalWork) return;
        if (hasTimestampCheckers && record instanceof TCCDMDatum) {
            handleCheckersTimestamp(key, (TCCDMDatum)record, timestamp, tsType);
        }
        processRecord(key, record);
    }
    
    @Override
    protected void processRecord(String key, GenericContainer record) throws Exception {
        if (minimalWork) return;
    	if (key != null) {
    		try {
    			long delta = System.currentTimeMillis() - Long.parseLong(key);
    			if(delta > 0 && latencyStats != null) latencyStats.sample(delta);
    		} catch (NumberFormatException ex) {
    		}
    	}
        if(record == null) return;
        /**
         * Records can be of different types depending on the consumer flags. If you pass -c to the Consumer
         *  this will set the isSpecific flag and the records will be consumed as compiled objects. The record types:
         *  - Generic records are not typed: these are simple k/v tables (like with python)
         *  - Compiled records are typed: these are TCCDMDatum objects
         */
        if (record instanceof GenericData.Record) processGenericRecord(key, record);

        else if (record instanceof TCCDMDatum) processCompiledRecord(key, (TCCDMDatum) record);

        else throw new IllegalArgumentException("Bad record type "+record.getClass().getCanonicalName());

    }

    protected void processGenericRecord(String key, GenericContainer record) throws Exception {
        logger.info("CDM generic record " + jsonSerializer.serializeToJson(record,
                logger.isTraceEnabled() ? true : false)
        );
    }

    /**
     * This is the more interesting case and we recommend you always use compiled record processing
     *
     * @see {@link com.bbn.tc.schema.utils.RecordGenerator} for how the compiled records are generated.
     * This will help with how to parse
     * @param datum
     * @throws Exception
     */
    protected void processCompiledRecord(String key, TCCDMDatum datum)throws Exception {
        if (minimalWork) return;
        handleCheckers(key, datum);
        
        Object record = datum.getDatum();
        if(logger.isDebugEnabled()) logger.debug("Processing CDM"+datum.getCDMVersion()
                + " record of type " + record.getClass().getCanonicalName()
                + " with key " + key);

        if(record instanceof Principal) processPrincipal((Principal)record);
        else if(record instanceof ProvenanceTagNode) processProvenanceTagNode((ProvenanceTagNode)record);
        else if(record instanceof Subject) processSubject((Subject)record);
        else if(record instanceof FileObject) processFileObject((FileObject)record);
        else if(record instanceof IpcObject) processIpcObject((IpcObject)record);
        else if(record instanceof RegistryKeyObject) processRegistryKeyObject((RegistryKeyObject)record);
        else if(record instanceof NetFlowObject) processNetFlowObject((NetFlowObject)record);
        else if(record instanceof MemoryObject) processMemoryObject((MemoryObject)record);
        else if(record instanceof SrcSinkObject) processSrcSinkObject((SrcSinkObject)record);
        else if(record instanceof Event) processEvent((Event)record);
        else if(record instanceof UnitDependency) processUnitDependency((UnitDependency)record);
        else if(record instanceof TimeMarker) processTimeMarker((TimeMarker)record);
        else if(record instanceof Host) processHost((Host)record);
        else if(record instanceof Interface) processInterface((Interface)record);
        else if(record instanceof HostIdentifier) processHostIdentifier((HostIdentifier)record);
        else if(record instanceof EndMarker) processEndMarker((EndMarker)record);
        else if(record instanceof CryptographicHash) processCryptographicHash((CryptographicHash)record);
        else if(record instanceof PacketSocketObject) processPacketSocketObject((PacketSocketObject)record);
        else if(record instanceof Host) processHost((Host)record);
        else if(record instanceof UnknownProvenanceNode) processUnknownProvenanceNode((UnknownProvenanceNode)record);
        
        else {
            logger.warn("Unexpected CDM record of type "+record.getClass().getCanonicalName());
        }

        if(logger.isTraceEnabled()) logger.trace(jsonSerializer.serializeToJson(datum, true));
        /*if (busyWork) {
            // Generate the json string
            String s = jsonSerializer.serializeToJson(datum, true);
            // Do something with it so no one optimizes this away
            busyWorkInt = s.length();
        }*/
    }

    protected void processHost(Host record) throws Exception {
        // TODO: TA2 performers update this
    }

    protected void processUnknownProvenanceNode(UnknownProvenanceNode record) throws Exception {
        // TODO: TA2 performers update this
    }
    
    protected void processInterface(Interface record) throws Exception {
        // TODO: TA2 performers update this
    }

    protected void processPacketSocketObject(PacketSocketObject record) throws Exception {
        // TODO: TA2 performers update this
    }
    
    protected void processHostIdentifier(HostIdentifier record) throws Exception {
        // TODO: TA2 performers update this
    }
    
    protected void processIpcObject(IpcObject record) throws Exception {
        // TODO: TA2 performers update this
    }

    protected void processTimeMarker(TimeMarker record) throws Exception {
        // TODO: TA2 performers update this
    }

    
    protected void processEndMarker(EndMarker record) throws Exception {
        // TODO: TA2 performers update this
    }
    
    protected void processCryptographicHash(CryptographicHash record) throws Exception {
        // TODO: TA2 performers update this
    }
    
    protected void processUnitDependency(UnitDependency record) throws Exception {
        // TODO: TA2 performers update this
    }

    protected void processRegistryKeyObject(RegistryKeyObject record) throws Exception {
        // TODO: TA2 performers update this
    }

    protected void processPrincipal(Principal record) throws Exception {
        // TODO: TA2 performers update this
    }

    protected void processSrcSinkObject(SrcSinkObject record) throws Exception {
        // TODO: TA2 performers update this
    }

    protected void processNetFlowObject(NetFlowObject record) throws Exception {
        // TODO: TA2 performers update this
    }

    protected void processMemoryObject(MemoryObject record) throws Exception {
        // TODO: TA2 performers update this
    }

    protected void processFileObject(FileObject record) throws Exception {
        // TODO: TA2 performers update this
    }

    protected void processEvent(Event record) throws Exception {
        // TODO: TA2 performers update this, providing an example here
        UUID uuid = record.getUuid();
        try {
            Long sequence = record.getSequence();
            
            boolean debugToWarn = false;
            
            EventType type = record.getType();
            Integer threadId = record.getThreadId();

            // **** all the rest of the fields are optional so check for null ***

            String name = record.getNames()!=null ? record.getNames().toString() : null;
            List<Value> parameters = record.getParameters();
            long location = record.getLocation()!=null?record.getLocation() : -1;
            long size = record.getSize()!=null?record.getSize() : -1;
            String programPoint = record.getProgramPoint() != null ? record.getProgramPoint().toString() : null;
            Map<CharSequence, CharSequence> properties = record.getProperties();
            long timeStampMicros = record.getTimestampNanos() != null ? record.getTimestampNanos() : -1;

            // just printing it here
            StringBuffer sbuf = new StringBuffer();
            sbuf.append(record.getClass().getSimpleName()).append(" {")
            .append(Arrays.toString(uuid.bytes())).append(SEPARATOR)
            .append(sequence).append(SEPARATOR)
            .append(type).append(SEPARATOR)
            .append(threadId).append(SEPARATOR)
            .append(timeStampMicros).append(SEPARATOR)
            .append(name).append(SEPARATOR)
            .append(location).append(SEPARATOR)
            .append(size).append(SEPARATOR)
            .append(programPoint).append(SEPARATOR)
            .append("[");
            if(parameters != null)
                for(Value value:parameters) { // Value is a typed object and can processed just the same way
                    sbuf.append("{").append(value.getType()).append(SEPARATOR)
                    .append(value.getSize()).append(SEPARATOR)
                    .append(value.getValueDataType()).append(SEPARATOR)
                    .append(value.getValueBytes() != null ? Arrays.toString(value.getValueBytes().array()):null)
                    .append(SEPARATOR)
                    .append(value.getTag()).append("}");
                    // TODO: still need to iterate and append component values, this is just an example here
                }
            sbuf.append("]").append(SEPARATOR);
            if(properties != null) {
                Set<CharSequence> keys = properties.keySet();
                sbuf.append("[");
                for (CharSequence key:keys) sbuf.append(key.toString()).append(":").append(properties.get(key))
                .append(SEPARATOR);
                sbuf.append("]");
            }

            if (debugToWarn) {
                logger.warn(sbuf.toString());
            } else {
                if(logger.isDebugEnabled()) logger.debug(sbuf.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void processSubject(Subject record) throws Exception {
        // TODO: TA2 performers update this
    }

    protected void processProvenanceTagNode(ProvenanceTagNode record) throws Exception {
        // TODO: TA2 performers update this
    }


    /**
     * Parse the argument list, doing some error checking for each specific argument type
     * If errorOnExtras is true, return false and abort processing if we get any unknown parameters
     * If errorOnExtras is false, allow unknown parameters by ignoring them.
     */
    protected static boolean parseArgs(String [] args, boolean errorOnExtras) {
        if(args.length < 1) {
            return false;
        }
        if(args[0].startsWith("-")){
            return false;
        }

        topicStr = args[0];
        if(topicStr == null || topicStr.isEmpty()) return false;
        topics = topicStr.split(",");

        logger.info("NewCDMConsumer subscribing to "+topicStr);

        int index = 1;
        //parse the options
        // TODO: Code duplication with Main!
        while(index < args.length){
            String option = args[index].trim();
            if (option.equals("-v")){
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
            } else if(option.equals("-ks")){
                index++;
                kafkaServer = args[index];
                if(kafkaServer.startsWith("-")){
                    System.err.println("Bad kafka server");
                    return false;
                }
            } else if(option.equals("-psf")){
                index++;
                producerSchemaFilename = args[index];
                if(producerSchemaFilename.startsWith("-")){
                    System.err.println("Bad producer schema file name");
                    return false;
                } 
            } else if(option.equals("-csf")){
                index++;
                consumerSchemaFilename = args[index];
                if(consumerSchemaFilename.startsWith("-")){
                    System.err.println("Bad consumer schema file name");
                    return false;
                } 
            } else if(option.equals("-d")) {
                index++;
                String dStr = args[index];
                try {
                    defaultDuration = Integer.parseInt(dStr);
                } catch (NumberFormatException ex) {
                    System.err.println("Bad duration parameter, expecting an int (seconds)");
                    return false;
                } 
            } else if(option.equals("-g")){
                index++;
                consumerGroupID = args[index];
            } else if (option.equals("-mr")) {
                index++;
                try {
                    maxRecords = Long.parseLong(args[index]);
                } catch (Exception e) {
                    System.err.println("Bad Max Records parameter");
                    return false;
                }
            } else if (option.equals("-call")) {
                consumeAll = true;
            } else if (option.equals("-co")) {
                index++;
                autoOffset = args[index];
            } else if (option.equals("-offset")) {
                index++;
                specificOffset = args[index];
            } else if (option.equals("-f")) {
                index++;
                consumeFromFile = new File(args[index]);
                if (!consumeFromFile.exists()) {
                    System.err.println("Consume from file: "+args[index]+" doesnt exist");
                    return false;
                }
            } else if (option.equals("-mr")) {
                index++;
                try {
                    maxRecords = Long.parseLong(args[index]);
                } catch (NumberFormatException ex) {
                    System.err.println("Unable to parse maxRecords: "+args[index]);
                    return false;
                }  
            } else if (option.equals("-rg")) {
                java.util.UUID gid = java.util.UUID.randomUUID();
                consumerGroupID = gid.toString();
            } else if (option.equals("-min")) {
                minimalWork = true;
            } else if (option.equals("-nometrics")) {
                NewConsumer.Instrumented = false;
                Producer.Instrumented = false;
            } else if (option.equals("-pullmetrics")) {
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
            } else if (errorOnExtras) {
                System.err.println("Bad option " + option);
                return false;
            } 
            index++;
        }
        return true;
    }

    protected static String usage() {
        return "NewCDMConsumer [<topics>] <options> where options \n" +
                "   <topics>  is a comma separated list of kafka topics to publish EndOfMessages to\n" +
                "   <options>  include the following:\n" +
                "     -ks   the kafka server needed by the consumer/producer for bootstrapping\n" +
                "     -psf  producer/writer avro graph record schema file \n" +
                "     -csf  consumer/writer avro graph record schema file \n" +
                "     -v    verbose (default false) \n" +
                "     -g    group id (default TopicStatusConsumer)\n" +
                "     -call consume all records in the topic (default false)\n" +
                "     -co   consumer auto offset earliest|latest (default earliest)\n" +
                "     -offset specific offset to start consuming from (default null)\n" +
                "     -f    instead of consuming from kafka, consume from this file (default null)\n" + 
                "     -mr   max number of records to consume (default -1 (no limit))\n"+
                "     -kv   kafka verbose (kafka logging to DEBUG)\n";               
    }

    protected String getConfig(){
        char separator = '=';
        char fseparator = ',';

        StringBuffer buffer = new StringBuffer();
        buffer.append("topic").append(separator).append(topics).append(fseparator)
        .append("verbose").append(separator).append(verbose).append(fseparator)
        .append("kserver").append(separator).append(kafkaServer).append(fseparator)
        .append("consumerGroupID").append(separator).append(consumerGroupID).append(fseparator)
        .append("consumeFromFile").append(separator).append(consumeFromFile).append(fseparator)
        .append("producerSchema").append(separator).append(producerSchemaFilename).append(fseparator)
        .append("consumerSchema").append(separator).append(consumerSchemaFilename).append(fseparator)
        .append("consumeAll").append(separator).append(consumeAll).append(fseparator)
        .append("autoOffset").append(separator).append(autoOffset).append(fseparator)
        .append("specificOffset").append(separator).append(specificOffset).append(fseparator)
        .append("maxRecords").append(separator).append(maxRecords).append(fseparator);

        return buffer.toString();
    }
    
    @Override
    protected void closeConsumer() {
	super.closeConsumer();
        for (CDMChecker checker : cdmCheckers) {
            checker.close();
        }
    }

}
