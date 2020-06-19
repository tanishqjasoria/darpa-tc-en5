/*
 * Copyright (c) 2016 Raytheon BBN Technologies Corp.  All rights reserved.
 */

package com.bbn.tc.schema;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * This test is created to highlight a BUG in Avro!!
 * see inline comments
 *
 * @author jkhoury
 */
public class ByteRecordTest extends BaseTest{
    private static final Logger logger = Logger.getLogger(ByteRecordTest.class);
    private static final String AVSC =
            "{\"type\": \"record\", \"name\": \"testrecord\", "
                    + "\"fields\": ["
                    + "{\"name\":\"byteField\"" +
                    " , \"type\":\"bytes\"" +
                      "}]}";

    DatumWriter<GenericData.Record> writer;
    DatumReader<GenericData.Record> reader;

    @Test
    public void doTest(){

        Schema SCHEMA = new Schema.Parser().parse(AVSC);

        try{

            //create the record
            ByteBuffer buffer = ByteBuffer.wrap("0123456789".getBytes());
            GenericData.Record record = new GenericRecordBuilder(SCHEMA)
                    .set("byteField", buffer)
                    .build();
            /**
             * TODO: this is the BUG in avro!!
             * If you perform a toString on the record before encoding, it corrupts the bytebuffer!!
             * Try running this test with and without the logger.info statement below and see the output
             */
            //logger.info(record.toString());
            ByteBuffer b = (ByteBuffer)record.get("byteField");
            logger.debug(new String(b.array()));

            //serialize the edge
            writer = new GenericDatumWriter<>(SCHEMA);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BinaryEncoder encoder = encoderFactory.directBinaryEncoder(out, null);
            writer.write(record, encoder);
            encoder.flush();
            byte [] bytes = out.toByteArray();
            logger.debug(Arrays.toString(bytes) + ", "+bytes.length);
            out.close();

            // deserialize the edge bytes
            reader = new GenericDatumReader<>(SCHEMA);
            BinaryDecoder decoder = decoderFactory.binaryDecoder(bytes, 0, bytes.length, null);
            GenericData.Record deserializedRecord = reader.read(null, decoder);
            //logger.info(deserializedRecord.toString());
            b = (ByteBuffer)deserializedRecord.get("byteField");
            logger.debug(new String(b.array()));
            assertTrue(b.array() != null && b.array().length == 10);
            assertTrue(record.equals(deserializedRecord));
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e);
            assertTrue(false);
        }

    }
}
