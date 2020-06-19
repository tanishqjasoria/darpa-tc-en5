/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization.kafka;

import com.bbn.tc.schema.serialization.AvroGenericSerializer;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericData;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Responsible for serializing single avro records to kafka <br>
 * Expects the following properties: <br>
 *   <code>com.bbn.tc.schema.writer.file</code> <br>
 *   <code>com.bbn.tc.schema.fullname</code> <br>
 *
 * TODO: implement a schema registry where each record contains
 *   an index of its schema that is looked up from some cache
 *
 * Created by jkhoury
 */
public class KafkaAvroGenericSerializer<T extends GenericContainer>
        extends AvroGenericSerializer implements Serializer<T>{

    private final Logger logger = Logger.getLogger(this.getClass());

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        try {
            initialize(configs, isKey);
        }catch(Exception e) {
            throw new ConfigException("Failed to configure kafka serializer", e);
        }
    }

    @Override
    public byte[] serialize(String topic, T data) {
        if(data == null) throw new SerializationException("Can not serialize null data");
        try {
            return serializeToBytes(data);
        }catch(Exception e){
            throw new SerializationException("Failed to serialize object, "+e.getMessage(), e);
        }
    }

    @Override
    public void close() {

    }
}
