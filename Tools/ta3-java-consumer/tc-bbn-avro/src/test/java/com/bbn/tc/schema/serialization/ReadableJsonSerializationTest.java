/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

import com.bbn.tc.schema.avro.cdm20.Event;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * @author jkhoury
 */
public class ReadableJsonSerializationTest{
    private static final Logger logger = Logger.getLogger(ReadableJsonSerializationTest.class);
    private ReadableJsonEncoder jsonEncoder;
    private ReadableJsonDecoder jsonDecoder;
    private DatumWriter<Object> datumWriter;
    private DatumReader<GenericContainer> datumReader;
    private ByteArrayOutputStream out;
    Schema schema;

    @Test
    public void doJsonTest(){
        try {
            Event event = TestUtils.generateEvent();
            schema = event.getSchema();

            init(schema);

            datumWriter.write(event, jsonEncoder);
            jsonEncoder.flush();
            String strEdgeJson = new String(out.toByteArray());
            logger.debug("toString: " + event.toString());
            logger.debug("Json String: " + strEdgeJson);


            jsonDecoder = new ReadableJsonDecoder(schema, strEdgeJson);
            //jsonDecoder = (new DecoderFactory()).jsonDecoder(schema, strEdgeJson);
            Event deserializedEvent = (Event)datumReader.read(null, jsonDecoder);
            logger.debug("deserialized edge toString: " + deserializedEvent.toString());
            assertTrue(deserializedEvent!= null && deserializedEvent.toString().equals(event.toString()));
            assertTrue(Arrays.equals(deserializedEvent.getUuid().bytes() , event.getUuid().bytes()));
            assertTrue(Arrays.equals(deserializedEvent.getUuid().bytes() , event.getUuid().bytes()));
        } catch (Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }

    protected void init(Schema schema) throws Exception{
        datumWriter = new SpecificDatumWriter<>(schema);
        out = new ByteArrayOutputStream();
        //jsonEncoder = EncoderFactory.get().jsonEncoder(schema, out, true);
        jsonEncoder = new ReadableJsonEncoder(schema, out, true);
        datumReader = new SpecificDatumReader<>(schema, schema);
    }
}
