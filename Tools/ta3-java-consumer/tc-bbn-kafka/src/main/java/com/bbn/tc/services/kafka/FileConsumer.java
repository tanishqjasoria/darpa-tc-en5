package com.bbn.tc.services.kafka;

import java.io.File;

import org.apache.avro.generic.GenericContainer;
import org.apache.log4j.Logger;

import com.bbn.tc.schema.avro.cdm20.TCCDMDatum;
import com.bbn.tc.schema.serialization.AvroGenericSerializer;

public class FileConsumer extends NewCDMConsumer {
    
    private static final Logger logger = Logger.getLogger(FileConsumer.class);
    
    public static boolean writeJson = false;
    public static boolean writeBinary = false;
    public static long rolloverRecordCount = 20000000;
    public static String outputFilePrefix = null; // Use the topic name
    public static String outputFileSuffix = null;
    public static String outputDir = ".";
    protected static boolean isSpecific=false;
    public static String filename = null;

    private File outputFileJson;
    private File outputFileBinary;
    
    private int curRecordCount = 0;
    private int curLogIndex = 0;
    
    private AvroGenericSerializer<GenericContainer> jsonFileSerializer = null;
    private AvroGenericSerializer<GenericContainer> binaryFileSerializer = null;
    
    // Temp
    private long skipStartKey = -1l; // 1495607469364l;
    private long skipEndKey = -1l; // 1495608568737l;
    
    public FileConsumer() {
        this(kafkaServer, consumerGroupID, topicStr, defaultDuration,
             consumerSchemaFilename);
    }
    
    public FileConsumer(String kafkaServer, String groupId, String topics,
            int duration, String consumerSchemaFilename) {
        super(kafkaServer, groupId, topics, duration, consumeAll, isSpecific,
                consumerSchemaFilename, consumerSchemaFilename, autoOffset,
                specificOffset, false, maxRecords, -1, consumeFromFile);
        createOutputFiles();
    }
    
    public static boolean parseAdditionalArgs(String[] args) {
        int index = 0;

        if (topicStr.startsWith("file:")) {
            filename = topicStr.replace("file:", "").trim();
        }
        
        //verify the file record exists
        if(filename != null) {
            consumeFromFile = new File(filename);
            if (!consumeFromFile.exists()) {
                logger.error("File does not exist " + consumeFromFile.getAbsolutePath());
                System.exit(1);
            }
        }
        
        while(index < args.length){
            String option = args[index].trim();
            if(option.equals("-roll")){
                index++;
                String periodStr = args[index];
                try {
                    rolloverRecordCount = Integer.parseInt(periodStr);
                } catch (NumberFormatException ex) {
                    System.err.println("Bad rollover parameter, expecting an int (count)");
                    return false;
                } 
            } else if (option.equals("-wj")){
               writeJson = true;
            } else if (option.equals("-wb")) {
               writeBinary = true;
            } else if (option.equals("-ofp")) {
                index++;
                outputFilePrefix = args[index];
            } else if (option.equals("-ofs")) {
                index++;
                outputFileSuffix = args[index];
            } else if (option.equals("-odir")) {
                index++;
                outputDir = args[index];
            } else if(option.equals("-c")){
                isSpecific = true;
            }
            index++;
        } 
        
        if (writeJson == false && writeBinary == false) {
            logger.warn("Neither writeJson nor writeBinary specified, setting writeBinary to true");
            writeBinary = true;
        }
        
        return true;
    }
    
    public static String usage() {
        StringBuffer sb = new StringBuffer(NewCDMConsumer.usage());
        sb.append("     -roll rollover record count, start a new file every X records (default 20M)\n");
        sb.append("     -wj   write json formatted output (default false)\n");
        sb.append("     -c    use compiled objects instead of generic records (default false, generic) \n");
        sb.append("     -wb   write binary avro format (default true)\n");
        sb.append("     -ofp  String filename prefix of the output file to write to\n");
        sb.append("     -ofs  String filename suffix of the output file to write to\n");
        sb.append("     -dir  String directory to write files to (default .)\n");
        return sb.toString();
    }

    private File createOutputFile(String oFileBase, String extension) {
        File outputFile = new File(outputDir, oFileBase + "." + extension);
        if (outputFile.exists()) {
            logger.info(outputFile+" exists, overwriting");
        } else {
            try {
                boolean retval = outputFile.createNewFile();       
                if (!retval) {
                    logger.fatal("Unable to create file at "+outputFile.getAbsolutePath());
                    logger.fatal(usage());
                    System.exit(-1);
                }
            } catch (Exception ex) {
                logger.fatal("Attempted to create "+outputFile.getAbsolutePath());
                ex.printStackTrace();
            }
        }
        return outputFile;
    }

