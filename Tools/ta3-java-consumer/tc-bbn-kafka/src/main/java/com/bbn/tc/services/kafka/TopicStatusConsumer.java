/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.services.kafka;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.avro.generic.GenericContainer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.log4j.Logger;

import com.bbn.tc.schema.avro.cdm20.Event;
import com.bbn.tc.schema.avro.cdm20.TCCDMDatum;
import com.bbn.tc.services.kafka.checker.TimestampCheck;

import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.MetricsServlet;

/**
 * Simple consumer that consumes the last CDM record on each topic and extracts the timestamp
 */

public class TopicStatusConsumer extends NewCDMConsumer {
	
    // Move shared stuff to NewCDMConsumer
    
    private static final Logger logger = Logger.getLogger(TopicStatusConsumer.class);
       
    protected static int period_sec = 300; // 5 minutes
    protected static String outputFile = "/tmp/TopicStatus.txt";
    SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS");

    Gauge topicDelayMetrics;
       
    public TopicStatusConsumer(String kafkaServer, String groupId,
			String topics, int duration, String consumerSchemaFilename,
			String producerSchemaFilename, int period) {
    	
    	super(kafkaServer, groupId, topics, duration, false, true,
    			consumerSchemaFilename, producerSchemaFilename, "latest", "-1", false, -1, -1, null);
    	TopicStatusConsumer.defaultDuration = duration;
    	TopicStatusConsumer.period_sec = period;
    }
    
    public TopicStatusConsumer() {
        this(kafkaServer, consumerGroupID, topicStr, defaultDuration,
             consumerSchemaFilename, producerSchemaFilename, period_sec);
    }
    
    @Override
    public void run() {
    	logger.info("Started TopicStatusConsumer");

    	ConsumerRecords<String, GenericContainer> records = null;

    	long initDuration = duration;
    	long endTime = (duration <= 0) ? Long.MAX_VALUE : System.currentTimeMillis() + initDuration * 1000;

    	logger.info("Stopping at "+endTime);
    	String lineSep = System.getProperty("line.separator");

    	// long startTime = System.currentTimeMillis();

    	Map<TopicPartition, Long> lastOffset = new HashMap<TopicPartition, Long>();
    	Map<TopicPartition, Long> lastTime = new HashMap<TopicPartition, Long>();

	topicDelayMetrics = Gauge.build().name("publishing_delay").help("Publishing delta between wallclock and CDM timestamp")
	    .labelNames("topic","partition").register();

	
	boolean ignoreFirst = true;

    	try{
	    while (!shutdown.get() && System.currentTimeMillis() <= endTime) {
		
		// Set all offsets to the latest
		Set<TopicPartition> consumer_assignment = consumer.assignment();
		
		long nowTime = System.currentTimeMillis();
                String dateStr = sdf.format(new Date(nowTime));
		
                consumer.seekToEnd(consumer_assignment);
		records = consumer.poll(1);
		
		// Set all offsets to the latest
		consumer_assignment = consumer.assignment();
		logger.info("Assigned topics2: "+consumer_assignment.size());
		int topicCounts = 0;

		for (TopicPartition tPart : consumer_assignment) {
		    logger.info("Assigned topic2: "+tPart);
		    topicCounts++;
		}
		
		HashMap<String, Long> topicDelays = new HashMap<String, Long>();
		long curTime = System.currentTimeMillis();
		
		// Give up after 30 seconds
		while (topicDelays.size() < topicCounts && ((curTime - nowTime) < 30000)) {
		    records = consumer.poll(1000);
		    logger.info("Consumed "+records.count()+" records, checking delays for "+(topicCounts - topicDelays.size())+" more topics");


		    if (ignoreFirst) {
			logger.info("Ignoring the first iteration");
			ignoreFirst = false;
			break;
		    }
		    
		    if (records.count() == 0) {
			logger.warn("No records available on any topic!"); 
		    } else {
			for (ConsumerRecord<String, GenericContainer> record : records) {
			    String topic = record.topic();
			    int partition = record.partition();
			    long offset = record.offset();
			    
			    logger.info("Consumed record # "+offset+" from "+topic+":"+partition);
			    String key = topic+":"+partition;
			    if (topicDelays.containsKey(key)) {
				continue;
			    }
			    
			    GenericContainer datum = record.value();
			    if (datum instanceof TCCDMDatum) {
				Object cdmRecord = ((TCCDMDatum)datum).getDatum();
				if (cdmRecord instanceof Event) {
				    Event event = (Event)cdmRecord;
				    long timeStampNanos = event.getTimestampNanos() != null ? event.getTimestampNanos() : -1;
				    long timeStampMs = convertToMs(timeStampNanos);
				    logger.info("Topic: "+topic+" Wall Clock Time: "+dateStr+" CDM time: "+sdf.format(new Date(timeStampMs)));
				    long delay = nowTime - timeStampMs;
				    topicDelays.put(key, delay);
				    
				    Gauge.Child topicGauge = topicDelayMetrics.labels(topic, ""+partition);
				    topicGauge.set(delay);
				    
				    if (topicDelays.size() >= topicCounts) {
					logger.info("Got the current delay for all topics ("+topicCounts+")");
					break;
				    }				    
				} else {
				    logger.info("Record is not an Event: "+cdmRecord.getClass().getCanonicalName());
				}
			    } else {
				logger.info("Record is not a TCCDMDatum: "+datum.getClass().getCanonicalName());
			    }
			}
		    }
		    curTime = System.currentTimeMillis();
		}
		for (String k : topicDelays.keySet()) {
		    logger.info(k+" publishing delay is "+topicDelays.get(k));
		}
		logger.debug("Sleeping for "+period_sec+" seconds");
		Thread.sleep(period_sec * 1000);    			
	    }
	    
	    logger.info("Duration "+duration+" (ms) elapsed. Exiting");
	    closeConsumer();
	    logger.info("Done.");
	    
    	} catch(Exception e){
	    logger.error("Error while consuming", e);
	    e.printStackTrace();
    	}
    }
    
