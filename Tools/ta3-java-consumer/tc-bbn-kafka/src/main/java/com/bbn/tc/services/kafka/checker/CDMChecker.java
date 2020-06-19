package com.bbn.tc.services.kafka.checker;

import java.util.Properties;

import org.apache.avro.generic.GenericContainer;
import org.apache.kafka.common.record.TimestampType;

import com.bbn.tc.schema.avro.cdm20.TCCDMDatum;
import com.bbn.tc.schema.serialization.AvroGenericSerializer;

/**
 * Perform some check on a stream of CDM records, receiving one record at a time
 * Write output to the logger
 * @author bbenyo
 *
 */
public abstract class CDMChecker {

    protected AvroGenericSerializer<GenericContainer> jsonSerializer;

    public CDMChecker() {
    }
        
    // Initialize any settable properties
    // The json serializer is for writing full record data to debug logs
    public void initialize(Properties properties, AvroGenericSerializer<GenericContainer> jsonSerializer) {
        this.jsonSerializer = jsonSerializer;
        initialize(properties);
    }
    
    public void initialize(Properties properties) {
    }
    
    // Write a description to a string including describing any properties that can be set
    public abstract String description();
    
    // Handle a new record
    //  Exceptions will be caught by the caller, so the implementation is free to just throw
    public abstract void processRecord(String key, TCCDMDatum record) throws Exception;
      
    // Have any issues been found yet (conditions where whatever the checker is checking for are violated)
    // Write any issues found to the log. 
    public boolean issuesFound() {
        return false;
    }
    
    // No more records are coming, do what you want to finish up
    // Write to the log a summary of what has been found 
    public abstract void close();
}
