package com.bbn.tc.services.kafka.checker;

import org.apache.kafka.common.record.TimestampType;

import com.bbn.tc.schema.avro.cdm20.TCCDMDatum;

public abstract class TimestampCDMChecker extends CDMChecker {

    // Handle a record with a Kafka timestamp
    //  If you need the kafka timestamp (timestamp when the record was published to kafka, 
    //   or when the producer created it, see TimestampType),  use this method. 
    //  When we read CDM from a file, and not live from kafka, this method won't get called.
    abstract public void processRecordWithKafkaTimestamp(String key, TCCDMDatum record, long timestamp, TimestampType tsType);
}

