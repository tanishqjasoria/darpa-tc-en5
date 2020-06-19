/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericData;
import org.apache.avro.util.Utf8;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Test serializing different record types to files
 * @author jkhoury
 */
public class FileSerializationTest extends BaseTest {

    private static final Logger logger = Logger.getLogger(FileSerializationTest.class);
    private static File nodeFile;
    private static File edgeFile;

    public FileSerializationTest(){
        super();
        String path = this.getClass().getClassLoader().getResource(".").getPath();
        nodeFile = new File(path + "testNodes.avro");
        edgeFile = new File(path + "testEdges.avro");
    }

    @Test
    public void doTest() {
        execute(true);
    }

    @Test
    public void doTest2() {
        // test again with the non union schema
        setReaderSchemaFilename("schemas/test/LabeledEdge.avsc");
        setWriterSchemaFilename("schemas/test/LabeledEdge.avsc");
        execute(false);
    }

    protected void execute(boolean unionSchema){
        /**
         * Serialize and deserialize to disk
         * Disk serialization always writes the schema in the file
         * So the data is self-describing
         */
        try{

            //same writer and reader schema
            initialize();

            // Create the records
            GenericData.Record node1 = TestUtils.createNode(1, "unitOfExecution", true, nodeSchema);
            logger.debug("created node "+node1.toString());
            GenericData.Record node2 = TestUtils.createNode(2, "artifact", true, nodeSchema);
            logger.debug("created node "+node2.toString());
            GenericData.Record edge = TestUtils.createEdge(node1, node2, "read", true, edgeSchema);
            logger.debug("created edge "+edge.toString());

            List<GenericData.Record> records = new ArrayList<>();
            List<GenericContainer> nodeRecords;
            List<GenericContainer> edgeRecords;

            if(unionSchema) {

                // reinitialize the node serializer
                serializer = new AvroGenericSerializer(writerSchema, nodeFile);

                /**
                 * first the nodes: note that the schema is written to the file as well!
                 * With avro, the schema always accompanies the data
                 */
                records.add(node1);
                records.add(node2);
                serializer.serializeToFile(records);
                logger.debug("serialized nodes to " + nodeFile.getAbsolutePath() + ", len " + nodeFile.length());
                serializer.close();

                // test the deserialization
                deserializer = new AvroGenericDeserializer(readerSchema, writerSchema, false, nodeFile);
                /**
                 * deserialize the nodes and edges, using the same deserializer
                 */
                nodeRecords = deserializer.deserializeNRecordsFromFile(0);
                logger.debug("deserialized nodes from " + nodeFile.getAbsolutePath()
                        + ", numrecords " + nodeRecords.size());
                assertTrue(nodeRecords.size() == 2);
                logger.debug("deserialized node 1 " + nodeRecords.get(0).toString());
                compareNodes(node1, (GenericData.Record)nodeRecords.get(0));

                logger.debug("deserialized node 2 " + nodeRecords.get(1).toString());
                compareNodes(node2, (GenericData.Record)nodeRecords.get(1));

                deserializer.close();
            }

            serializer = new AvroGenericSerializer(writerSchema, edgeFile);
            // now serialize the edge 4 times
            serializer.serializeToFile(edge);
            logger.debug("serialized edge "+ edge.toString());
            HashMap<String, String> properties = (HashMap)edge.get("properties");
            properties.put("timestamp", "test1");
            edge.put("properties", properties);
            serializer.serializeToFile(edge);
            logger.debug("serialized edge "+ edge.toString());
            properties.put("timestamp", "test2");
            edge.put("properties", properties);
            serializer.serializeToFile(edge);
            logger.debug("serialized edge "+ edge.toString());
            properties.put("timestamp", "test3");
            edge.put("properties", properties);
            serializer.serializeToFile(edge);
            logger.debug("serialized edge "+ edge.toString());
            logger.debug("serialized 4 edges to " + edgeFile.getAbsolutePath() + ", len " + edgeFile.length());
            serializer.close();

            deserializer = new AvroGenericDeserializer(readerSchema, writerSchema, false, edgeFile);

            //deserialize the edge
            GenericData.Record edgeRecord;
            edgeRecord = (GenericData.Record) deserializer.deserializeNextRecordFromFile();
            logger.debug("deserialized edge "+ edgeRecord.toString());
            compareEdges(edge, (GenericData.Record)edgeRecord);

            edgeRecord = (GenericData.Record) deserializer.deserializeNextRecordFromFile();
            logger.debug("deserialized edge "+ edgeRecord.toString());
            compareEdges(edge, (GenericData.Record)edgeRecord);
            properties = (HashMap<String, String>)edgeRecord.get("properties");
            assertTrue(properties.get(new Utf8("timestamp")).equals("test1"));

            edgeRecord = (GenericData.Record) deserializer.deserializeNextRecordFromFile();
            logger.debug("deserialized edge "+ edgeRecord.toString());
            compareEdges(edge, (GenericData.Record)edgeRecord);
            properties = (HashMap)edgeRecord.get("properties");
            assertTrue(properties.get(new Utf8("timestamp")).equals("test2"));

            edgeRecord = (GenericData.Record) deserializer.deserializeNextRecordFromFile();
            logger.debug("deserialized edge "+ edgeRecord.toString());
            compareEdges(edge, (GenericData.Record)edgeRecord);
            properties = (HashMap)edgeRecord.get("properties");
            assertTrue(properties.get(new Utf8("timestamp")).equals("test3"));

            edgeRecord = (GenericData.Record) deserializer.deserializeNextRecordFromFile();
            assertTrue(edgeRecord == null);

            edgeRecord = (GenericData.Record) deserializer.deserializeNextRecordFromFile();
            assertTrue(edgeRecord == null);

            edgeRecords = deserializer.deserializeNRecordsFromFile(10);
            logger.debug("num deserialized edges " + edgeRecords.size());
            assertTrue(edgeRecords.size() == 0);

            // reset and try again
            deserializer = new AvroGenericDeserializer(readerSchema, writerSchema, false, edgeFile);
            edgeRecords = deserializer.deserializeNRecordsFromFile(10);
            logger.debug("num deserialized edges " + edgeRecords.size());
            assertTrue(edgeRecords.size() == 4);

            edgeRecords = deserializer.deserializeNRecordsFromFile(10);
            logger.debug("num deserialized edges " + edgeRecords.size());
            assertTrue(edgeRecords.size() == 0);

            logger.debug("Test complete");

        }catch (Exception e){
            e.printStackTrace();
            logger.error(e);
            assertTrue(false);
        }finally{
            if(nodeFile != null) nodeFile.delete();
            if(edgeFile != null) edgeFile.delete();
        }

    }

}
