/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Test compiled record serialization using schema TCCDMDatum11.avsc.
 * This is to verify that regardless of what schema we use, the results
 * should be the same (whether we use the class schema or the avsc file)
 * @author jkhoury
 */
public class CDMFileSerializationTest2 extends CDMFileSerializationTest {

    private static final Logger logger = Logger.getLogger(CDMFileSerializationTest2.class);
    final String schemaFilename = "TCCDMDatum13.avsc";
    File schemaFile;

    public CDMFileSerializationTest2(){
        String path = this.getClass().getClassLoader().getResource(".").getPath() + "schemas/test/";
        testFile = new File(path + "testRecords2.avro");
        try {
            schemaFile = new File(path + schemaFilename);
            serializer = new AvroGenericSerializer(schemaFile.getAbsolutePath(), true, testFile);
            deserializer = new AvroGenericDeserializer(schemaFile.getAbsolutePath(), schemaFile.getAbsolutePath(),
                    true, testFile);
            schema = Utils.loadSchema(schemaFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
