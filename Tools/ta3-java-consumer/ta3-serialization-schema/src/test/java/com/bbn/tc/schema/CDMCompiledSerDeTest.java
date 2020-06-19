/*
 * Copyright (c) 2016 Raytheon BBN Technologies Corp.  All rights reserved.
 */

package com.bbn.tc.schema;


import com.bbn.tc.schema.avro.cdm20.TCCDMDatum;
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
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Test the CDM schema using compiled records
 * @author jkhoury
 */
public class CDMCompiledSerDeTest extends BaseTest{

    private static final Logger logger = Logger.getLogger(CDMCompiledSerDeTest.class);

    DatumWriter<TCCDMDatum> writer;
    DatumReader<TCCDMDatum> reader;

    @Test
    public void doTest(){
        genRecords(1, 1000);
    }

    @Test
    public void doTestNoProperties(){
        genRecords(0, 1000);
    }
 
    public void genRecords(int kvpairs, int count) {
        //set the schema to CDM
        readerSchemaFilename = "TCCDMDatum.avsc";

        ByteArrayOutputStream out=null;
        try{
            initialize();

            for(int i=0; i< count; i++) {

                TCCDMDatum tccdmDatum = (TCCDMDatum) RecordGenerator.randomEdgeRecord(readerSchema, kvpairs, true);
                logger.debug("created record " + tccdmDatum.toString());

                //serialize the edge
                writer = new SpecificDatumWriter<>(TCCDMDatum.class);
                out = new ByteArrayOutputStream();
                BinaryEncoder encoder = encoderFactory.directBinaryEncoder(out, null);
                writer.write(tccdmDatum, encoder);
                encoder.flush();
                byte[] bytes = out.toByteArray();
                logger.debug(Arrays.toString(bytes) + ", " + bytes.length);
                out.close();

                // deserialize the edge bytes
                reader = new SpecificDatumReader<>(TCCDMDatum.class);
                BinaryDecoder decoder = decoderFactory.binaryDecoder(bytes, 0, bytes.length, null);
                TCCDMDatum deserializedDatum = reader.read(null, decoder);
                logger.debug("deserialized to " + deserializedDatum.toString());
                Object object = deserializedDatum.getDatum();
                logger.debug(object.getClass().getSimpleName());
                assertTrue(tccdmDatum.equals(deserializedDatum));
            }
        }catch(Exception e){
            e.printStackTrace();
            logger.error(e);
            assertTrue(false);

        }
        
    }

}
