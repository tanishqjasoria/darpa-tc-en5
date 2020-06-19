/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

import com.bbn.tc.schema.avro.cdm20.TCCDMDatum;
import com.bbn.tc.schema.utils.RecordGenerator;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericContainer;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Test compiled record serialization using schema TCCDMDatum.getClassSchema()
 * @author jkhoury
 */
public class CDMFileSerializationTest{
    private static final Logger logger = Logger.getLogger(CDMFileSerializationTest.class);
    private static final int NUM_RECORDS = 1000;

    AvroGenericSerializer serializer;
    AvroGenericDeserializer deserializer;

    File testFile;
    Schema schema;

    public CDMFileSerializationTest(){
        String path = this.getClass().getClassLoader().getResource(".").getPath();
        testFile = new File(path + "testRecords.avro");
        schema = TCCDMDatum.getClassSchema();
        init(schema, testFile);
    }

    @Test
    public void testCDMFileSerialization() throws Exception {
        try{

            GenericContainer record, drecord;
            for(int i=0;i<NUM_RECORDS; i++){
                record = RecordGenerator.randomEdgeRecord(getSchema(), 1, true);
                logger.debug("created random record: "+record.toString());
                serializer.serializeToFile(record);
                logger.debug("serialized to file");
            }
            serializer.close();


            for(int i=0;i<NUM_RECORDS; i++) {
                //deserialize
                drecord = deserializer.deserializeNextRecordFromFile();
                logger.debug("deserialized record: " + drecord.toString());
            }
            deserializer.close();


        }catch (Exception e){
            e.printStackTrace();
            assertTrue(false);
        }finally {
            if(testFile != null)
                try {
                    testFile.delete();
                }catch (Exception e){}
        }
    }

    @Test
    public void testCDMFileSerialization2() throws Exception {
        try{

            List<GenericContainer> records, drecords;
            records = new ArrayList<>();
            for(int i=0;i<NUM_RECORDS; i++){
                records.add(i, RecordGenerator.randomEdgeRecord(getSchema(), 1, true));
                logger.debug("added random record: "+records.get(i));
            }
            serializer.serializeToFile(records);
            logger.debug(NUM_RECORDS + " records serialized to file ");
            serializer.close();

            drecords = new ArrayList<>();
            drecords = deserializer.deserializeNRecordsFromFile(NUM_RECORDS);
            for(int i=0;i<NUM_RECORDS; i++) {
                logger.debug("deserialized record: " + drecords.get(i).toString());
            }
            deserializer.close();

        }catch (Exception e){
            e.printStackTrace();
            assertTrue(false);
        }finally {
            if(testFile != null)
                try {
                    testFile.delete();
                }catch (Exception e){}
        }
    }

    protected Schema getSchema(){
        return schema;

    }

    protected void init(Schema schema, File outputFile){
        serializer = new AvroGenericSerializer(schema, true, outputFile);
        deserializer = new AvroGenericDeserializer(schema, schema, true, outputFile);
    }
}
