/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

import com.bbn.tc.schema.utils.SchemaUtils;
import org.apache.avro.Schema;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Test a complex schema with nested records
 * @author jkhoury
 */
public class NestedSchemaTest {
    private static final Logger logger = Logger.getLogger(NestedSchemaTest.class);
    public static final String schemaFilename = "schemas/test/LabeledEdge.avsc";
    public static final String unionSchemaFilename = "schemas/test/LabeledGraph.avsc";
    private Schema schema, unionSchema;

    @Test
    public void doTest(){

        try {

            loadSchemas();

            for(Schema.Field f:schema.getFields()) {
                logger.debug(f.name() + " - " + f.schema().getType() + " - "
                        + f.schema().toString(true));
            }

            assertTrue(schema.getField("fromNode").schema().getName().equals("LabeledNode"));
            assertTrue(schema.getField("fromNode").schema().equals(schema.getField("toNode").schema()));

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }

    }

    @Test
    public void doSchemaTest(){
        try{
            loadSchemas();
            Schema tmpSchema  = SchemaUtils.getTypeSchemaByName(schema, TestUtils.NODE_SCHEMA_FULLNAME, true);
            logger.debug("tmpSchema "+tmpSchema.toString(true));
            Schema tmpUSchema = SchemaUtils.getTypeSchemaByName(unionSchema, TestUtils.NODE_SCHEMA_FULLNAME, true);
            logger.debug("tmpUSchema " + tmpUSchema.toString(true));
            assertTrue(tmpSchema.equals(tmpUSchema));

            tmpSchema  = SchemaUtils.getTypeSchemaByName(schema, TestUtils.EDGE_SCHEMA_FULLNAME, true);
            logger.debug("edgetmpSchema "+tmpSchema.toString(true));
            tmpUSchema = SchemaUtils.getTypeSchemaByName(unionSchema, TestUtils.EDGE_SCHEMA_FULLNAME, true);
            logger.debug("edgetmpUSchema " + tmpUSchema.toString(true));
            assertTrue(tmpSchema.equals(tmpUSchema));
            tmpSchema  = SchemaUtils.getTypeSchemaByName(schema, "nonexisting_schema_name", true);
            assertTrue(tmpSchema == null);

        }catch(Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }

    private void loadSchemas() throws Exception{
        schema = Utils.loadSchema(
                new File(this.
                        getClass().
                        getClassLoader().
                        getResource(schemaFilename).
                        getFile()));

        unionSchema = Utils.loadSchema(
                new File(this.
                        getClass().
                        getClassLoader().
                        getResource(unionSchemaFilename).
                        getFile()));
    }
}