    public long convertToMs(long timestamp) {
        long tsConversionFactor = TimestampCheck.computeTimestampConversionFactor(timestamp);
        return timestamp * tsConversionFactor / 1000000;
    }
    
    public static boolean parseAdditionalArgs(String[] args) {
        int index = 0;
        while(index < args.length){
            String option = args[index].trim();
            if(option.equals("-p")){
                index++;
                String periodStr = args[index];
                try {
                    period_sec = Integer.parseInt(periodStr);
                } catch (NumberFormatException ex) {
                    System.err.println("Bad period parameter, expecting an int (seconds)");
                    return false;
                } 
            } else if(option.equals("-of")){
                index++;
                outputFile = args[index];
            }
            index++;
        } 
        return true;
    }
    
    public static String usage() {
        StringBuffer sb = new StringBuffer(NewCDMConsumer.usage());
        sb.append("     -p    period (seconds), how often to check the last published record \n");
        sb.append("     -of   String filename, output file to write to\n");
        return sb.toString();
    }

    public static void main(String [] args){
        consumerGroupID = "TopicStatusConsumer";
    	if(!NewCDMConsumer.parseArgs(args, false)) {
    		logger.error(usage());
    		System.exit(1);
    	}
    	
    	parseAdditionalArgs(args);

    	TopicStatusConsumer tconsumer = new TopicStatusConsumer();
    	//start the consumer
    	tconsumer.start();
    }
    
    public String getConfig() {  
        char separator = '=';
        char fseparator = ',';
        StringBuffer cfg = new StringBuffer(super.getConfig());
        cfg.append("period").append(separator).append(period_sec).append(fseparator);
        cfg.append("outputFile").append(separator).append(outputFile).append(fseparator);
        return cfg.toString();
    }




}
