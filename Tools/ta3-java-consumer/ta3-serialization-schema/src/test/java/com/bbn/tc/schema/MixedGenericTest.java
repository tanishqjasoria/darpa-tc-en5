/*
 * Copyright (c) 2016 Raytheon BBN Technologies Corp.  All rights reserved.
 */

package com.bbn.tc.schema;

import com.bbn.tc.schema.avro.LabeledEdge;
import com.bbn.tc.schema.utils.RecordGenerator;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * @author jkhoury
 */
public class MixedGenericTest extends BaseTest {

    private static final Logger logger = Logger.getLogger(MixedGenericTest.class);


    /**
     * Test serialization of a generic record and deserialization as an Object using schema instead of class
     */
    @Test
    public void doTest(){
        ByteArrayOutputStream out=null;
        try{
            initialize();

            //create edge (generic)
            GenericData.Record gEdge = (GenericData.Record) RecordGenerator.randomEdgeRecord(readerSchema, 1, false);
            logger.debug("created edge "+gEdge.toString());

            //serialize the generic edge using the generic serializer
            DatumWriter gWriter = new GenericDatumWriter<>(readerSchema);
            out = new ByteArrayOutputStream();
            BinaryEncoder encoder = encoderFactory.directBinaryEncoder(out, null);
            gWriter.write(gEdge, encoder);
            encoder.flush();
            byte [] bytes = out.toByteArray();
            logger.debug(Arrays.toString(bytes) + ", "+bytes.length);
            out.close();

            // deserialize the generic edge bytes to a compiled object
            DatumReader oreader = new SpecificDatumReader<Object>(readerSchema);
            BinaryDecoder decoder = decoderFactory.binaryDecoder(bytes, 0, bytes.length, null);
            Object o = oreader.read(null, decoder);
            logger.debug("o type "+o.getClass().getCanonicalName());
            LabeledEdge deserializedEdge = (LabeledEdge)o;
            logger.debug("Using generic object" + deserializedEdge.toString());

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
