/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.services.kafka;

import com.bbn.tc.schema.serialization.kafka.KafkaAvroGenericDeserializer;
import com.bbn.tc.schema.serialization.kafka.KafkaAvroGenericSerializer;
import org.apache.avro.Schema;

import java.util.Map;

/**
 * @author jkhoury
 */
public class BaseTest {

    protected static final String schemaFileName = "LabeledEdge.avsc";
    protected Schema schema;
    protected KafkaAvroGenericSerializer serializer;
    protected KafkaAvroGenericDeserializer deserializer;

    public void initialize(Map props){
        serializer = new KafkaAvroGenericSerializer();
        serializer.configure(props, false);

        deserializer = new KafkaAvroGenericDeserializer();
        deserializer.configure(props, false);

    }
}