    public void createOutputFiles() {
        StringBuffer oFileBase = new StringBuffer();
        if (outputFilePrefix == null) {
            boolean first = true;
            if (consumeFromFile != null) {
                oFileBase.append(consumeFromFile.getName());
            } else {
                for (String topic : topics) {
                    if (!first) {
                        oFileBase.append("-");
                    } else {
                        first = false;
                    }
                    oFileBase.append(topic);
                }
            }
        } else {
            oFileBase.append(outputFilePrefix);
        }
        
        if (outputFileSuffix != null) {
            oFileBase.append("_");
            oFileBase.append(outputFileSuffix);
        }

        if (writeJson) {
            String ext = "json";
            if (curLogIndex > 0) {
                ext = ext + "." + curLogIndex;
            }
            outputFileJson = createOutputFile(oFileBase.toString(), ext);
            logger.info("Writing JSON to "+outputFileJson.getAbsolutePath());
            try {
                jsonFileSerializer = new AvroGenericSerializer<>(consumerSchemaFilename, true, outputFileJson, true);
            } catch (Exception e){
                e.printStackTrace();
                logger.error(e);
            }
        } 
        if (writeBinary) {
            String ext = "bin";
            if (curLogIndex > 0) {
                ext = ext + "." + curLogIndex;
            }
            outputFileBinary = createOutputFile(oFileBase.toString(), ext);
            logger.info("Writing Binary to "+outputFileBinary.getAbsolutePath());
            try {
                binaryFileSerializer = new AvroGenericSerializer<>(consumerSchemaFilename, true, outputFileBinary, false);
            } catch (Exception e){
                e.printStackTrace();
                logger.error(e);
            }
        }
        
        if (outputFileJson == null && outputFileBinary == null) {
            logger.fatal("No output files were created, check parameters");
            System.exit(-1);
        }
    }

    public static void main(String [] args){
        consumerGroupID = "FileConsumer";
        if(!NewCDMConsumer.parseArgs(args, false)) {
            logger.error(usage());
            System.exit(1);
        }
        
        parseAdditionalArgs(args);
        
        
        final FileConsumer tconsumer = new FileConsumer();
        //start the consumer
        tconsumer.start();
        
        // add a shutdown hook to clean up the consumer
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                try {
                    logger.info("Shutting down consumer");
                    tconsumer.setShutdown();
                    tconsumer.join();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
    
    public String getConfig() {  
        char separator = '=';
        char fseparator = ',';
        StringBuffer cfg = new StringBuffer(super.getConfig());
        cfg.append("rolloverCount").append(separator).append(rolloverRecordCount).append(fseparator);
        cfg.append("writeJson").append(separator).append(writeJson).append(fseparator);
        cfg.append("writeAvro").append(separator).append(writeBinary).append(fseparator);
        cfg.append("outputFilePrefix").append(separator).append(outputFilePrefix).append(fseparator);
        cfg.append("outputFileSuffix").append(separator).append(outputFileSuffix).append(fseparator);
        cfg.append("outputDir").append(separator).append(outputDir).append(fseparator);
        return cfg.toString();
    }
    
    protected void processCompiledRecord(String key, TCCDMDatum datum) throws Exception {
        super.handleCheckers(key, datum);
        Object record = datum.getDatum();

        String clsName = record.getClass().getCanonicalName();
        if(logger.isDebugEnabled()) logger.debug("Processing CDM"+datum.getCDMVersion()
                + " record of type " + clsName
                + " with key " + key);
        
        if (skipStartKey > 0) {
            try {
                Long sk = Long.parseLong(key);
                if (sk >= skipStartKey && sk < skipEndKey) {
                    logger.info("Skipping duplicate record: "+key);
                    skipping = true;
                    return;
                } else if (skipping && sk >= skipEndKey) {
                    logger.info("End of skip region: "+key);
                    skipStartKey = -1;
                    skipping = false;
                } 
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        if (writeJson) {
            logger.debug("Writing record to JSON file");
            jsonFileSerializer.serializeToFile(datum);
        }
        if (writeBinary) {
            logger.debug("Writing record to Binary file");
            binaryFileSerializer.serializeToFile(datum);
        }  
        checkRollover();
        
    }
    
    boolean skipping = false;
    
    protected void processGenericRecord(String key, GenericContainer record) throws Exception {
        logger.info("CDM generic record: " + record.getClass()+" with key "+key);
        if (skipStartKey > 0) {
            try {
                Long sk = Long.parseLong(key);
                if (sk >= skipStartKey && sk < skipEndKey) {
                    logger.info("Skipping duplicate record: "+key);
                    skipping = true;
                    return;
                } else if (skipping && sk >= skipEndKey) {
                    logger.info("End of skip region");
                    skipping = false;
                    skipStartKey = -1;
                } 
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (writeJson) {
            logger.debug("Writing record to JSON file");
            jsonFileSerializer.serializeToFile(record);
        }
        if (writeBinary) {
            logger.debug("Writing record to Binary file");
            binaryFileSerializer.serializeToFile(record);
        }
        checkRollover();
    }
    
    protected void checkRollover() {
        curRecordCount++;
        if (curRecordCount >= rolloverRecordCount) {
            logger.info("Wrote "+curRecordCount+" records, rolling over the output file(s)");
            if (jsonFileSerializer != null) {
                jsonFileSerializer.close();
            }
            if (binaryFileSerializer != null) {
                binaryFileSerializer.close();
            }
            
            curRecordCount = 0;
            curLogIndex++;
            createOutputFiles();
        }
    }
    
    protected void closeConsumer() {
        super.closeConsumer();
        if (jsonFileSerializer != null) {
            jsonFileSerializer.close();
        }
        if (binaryFileSerializer != null) {
            binaryFileSerializer.close();
        }
    }
}
