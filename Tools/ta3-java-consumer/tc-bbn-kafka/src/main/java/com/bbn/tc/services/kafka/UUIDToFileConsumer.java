package com.bbn.tc.services.kafka;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;

import org.apache.log4j.Logger;

import com.bbn.tc.schema.avro.cdm20.*;

public class UUIDToFileConsumer extends FullTopicConsumer {
    
    public static String UuidFileName = "uuids.txt";
    
    private static final Logger logger = Logger.getLogger(UUIDToFileConsumer.class);
    
    private BufferedWriter bwrite;
    
    private long earliestEventTS = -1l;
    private long latestEventTS = -1l;

    public UUIDToFileConsumer() {
        super();
        File f1 = new File(UuidFileName);
        try {
            bwrite = new BufferedWriter(new FileWriter(f1, false));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
        
        logger.info("Writing UUIDs to "+f1.getAbsolutePath());        
    }
    
    protected void closeConsumer() {
        super.closeConsumer();
        logger.info("Earliest Timestamp: "+earliestEventTS);
        logger.info("Latest Timestamp: "+latestEventTS);
        try {
            bwrite.write("\nEarliest Timestamp: "+earliestEventTS);
            bwrite.write("\nLatest Timestamp: "+latestEventTS);
            bwrite.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    protected void processCompiledRecord(String key, TCCDMDatum datum) throws Exception {
        super.processCompiledRecord(key, datum);
        
        Object record = datum.getDatum();
        try {
            String clsName = record.getClass().getCanonicalName();
            if (record.getClass().equals(Event.class)) {
                Event e1 = (Event)record;
                clsName = e1.getType().name();
            }
            
            UUID uuid = null;
            if(record instanceof Principal) {
                uuid = ((Principal)record).getUuid();
            }
            else if(record instanceof Subject) {
                uuid = ((Subject)record).getUuid();
            }
            else if(record instanceof FileObject) {
                uuid = ((FileObject)record).getUuid();
            }
            else if(record instanceof IpcObject) {
                uuid = ((IpcObject)record).getUuid();
            }
            else if(record instanceof RegistryKeyObject) {
                uuid = ((RegistryKeyObject)record).getUuid();
            }
            else if(record instanceof NetFlowObject) {
                uuid = ((NetFlowObject)record).getUuid();
            }
            else if(record instanceof MemoryObject) {
                uuid = ((MemoryObject)record).getUuid();
            }
            else if(record instanceof SrcSinkObject) {
                uuid = ((SrcSinkObject)record).getUuid();
            } 
            else if(record instanceof Host) {
                uuid = ((Host)record).getUuid();
            } 
            else if(record instanceof Event) {
                uuid = ((Event)record).getUuid();
                long ts = ((Event)record).getTimestampNanos();
                if (earliestEventTS == -1) {
                    earliestEventTS = ts;
                    logger.info("First Event TS: "+earliestEventTS);
                } else if (earliestEventTS > ts) {
                    logger.warn("Gap in timestamps! "+ts+" is earlier than the previous earliest: "+earliestEventTS);
                }
                if (latestEventTS > -1 && latestEventTS > ts) {
                    logger.warn("Gap in timestamps! "+ts+" -> "+latestEventTS);
                }
                latestEventTS = ts;
            } else {
                uuid = new UUID();
            }
            
            int uuidInt = new BigInteger(uuid.bytes()).intValue();
            bwrite.write(clsName+" | "+uuidInt+System.lineSeparator());
             
        } catch (Exception ex) {
            ex.printStackTrace();
        }        
    }
    
  public static void main(String [] args){
        
        java.util.UUID gid = java.util.UUID.randomUUID();
        consumerGroupID = gid.toString();

        if(!NewCDMConsumer.parseArgs(args, false)) {
            logger.error(usage());
            System.exit(1);
        }
        
        parseAdditionalArgs(args);

        UUIDToFileConsumer tconsumer = new UUIDToFileConsumer();
        //start the consumer
        tconsumer.start();
    }
    
  
}
