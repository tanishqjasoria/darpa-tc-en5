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
 * Tests the serialization of generic records for CDM schema
 * @author jkhoury
 */
public class CDMGenericSerDeTest extends BaseTest {
    private static final Logger logger = Logger.getLogger(CDMGenericSerDeTest.class);

    DatumWriter<GenericData.Record> writer;
    DatumReader<GenericData.Record> reader;

    @Test
    public void doTest(){
        genRecords(1, 1000);
    }
    
    @Test
    public void doTestNoProperties() {
        genRecords(0, 1000);
    }
    
    public void genRecords(int kvpairs, int count) {

        //set the schema to CDM
        readerSchemaFilename = "TCCDMDatum.avsc";

        ByteArrayOutputStream out = null;
        try{

            // loads the schema
            initialize();

            for(int i=0; i< count; i++) {

                GenericData.Record record = (GenericData.Record) RecordGenerator.randomEdgeRecord(readerSchema, kvpairs, false);
                assertTrue(record instanceof GenericRecord);
                GenericData.Record datumRecord = (GenericData.Record) record.get("datum");
                //if(logger.isDebugEnabled()) logger.debug(datumRecord.toString());

                String datumSchemaName = datumRecord.getSchema().getName();

                //serialize the edge
                writer = new GenericDatumWriter<>(readerSchema);
                out = new ByteArrayOutputStream();
                BinaryEncoder encoder = encoderFactory.directBinaryEncoder(out, null);
                writer.write(record, encoder);
                encoder.flush();
                byte[] bytes = out.toByteArray();
                logger.debug(Arrays.toString(bytes) + ", " + bytes.length);
                out.close();

                // deserialize the edge bytes
                reader = new GenericDatumReader<>(readerSchema);
                BinaryDecoder decoder = decoderFactory.binaryDecoder(bytes, 0, bytes.length, null);
                GenericData.Record deserializedRecord = reader.read(null, decoder);
                GenericData.Record deserializedDatumRecord = (GenericData.Record) deserializedRecord.get("datum");
                /*if(logger.isDebugEnabled())
                    logger.info(deserializedDatumRecord.toString()
                            + ", schema name: " + deserializedRecord.getSchema().getName());
                            */
                assertTrue(datumRecord.getSchema().getName().equals(datumSchemaName));
                assertTrue(deserializedDatumRecord.toString().equals(datumRecord.toString()));
            }

        }catch (Exception e){
            e.printStackTrace();
            logger.error(e);
            assertTrue(false);
        }

    }


}
