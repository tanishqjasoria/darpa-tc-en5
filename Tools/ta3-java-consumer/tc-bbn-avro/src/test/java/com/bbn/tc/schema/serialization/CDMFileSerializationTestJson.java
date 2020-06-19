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

import static org.junit.Assert.assertTrue;

/**
 * CDM file serialize to json and deserialize
 * @author jkhoury
 */
public class CDMFileSerializationTestJson {

    private static final Logger logger = Logger.getLogger(CDMFileSerializationTestJson.class);
    final int NUM_RECORDS = 1000;
    Schema schema;
    File testFile;
    AvroGenericSerializer serializer;

    public CDMFileSerializationTestJson(){
        schema = TCCDMDatum.getClassSchema();
        String path = this.getClass().getClassLoader().getResource(".").getPath() + "schemas/test/";
        testFile = new File(path + "testRecordsAvro.json");
        try {

            serializer = new AvroGenericSerializer(schema, true, testFile, true);

        } catch (Exception e) {
            e.printStackTrace();
            assert(false);
        }
    }

    @Test
    public void testCDMFileSerialization() throws Exception {
        try{

            GenericContainer record, drecord;
            for(int i=0;i<NUM_RECORDS; i++){
                record = RecordGenerator.randomEdgeRecord(schema, 1, true);
                if(logger.isDebugEnabled()) logger.debug("created random record: "+record.toString());
                serializer.serializeToFile(record);
                if(logger.isDebugEnabled()) logger.debug("serialized to file");
            }
            serializer.close();

        }catch (Exception e){
            e.printStackTrace();
            assertTrue(false);
        }finally {
            if(testFile != null)
                try {
                    //testFile.delete();
                }catch (Exception e){}
        }
    }

}
