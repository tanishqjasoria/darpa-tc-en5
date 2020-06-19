package com.bbn.tc.services.kafka.checker;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.bbn.tc.schema.avro.cdm20.Event;
import com.bbn.tc.schema.avro.cdm20.TCCDMDatum;

// Verify that the sequence numbers increase by no more than N
// Record any gaps, or cases where sequence numbers decrease
public class SequenceNumberCheck extends CDMChecker {

    private static final Logger logger = Logger.getLogger(SequenceNumberCheck.class);
       
    protected long lastSequence = -1;
    protected int incrementMax = 1;
    
    int gapCount = 0;
    int decreaseCount = 0;
    int dupCount = 0;
    
    public SequenceNumberCheck() {
        lastSequence = -1;
    }
    
    @Override
    public void processRecord(String key, TCCDMDatum record) throws Exception {
        if (record.getDatum() instanceof Event) {
            Event event = (Event)record.getDatum();   
            Long sequence = event.getSequence();
            if (sequence != null) {
                // Ignore a sequence # of 0, that can mean we don't know
                if (lastSequence > 0) {
                    if (lastSequence > sequence) {
                        logger.info("Sequence number decrease: "+lastSequence+" "+sequence);
                        //logger.debug("Record: "+jsonSerializer.serializeToJson(record, true));
                        decreaseCount++;
                    } else if (lastSequence == sequence) {
                        logger.warn("Duplicate sequence number: "+lastSequence);
                        //logger.debug("Record: "+jsonSerializer.serializeToJson(record, true));
                        dupCount++;
                    } else if (sequence > (lastSequence + 1)) {
                        logger.info("Sequence number gap: "+lastSequence+" -> "+sequence);
                        //logger.debug("Record: "+jsonSerializer.serializeToJson(record, true));
                        gapCount++;
                    }
                }
                lastSequence = sequence;            
            }
        }
    }

    @Override
    public void close() {
        if (gapCount > 0) {
            logger.warn("Gaps in sequence numbers found: "+gapCount);
        } else {
            logger.info("NO gaps in sequence numbers found");
        }
        
        if (decreaseCount > 0) {
            logger.warn("Decreases in sequence numbers found: "+decreaseCount);
        } else {
            logger.info("NO decreases in sequence numbers found");
        }
        
        if (dupCount > 0) {
            logger.warn("Back to back duplicate sequence numbers found: "+dupCount);
        } else {
            logger.info("NO back-to-back duplicate sequence numbers found");
        }
        
        if (lastSequence == -1) {
            logger.warn("NO Events with sequence numbers found");
        }

    }

    @Override
    public void initialize(Properties properties) {
        logger.info("Initializing SequenceNumberCheck from properties");
        String incMax = properties.getProperty("SequenceNumberCheck.incrementMax");
        if (incMax != null) {
            try {
                incrementMax = Integer.parseInt(incMax);
                logger.info("SequenceNumberCheck incrementMax = "+incrementMax);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                logger.error(ex.getMessage(), ex);
            }
        }  
        gapCount = 0;
        decreaseCount = 0;
        dupCount = 0;
    }

    @Override
    public String description() {
        StringBuffer sb = new StringBuffer("Check that sequenceNumbers for Events increment with no gap greater than ");
        sb.append(incrementMax);
        sb.append(System.lineSeparator());
        sb.append("\tProperty: SequenceNumberCheck.incrementMax: maximum allowed gap between sequence numbers");
        return sb.toString();
    }

    @Override
    public boolean issuesFound() {
        if (gapCount == 0 && decreaseCount == 0 && dupCount == 0) {
            return false;
        }
        return true;
    }

}
