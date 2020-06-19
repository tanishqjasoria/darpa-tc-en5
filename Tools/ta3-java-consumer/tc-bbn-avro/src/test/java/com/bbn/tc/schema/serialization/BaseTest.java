/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

import com.bbn.tc.schema.utils.SchemaUtils;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.util.Utf8;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Base test class
 * @author jkhoury
 */
public abstract class BaseTest {

    private static final Logger logger = Logger.getLogger(BaseTest.class);

    protected String readerSchemaFilename = "schemas/test/LabeledGraph.avsc";
    protected String writerSchemaFilename = "schemas/test/LabeledGraph.avsc";

    protected Schema readerSchema, writerSchema, nodeSchema, edgeSchema;

    protected AvroGenericSerializer<GenericData.Record> serializer;
    protected AvroGenericDeserializer deserializer;

    public BaseTest(){}


    public void setReaderSchemaFilename(String readerSchemaFilename) {
        this.readerSchemaFilename = readerSchemaFilename;
    }

    public void setWriterSchemaFilename(String writerSchemaFilename) {
        this.writerSchemaFilename = writerSchemaFilename;
    }

    protected void initialize() throws Exception{
        // The schemas
        readerSchema = Utils.loadSchema(
                new File(this.
                        getClass().
                        getClassLoader().
                        getResource(readerSchemaFilename).
                        getFile()));
        writerSchema = Utils.loadSchema(
                new File(this.getClass().getClassLoader().getResource(writerSchemaFilename).getFile()));

        // The serializers
        serializer = new AvroGenericSerializer(writerSchema);
        deserializer = new AvroGenericDeserializer(readerSchema, writerSchema);

        // These are only for records creation
        nodeSchema = SchemaUtils.getTypeSchemaByName(writerSchema, TestUtils.NODE_SCHEMA_FULLNAME, true);
        edgeSchema = SchemaUtils.getTypeSchemaByName(writerSchema, TestUtils.EDGE_SCHEMA_FULLNAME, true);

        logger.debug("readerSchema: "+readerSchemaFilename);
        logger.debug("writerSchema: "+writerSchemaFilename);
    }

    public void compareNodes(GenericData.Record node, GenericData.Record record){
        assertTrue(record.get("id").equals(node.get("id")));
        if(record.get("label")!=null)
            assertTrue(record.get("label").toString().equals(node.get("label").toString()));
        else
            assertTrue(record.get("role").toString().equals(node.get("label").toString()));
        Map<String, String> properties = (HashMap<String, String>)node.get("properties");
        // avro uses utf8 for map key (instead of String)
        Map<Utf8, String> recordproperties = (HashMap<Utf8, String>)record.get("properties");
        assertTrue(properties.size() == recordproperties.size());
        for(Utf8 o:recordproperties.keySet()) {
            assertTrue(properties.get(o.toString()).equals(recordproperties.get(o).toString()));
        }
    }

    public void compareEdges(GenericData.Record edge, GenericData.Record record){
        if(record.get("label")!=null)
            assertTrue(record.get("label").toString().equals(edge.get("label").toString()));
        else
            assertTrue(record.get("role").toString().equals(edge.get("label").toString()));
        //logger.debug(edge.get("fromNode").getClass().getCanonicalName());
        GenericData.Record fromNode = (GenericData.Record)edge.get("fromNode");
        GenericData.Record toNode = (GenericData.Record)edge.get("toNode");
        compareNodes((GenericData.Record) edge.get("fromNode"),
                (GenericData.Record) record.get("fromNode"));
        compareNodes((GenericData.Record) edge.get("toNode"),
                (GenericData.Record) record.get("toNode"));

    }
    protected GenericData.Record newGenericEdge(){
        // create a generic user record using the schema
        Map<String, String> properties = new HashMap<>();
        properties.put("timestamp", ""+System.currentTimeMillis());
        GenericRecordBuilder erb = new GenericRecordBuilder(readerSchema);
        GenericRecordBuilder nrb = new GenericRecordBuilder(readerSchema.getField("fromNode").schema());
        GenericData.Record node1 = nrb.
                set("id", 1L).
                set("label", "unitOfExecution").
                set("properties", properties).build();

        GenericData.Record node2 = nrb.
                set("id", 2L).
                set("label", "artifact").build();

        GenericData.Record edge = erb.
                set("label", "read").
                set("fromNode", node1).
                set("toNode", node2).
                set("properties", properties).build();
        return edge;
    }


}
