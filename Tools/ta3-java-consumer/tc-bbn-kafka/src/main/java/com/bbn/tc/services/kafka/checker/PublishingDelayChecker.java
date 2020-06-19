package com.bbn.tc.services.kafka.checker;

import java.util.ArrayList;

import org.apache.kafka.common.record.TimestampType;
import org.apache.log4j.Logger;

import com.bbn.tc.schema.avro.cdm20.Event;
import com.bbn.tc.schema.avro.cdm20.TCCDMDatum;

public class PublishingDelayChecker extends TimestampCDMChecker {
    private static final Logger logger = Logger.getLogger(PublishingDelayChecker.class);
    
    long maximumDelta = 0;
    long lastDelta = 0;
    
    long tsConversionFactor = 0;
    
    long reportingPeriod = 10000; // Report the current delay to the log every X records;
    long recordCount = 0;
    int windowCount = 0;
    
    DelayThreshold policyDemoMax;
    DelayThreshold realtimeMax;
    
    protected class DelayThreshold {
        long delayMs;
        long recordCount;
        String label;
        
        public DelayThreshold(String l, long ms) {
            this.label = l;
            this.delayMs = ms;
            recordCount = -1;
        }
    }
    
    ArrayList<DelayThreshold> thresholds = new ArrayList<DelayThreshold>();
    int lastThresholdHit = -1;
    
    public PublishingDelayChecker() {
        DelayThreshold d10s = new DelayThreshold("10 seconds", 1000 * 10);
        thresholds.add(d10s);
        DelayThreshold d1m = new DelayThreshold("1 minute", 1000 * 60);
        thresholds.add(d1m);
        DelayThreshold d3m = new DelayThreshold("3 minutes", 1000 * 60 * 3);
        thresholds.add(d3m);
        policyDemoMax = d3m;
        DelayThreshold d10m = new DelayThreshold("10 minutes", 1000 * 60 * 10);
        thresholds.add(d10m);
        DelayThreshold d1h = new DelayThreshold("1 hour", 1000 * 60 * 60);
        thresholds.add(d1h);
        realtimeMax = d1h;
    }
        
    public long convertToMs(long timestamp) {
        if (tsConversionFactor == 0) {
            tsConversionFactor = TimestampCheck.computeTimestampConversionFactor(timestamp);
        }
        
        return timestamp * tsConversionFactor / 1000000;
    }     
    
    @Override
    public void processRecordWithKafkaTimestamp(String key, TCCDMDatum record,
            long timestamp, TimestampType tsType) {
        recordCount++;
        windowCount++;
        Object datum = record.getDatum();
        if (datum instanceof Event) {
            Event event = (Event)datum;
            long timeStampNanos = event.getTimestampNanos() != null ? event.getTimestampNanos() : -1;
            long timeStampMs = convertToMs(timeStampNanos);
            // Assuming the kafka ts is in ms
            long delta = (timestamp - timeStampMs);
            lastDelta = delta;
            if (delta > maximumDelta) {
                for (int index = this.lastThresholdHit + 1; index < thresholds.size(); ++index) {
                    DelayThreshold threshold = thresholds.get(index);
                    if (delta > threshold.delayMs) {
                        // first time we've hit this threshold
                        threshold.recordCount = recordCount;
                        logger.warn("Delta hit "+threshold.label+": CDM timestamp (ms): "+timeStampMs+" Kafka timestamp: "+timestamp);
                        logger.warn("Kafka Timestamp type: "+tsType.name());
                        lastThresholdHit = index;
                    }                     
                }
                maximumDelta = delta;
            }
            if (windowCount > reportingPeriod) {
                windowCount = 0;
                logger.info("Current publishing delay after "+recordCount+" records: "+delta);
            }            
        }
    }

    @Override
    public String description() {
        return "Compute publishing delay";
    }

    @Override
    public void processRecord(String key, TCCDMDatum record) throws Exception {
        // We only care about the kafka timestamp version
    }

    // Have any issues been found yet (conditions where whatever the checker is checking for are violated)
    // Write any issues found to the log. 
    public boolean issuesFound() {
        if (lastThresholdHit > -1) {
            for (DelayThreshold threshold : thresholds) {
                if (threshold.recordCount > 0) {
                    logger.warn("Publishing delay hit "+threshold.label+" after "+threshold.recordCount+" records");
                }
            }
            logger.warn("Maximum publishing delay is "+maximumDelta);
            return true;
        }

        logger.info("Maximum publishing delay is "+maximumDelta);
        return false;
    }
    
    @Override
    public void close() {
        issuesFound();
        logger.info("Current publishing delay is: "+lastDelta);
        if (policyDemoMax.recordCount > -1) {
            logger.error("Delay of "+maximumDelta+" is higher than the policy demo maxiumum threshold of 3 minutes");
        }
        if (realtimeMax.recordCount > -1) {
            logger.error("Delay of "+maximumDelta+" is higher than the real time max threshold of 1 hour");
        }        
    }

}
