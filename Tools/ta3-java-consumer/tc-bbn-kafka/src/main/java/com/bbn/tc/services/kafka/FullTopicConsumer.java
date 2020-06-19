/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.services.kafka;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.apache.avro.generic.GenericContainer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.log4j.Logger;

import com.bbn.tc.schema.avro.cdm20.Event;
import com.bbn.tc.schema.avro.cdm20.TCCDMDatum;

/**
 * Simple consumer that consumes the last CDM record on each topic and extracts the timestamp
 */

public class FullTopicConsumer extends NewCDMConsumer {
	
    private static final Logger logger = Logger.getLogger(FullTopicConsumer.class);
      
    protected static String outputFile = "/tmp/FullTopicStats.txt";
    
    SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
       
    protected HashMap<String, Integer> typeCounts = new HashMap<String, Integer>();
    
    public FullTopicConsumer(String kafkaServer, String groupId,
			String topics, String consumerSchemaFilename,
			String producerSchemaFilename) {
    	super(kafkaServer, groupId, topics, 0, false, true,
    			consumerSchemaFilename, producerSchemaFilename, "earliest", null, false, maxRecords, -1, null);
    	FullTopicConsumer.topicStr = topics;
    }
    
    public FullTopicConsumer() {
        this(kafkaServer, consumerGroupID, topicStr,
             consumerSchemaFilename, producerSchemaFilename);
    }
    
    @Override
    public void run() {
    	logger.info("Started FullTopicConsumer");

    	ConsumerRecords<String, GenericContainer> records = null;

    	long initDuration = duration;
    	long finalEndTime = (duration <= 0) ? Long.MAX_VALUE : System.currentTimeMillis() + initDuration * 1000;

    	long startTime = System.currentTimeMillis();
    	
    	logger.info("Starting to consume records from "+topicStr);
    	
    	int totalCount = 0;    	
    	long lastRecordTime = startTime;
    	
    	try{
    		while (!shutdown.get() && System.currentTimeMillis() <= finalEndTime) {
    			
    			records = consumer.poll(100);
    			if (records.count() == 0) {
    				long endTime = System.currentTimeMillis();
    	    		long diff = endTime - lastRecordTime;
    	    		if (diff > 10000) {
    	    			logger.warn("No records available in the last 10 seconds");
    	    			shutdown.set(true);
    	    		}
    			} else {
    				lastRecordTime = System.currentTimeMillis();
    				for (ConsumerRecord<String, GenericContainer> record : records) {
    					totalCount++;
    					if (logger.isDebugEnabled()) {
    						logger.debug("Consumed record "+totalCount+" at offset: "+record.offset());
    					}
    					processRecord(record.key(), record.value());
    					if (Instrumented && InstrumentedKafkaConsumer.extraInstrumentation) {
                            ((InstrumentedKafkaConsumer<String, GenericContainer>)consumer).
                            recordCDMMetrics(record);
                        }
    				}
    			}
    		}

    		long endTime = System.currentTimeMillis();
    		long diff = endTime - startTime;
    		logger.info("Duration "+diff+" (ms) elapsed. Exiting");
    		closeConsumer();
    		
    		logger.info("Consumed "+totalCount+" records.");

    		for (String cls : typeCounts.keySet()) {
    			Integer count = typeCounts.get(cls);
    			logger.info(cls+": "+count);
    		}
    		
    		BufferedWriter bwrite = new BufferedWriter(new FileWriter(new File(outputFile)));
    		bwrite.write("Topic: "+topicStr+System.lineSeparator());
    		bwrite.write("Date: "+sdf.format(new Date())+System.lineSeparator());
    		bwrite.write("Records: "+totalCount+System.lineSeparator());
    		for (String cls : typeCounts.keySet()) {
    			Integer count = typeCounts.get(cls);
    			bwrite.write(cls+": "+count+System.lineSeparator());
    		}
    		bwrite.close();
    		
    	} catch(Exception e){
    		logger.error("Error while consuming", e);
    		e.printStackTrace();
    	}
    }
    
    protected void processCompiledRecord(String key, TCCDMDatum datum)throws Exception {
        Object record = datum.getDatum();
        handleCheckers(key, datum);
        
    	String clsName = record.getClass().getCanonicalName();
    	if (record.getClass().equals(Event.class)) {
    	    Event e1 = (Event)record;
    	    clsName = e1.getType().name();
    	}
    	
        if(logger.isDebugEnabled()) logger.debug("Processing CDM"+datum.getCDMVersion()
                + " record of type " + clsName
                + " with key " + key);
        
        Integer val = typeCounts.get(clsName);
    	if (val == null) {
    		val = new Integer(0);
    	}
    	typeCounts.put(clsName, val + 1);
    }
  
    public static String usage() {
        StringBuffer sb = new StringBuffer(NewCDMConsumer.usage());
        sb.append("     -p    period (seconds), how often to check the last published record \n");
        return sb.toString();
    }

    public static void main(String [] args){
        
        UUID gid = UUID.randomUUID();
        consumerGroupID = gid.toString();

        if(!NewCDMConsumer.parseArgs(args, false)) {
            logger.error(usage());
            System.exit(1);
        }
        
        parseAdditionalArgs(args);

        FullTopicConsumer tconsumer = new FullTopicConsumer();
        //start the consumer
        tconsumer.start();
    }
  
    /**
     * Parse the argument list, doing some error checking for each specific argument type
     */
    protected static boolean parseAdditionalArgs(String [] args) {   	
        // No additional args for this consumer
        return true;
    }

}
