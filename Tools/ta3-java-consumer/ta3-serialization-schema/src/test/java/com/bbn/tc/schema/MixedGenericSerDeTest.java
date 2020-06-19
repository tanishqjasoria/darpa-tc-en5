/*
 * Copyright (c) 2016 Raytheon BBN Technologies Corp.  All rights reserved.
 */

package com.bbn.tc.schema;

import com.bbn.tc.schema.avro.LabeledEdge;
import com.bbn.tc.schema.utils.RecordGenerator;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * Test serialization of a generic record and deserialization as an Object
 * Test serialization of a an object      and deserialization as a GenericRecord
 *
 * @author jkhoury
 */
public class MixedGenericSerDeTest extends BaseTest{
    private static final Logger logger = Logger.getLogger(MixedSerDeTest.class);

    DatumWriter<LabeledEdge> writer;
    DatumWriter<GenericData.Record> gWriter;
    DatumReader<LabeledEdge> reader;
    DatumReader<GenericData.Record> gReader;

    /**
     * Test serialization of a an object      and deserialization as a GenericRecord
     */
    @Test
    public void doTest(){
        ByteArrayOutputStream out=null;
        try{
            initialize();

            //create edge (generic and compiled)
            LabeledEdge edge = (LabeledEdge) RecordGenerator.randomEdgeRecord(readerSchema, 1, true);

            //serialize the class edge using the class
            writer = new SpecificDatumWriter<>(LabeledEdge.class);
            out = new ByteArrayOutputStream();
            BinaryEncoder encoder = encoderFactory.directBinaryEncoder(out, null);
            writer.write(edge, encoder);
            encoder.flush();
            byte [] bytes = out.toByteArray();
            logger.debug(Arrays.toString(bytes) + ", "+bytes.length);
            out.close();

            // deserialize the edge bytes to a generic record
            gReader = new GenericDatumReader<>(readerSchema);
            BinaryDecoder decoder = decoderFactory.binaryDecoder(bytes, 0, bytes.length, null);
            GenericData.Record deserializedRecord = gReader.read(null, decoder);
            logger.debug(deserializedRecord.toString());

            assertTrue(edge.getLabel().toString().equals(deserializedRecord.get("label").toString()));
            assertTrue(edge.getFromNode().getLabel().toString().equals(
                            ((GenericData.Record) deserializedRecord.get("fromNode")).get("label").toString())
            );

        }catch(Exception e){
            e.printStackTrace();
            logger.error(e);
            assertTrue(false);

        }

    }

    /**
     * Test serialization of a generic record and deserialization as an Object
     */
    @Test
    public void doTest2(){
        ByteArrayOutputStream out=null;
        try{
            initialize();

            //create edge (generic)
            GenericData.Record gEdge = (GenericData.Record)RecordGenerator.randomEdgeRecord(readerSchema, 1, false);

            //serialize the generic edge using the generic serializer
            gWriter = new GenericDatumWriter<>(readerSchema);
            out = new ByteArrayOutputStream();
            BinaryEncoder encoder = encoderFactory.directBinaryEncoder(out, null);
            gWriter.write(gEdge, encoder);
            encoder.flush();
            byte [] bytes = out.toByteArray();
            logger.debug(Arrays.toString(bytes) + ", "+bytes.length);
            out.close();

            // deserialize the generic edge bytes to a compiled object
            reader = new SpecificDatumReader<>(LabeledEdge.class);
            BinaryDecoder decoder = decoderFactory.binaryDecoder(bytes, 0, bytes.length, null);
            LabeledEdge deserializedEdge = reader.read(null, decoder);
            logger.debug(deserializedEdge.toString());

            assertTrue(gEdge.toString().equals(deserializedEdge.toString()));
            assertTrue(deserializedEdge.getLabel().toString().equals(gEdge.get("label").toString()));
            assertTrue(deserializedEdge.getFromNode().getLabel().toString().equals(
                            ((GenericData.Record) gEdge.get("fromNode")).get("label").toString())
            );

        }catch(Exception e){
            e.printStackTrace();
            logger.error(e);
            assertTrue(false);

        }

    }

}
