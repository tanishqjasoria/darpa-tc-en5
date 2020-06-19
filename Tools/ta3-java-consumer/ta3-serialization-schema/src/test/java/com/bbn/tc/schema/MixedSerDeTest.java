/*
 * Copyright (c) 2016 Raytheon BBN Technologies Corp.  All rights reserved.
 */

package com.bbn.tc.schema;

import com.bbn.tc.schema.avro.LabeledEdge;
import com.bbn.tc.schema.utils.RecordGenerator;
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
 * Mix a class writer and an object reader
 * @author jkhoury
 */
public class MixedSerDeTest extends BaseTest {

    private static final Logger logger = Logger.getLogger(MixedSerDeTest.class);

    DatumWriter<LabeledEdge> writer;
    DatumReader<Object> reader;

    @Test
    public void doTest(){
        ByteArrayOutputStream out=null;
        try{
            initialize();

            //create edge
            LabeledEdge edge = (LabeledEdge) RecordGenerator.randomEdgeRecord(readerSchema, 1, true);
            logger.debug(edge.toString());
            assertTrue(readerSchema.getFullName().equals(edge.getSchema().getFullName()));

            //serialize the edge using the class
            writer = new SpecificDatumWriter<>(LabeledEdge.class);
            out = new ByteArrayOutputStream();
            BinaryEncoder encoder = encoderFactory.directBinaryEncoder(out, null);
            writer.write(edge, encoder);
            encoder.flush();
            byte [] bytes = out.toByteArray();
            logger.debug(Arrays.toString(bytes) + ", "+bytes.length);
            out.close();

            // deserialize the edge bytes using the schema instead
            reader = new SpecificDatumReader<>(readerSchema);
            BinaryDecoder decoder = decoderFactory.binaryDecoder(bytes, 0, bytes.length, null);
            LabeledEdge deserializedEdge = (LabeledEdge)reader.read(null, decoder);
            logger.debug(deserializedEdge.toString());

            assertTrue(edge.equals(deserializedEdge));
            assertTrue(edge.getLabel().equals(deserializedEdge.getLabel()));
            assertTrue(edge.getFromNode().getLabel().equals(deserializedEdge.getFromNode().getLabel()));

        }catch(Exception e){
            e.printStackTrace();
            logger.error(e);
            assertTrue(false);

        }

    }

}
