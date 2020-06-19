/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization.kafka;

import com.bbn.tc.schema.serialization.AvroGenericDeserializer;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericData;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Responsible for deserializing single avro records from kafka <br>
 * Expects the following properties: <br>
 *   <code>com.bbn.tc.schema.writer.file</code> (mandatory) <br>
 *   <code>com.bbn.tc.schema.reader.file</code> (optional, defaults to writer.file) <br>
 *   <code>com.bbn.tc.schema.fullname</code> (mandatory) <br>
 *
 * @see {@link KafkaAvroGenericSerializer}
 * @author jkhoury
 */
public class KafkaAvroGenericDeserializer
        extends AvroGenericDeserializer implements Deserializer<Object>{
    private final Logger logger = Logger.getLogger(this.getClass());

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        try {
            initialize(configs, isKey);
        }catch(Exception e) {
            throw new ConfigException("Failed to configure kafka deserializer", e);
        }
    }

    @Override
    public Object deserialize(String topic, byte[] data) {
        if(data == null) throw new SerializationException("Can not deserialize null bytes");
        try {
            return deserializeBytes(data);
        }catch(Exception e){
            throw new SerializationException("Failed to deserialize object", e);
        }
    }

    @Override
    public void close() {

    }
}
