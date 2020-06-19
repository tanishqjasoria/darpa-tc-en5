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
import org.apache.avro.specific.SpecificRecord;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * Create an edge using compiled classes
 * serialize and deserialize using schema
 *
 * @author jkhoury
 */
public class CompiledSerDeClassTest extends BaseTest {

    private static final Logger logger = Logger.getLogger(CompiledSerDeClassTest.class);

    protected DatumWriter<Object> writer;
    protected DatumReader<Object> reader;

    @Test
    public void doTest(){
        ByteArrayOutputStream out=null;
        try{
            initialize();

            //create edge
            LabeledEdge edge = (LabeledEdge)RecordGenerator.randomEdgeRecord(readerSchema, 1, true);
            logger.debug(edge.toString());
            assertTrue(readerSchema.getFullName().equals(edge.getSchema().getFullName()));
            // verify it is a specific record
            assertTrue(edge instanceof SpecificRecord);

            //serialize the edge
            writer = new SpecificDatumWriter<Object>(readerSchema);
            out = new ByteArrayOutputStream();
            BinaryEncoder encoder = encoderFactory.directBinaryEncoder(out, null);
            writer.write(edge, encoder);
            encoder.flush();
            byte [] bytes = out.toByteArray();
            logger.debug(Arrays.toString(bytes) + ", "+bytes.length);
            out.close();

            // deserialize the edge bytes
            reader = new SpecificDatumReader<Object>(readerSchema);
            BinaryDecoder decoder = decoderFactory.binaryDecoder(bytes, 0, bytes.length, null);
            LabeledEdge deserializedEdge = (LabeledEdge)reader.read(null, decoder);
            logger.debug(deserializedEdge.toString());
            assertTrue(deserializedEdge.equals(edge));


        }catch(Exception e){
            e.printStackTrace();
            logger.error(e);
            assertTrue(false);

        }

    }
}
