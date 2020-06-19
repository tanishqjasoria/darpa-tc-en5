/*
 * Copyright (c) 2016 Raytheon BBN Technologies Corp.  All rights reserved.
 */

package com.bbn.tc.schema;

import com.bbn.tc.schema.utils.RecordGenerator;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * Test the Generic record vs Specific record
 *
 * @author jkhoury
 */
public class GenericSerDeTest extends BaseTest {

    private static final Logger logger = Logger.getLogger(GenericSerDeTest.class);

    DatumWriter<GenericData.Record> writer;
    DatumReader<GenericData.Record> reader;

    @Test
    public void doTest(){

        ByteArrayOutputStream out = null;
        try{

            initialize();

            GenericData.Record edge = (GenericData.Record)RecordGenerator.randomEdgeRecord(readerSchema, 1, false);
            logger.debug(edge);
            assertTrue(edge instanceof GenericRecord);

            //serialize the edge
            writer = new GenericDatumWriter<>(readerSchema);
            out = new ByteArrayOutputStream();
            BinaryEncoder encoder = encoderFactory.directBinaryEncoder(out, null);
            writer.write(edge, encoder);
            encoder.flush();
            byte [] bytes = out.toByteArray();
            logger.debug(Arrays.toString(bytes) + ", "+bytes.length);
            out.close();

            // deserialize the edge bytes
            reader = new GenericDatumReader<>(readerSchema);
            BinaryDecoder decoder = decoderFactory.binaryDecoder(bytes, 0, bytes.length, null);
            GenericData.Record deserializedRecord = reader.read(null, decoder);
            logger.debug(deserializedRecord.toString());

            assertTrue(edge.equals(deserializedRecord));
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e);
            assertTrue(false);
        }

    }

}
