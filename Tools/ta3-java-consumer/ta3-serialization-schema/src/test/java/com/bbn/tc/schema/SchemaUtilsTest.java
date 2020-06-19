/*
 * Copyright (c) 2016 Raytheon BBN Technologies Corp.  All rights reserved.
 */

package com.bbn.tc.schema;

import com.bbn.tc.schema.utils.SchemaUtils;
import org.apache.avro.Schema;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertTrue;

/**
 * Test the schema utils
 * @author jkhoury
 */
public class SchemaUtilsTest extends BaseTest{

    private static final Logger logger = Logger.getLogger(SchemaUtilsTest.class);

    @Test
    public void doTest(){

        //set the schema to CDM
        readerSchemaFilename = "TCCDMDatum.avsc";
        Schema s;
        String name;
        try{
            // loads the schema
            initialize();

            // traverse and find subSchemas by name including caching
            name = "com.bbn.tc.schema.avro.cdm20.Event";
            logger.debug("retrieving " + name);
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name, true)) != null
                    && s.getFullName().equals(name));

            // try event again, should come from cache this time
            name = "com.bbn.tc.schema.avro.cdm20.Event";
            logger.debug("retrieving " + name + " again, this time will come from cache");
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name, true)) != null
                    && s.getFullName().equals(name));

            name = "com.bbn.tc.schema.avro.cdm20.SrcSinkType";
            logger.debug("retrieving " + name);
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name , true)) != null
                    && s.getFullName().equals(name));

            name = "com.bbn.tc.schema.avro.cdm20.FileObject";
            logger.debug("retrieving " + name);
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name , true)) != null
                    && s.getFullName().equals(name));

            name = "com.bbn.tc.schema.avro.cdm20.MemoryObject";
            logger.debug("retrieving " + name);
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name , true)) != null
                    && s.getFullName().equals(name));

            name = "com.bbn.tc.schema.avro.cdm20.NetFlowObject";
            logger.debug("retrieving " + name);
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name , true)) != null
                    && s.getFullName().equals(name));

            name = "com.bbn.tc.schema.avro.cdm20.Subject";
            logger.debug("retrieving " + name);
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name , true)) != null
                    && s.getFullName().equals(name));

            name = "com.bbn.tc.schema.avro.cdm20.Principal";
            logger.debug("retrieving " + name);
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name , true)) != null
                    && s.getFullName().equals(name));

            name = "com.bbn.tc.schema.avro.cdm20.Value";
            logger.debug("retrieving " + name);
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name , true)) != null
                    && s.getFullName().equals(name));

            name = "com.bbn.tc.schema.avro.cdm20.TagOpCode";
            logger.debug("retrieving " +name);
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name, true)) != null
                    && s.getFullName().equals(name));

            name = "com.bbn.tc.schema.avro.cdm20.UUID";
            logger.debug("retrieving " +name);
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name, true)) != null
                    && s.getFullName().equals(name));

            name = "com.bbn.tc.schema.avro.cdm20.ConfidentialityTag";
            logger.debug("retrieving " +name);
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name, true)) != null
                    && s.getFullName().equals(name));

            name = "com.bbn.tc.schema.avro.cdm20.NONEXISTING";
            logger.debug("retrieving non-existent object, will return null");
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name, true)) == null);

            SchemaUtils.clearCache();
            //now try to retrieve record again, this will repopulate the cache
            name = "com.bbn.tc.schema.avro.cdm20.TagOpCode";
            logger.debug("retrieving " +name + " again after clearing cache");
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name, true)) != null
                    && s.getFullName().equals(name));

            // now try without caching
            name = "com.bbn.tc.schema.avro.cdm20.TagOpCode";
            logger.debug("retrieving " +name+" no caching");
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name, false)) != null
                    && s.getFullName().equals(name));

        } catch (Exception e){
            e.printStackTrace();
            logger.error(e);
            assertTrue(false);
        }
    }

    /**
     * This is a schema with a single record
     */
    //@Test
    public void doTest2(){

        //set the schema to CDM
        readerSchemaFilename = "LabeledEdge.avsc";
        Schema s;
        String name;
        try{
            // loads the schema
            initialize();

            // traverse and find subSchemas by name including caching
            name = "com.bbn.tc.schema.avro.LabeledNode";
            logger.debug("retrieving " + name);
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name, true)) != null
                    && s.getFullName().equals(name));

            // try event again, should come from cache this time
            name = "com.bbn.tc.schema.avro.LabeledEdge";
            logger.debug("retrieving " + name);
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name, true)) != null
                    && s.getFullName().equals(name));

            name = "com.bbn.tc.schema.avro.NONEXISTING";
            logger.debug("retrieving non-existent object, will return null");
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name, true)) == null);


        }catch (Exception e){
            e.printStackTrace();
            logger.error(e);
            assertTrue(false);
        }
    }

    /**
     * This is a schema with a top level union
     */
    @Test
    public void doTest3(){

        //set the schema to CDM
        readerSchemaFilename = "LabeledGraph.avsc";
        Schema s;
        String name;
        try{
            // loads the schema
            initialize();

            // traverse and find subSchemas by name including caching
            name = "com.bbn.tc.schema.avro.LabeledNode";
            logger.debug("retrieving " + name);
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name, true)) != null
                    && s.getFullName().equals(name));

            // try event again, should come from cache this time
            name = "com.bbn.tc.schema.avro.LabeledEdge";
            logger.debug("retrieving " + name);
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name, true)) != null
                    && s.getFullName().equals(name));

            name = "com.bbn.tc.schema.avro.NONEXISTING";
            logger.debug("retrieving non-existent object, will return null");
            assertTrue((s = SchemaUtils.getTypeSchemaByName(readerSchema, name, true)) == null);


        }catch (Exception e){
            e.printStackTrace();
            logger.error(e);
            assertTrue(false);
        }
    }



}
