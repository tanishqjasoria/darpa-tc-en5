package com.bbn.tc.services.kafka.checker;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.bbn.tc.schema.avro.cdm20.Host;
import com.bbn.tc.schema.avro.cdm20.TCCDMDatum;
import com.bbn.tc.schema.avro.cdm20.UUID;

public class HostRecordChecker extends CDMChecker {
    private static final Logger logger = Logger.getLogger(HostRecordChecker.class);
    
    int hostRecordIndex = -1;    
    int curIndex = -1;
    String version = null;
    String hostName = null;
    int hostRecordCount = 0;
    
    @Override
    public String description() {
        return "Verify Host record";
    }
    
    public void initialize(Properties properties) {
        curIndex = -1;
    }
    
    @Override
    public void processRecord(String key, TCCDMDatum datum) throws Exception {
        Object cdmRecord = datum.getDatum();
        if (cdmRecord instanceof Host) {
            hostRecordCount++;
            if (hostRecordCount > 1) {
                logger.warn("Found multiple Host records");
            }
        }
        
        if (hostRecordIndex == -1) { // Haven't found the Host record yet
            curIndex++;
            boolean writeRecord = false;
            if (cdmRecord instanceof Host) {
                Host h1 = (Host)cdmRecord;
                hostRecordIndex = curIndex;
                CharSequence ta1Version = h1.getTa1Version();
                if (ta1Version != null) {
                    version = ta1Version.toString();
                    if (version.length() == 0) {
                        logger.warn("Zero length version for Host Record");
                        writeRecord = true;
                    }
                } else {
                    logger.warn("Null TA1 Version for Host Record");
                    writeRecord = true;
                }
                
                CharSequence hname = h1.getHostName();
                if (hname == null) {
                    logger.error("Null HostName for Host record");
                    writeRecord = true;
                } else {
                    hostName = hname.toString();
                    if (hname.length() == 0) {
                        logger.warn("Zero length hostname in Host record");
                        writeRecord = true;
                    } 
                }
                UUID uuid = h1.getUuid();
                if (uuid == null) {
                    logger.error("Null UUID for Host Record");
                    writeRecord = true;
                }       
                
                if (writeRecord) {
                    logger.debug("Record: "+jsonSerializer.serializeToJson(datum, true));
                }
            }
        }
    }
    
    @Override
    public boolean issuesFound() {
        if (curIndex > -1) {
            if (hostRecordIndex == -1) {
                logger.warn("Haven't found a Host record by record # "+curIndex);
                return true;
            } else if (hostRecordIndex > 0) {
                logger.warn("Found Host record, but not first: "+hostRecordIndex);
                return true;
            }
        }
        if (hostRecordCount > 1) {
            logger.warn("Found multiple Host records: "+hostRecordCount);
        }
        return false;
    }

    @Override
    public void close() {
        if (hostRecordIndex == -1) {
            if (curIndex == -1) {
                logger.warn("Found NO records at all");
            } else {
                logger.error("NO Host record found");
            }               
        } else if (hostRecordIndex > 0) {
            logger.warn("Host record found, but was not the first record, it was record # "+hostRecordIndex);
        } else {
            logger.info("Found Host record as the first record");
        }
        logger.info("TA1 Version from Host record: "+version);
        logger.info("HostName from Host record: "+hostName);
        if (hostRecordCount > 1) {
            logger.warn("Found multiple Host records: "+hostRecordCount);
        }
    }    

}
