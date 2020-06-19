package com.bbn.tc.services.kafka.checker;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.bbn.tc.schema.avro.cdm20.Event;
import com.bbn.tc.schema.avro.cdm20.TCCDMDatum;

public class StatisticsChecker extends CDMChecker {
    
    private static final Logger logger = Logger.getLogger(StatisticsChecker.class);    
    
    protected HashMap<String, Integer> typeCounts = new HashMap<String, Integer>();
    int totalCount = 0;
    
    public StatisticsChecker() {
    }
    
    @Override
    public String description() {
        return "Count record type statistics";
    }

    @Override
    public void processRecord(String key, TCCDMDatum datum) throws Exception {
        Object record = datum.getDatum();
        totalCount++;
        
        String clsName = record.getClass().getCanonicalName();
        if (record.getClass().equals(Event.class)) {
            Event e1 = (Event)record;
            clsName = e1.getType().name();
        }
                
        Integer val = typeCounts.get(clsName);
        if (val == null) {
            val = new Integer(0);
        }
        typeCounts.put(clsName, val + 1);
    }

    @Override
    public void close() {
        logger.info("Consumed "+totalCount+" records.");

        for (String cls : typeCounts.keySet()) {
            Integer count = typeCounts.get(cls);
            logger.info("Record Counts: "+cls+" -> "+count);
        }
    }
}
