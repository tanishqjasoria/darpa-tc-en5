/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

import org.apache.avro.generic.GenericData;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * @author jkhoury
 */
public class JsonSerializationTest extends BaseTest{

    private static final Logger logger = Logger.getLogger(JsonSerializationTest.class);

    @Test
    public void doTest() {
        logger.debug("doTest");
        execute(true);
    }

    public void execute(boolean union){

        try {
            setWriterSchemaFilename("schemas/test/LabeledEdge.avsc");
            setReaderSchemaFilename("schemas/test/LabeledEdge.avsc");
            // init the serializers
            initialize();

            GenericData.Record node1 = TestUtils.createNode(1, "unitOfExecution", true, nodeSchema);
            logger.debug("created node "+node1.toString());
            GenericData.Record node2 = TestUtils.createNode(2, "artifact", true, nodeSchema);
            logger.debug("created node "+node2.toString());
            GenericData.Record edge = TestUtils.createEdge(node1, node2, "read", true, edgeSchema);
            logger.debug("created edge "+edge.toString());

            // use serializer to serialize to jsonString
            String jsonString = serializer.serializeToJson(edge, true);
            logger.debug("Edge serialized to Json " + jsonString);

            // now deserialize
            GenericData.Record record = (GenericData.Record)deserializer.deserializeJson(jsonString);
            assertTrue(record!= null && record.toString().equals(edge.toString()));

            // use the same serializer to serialize to bytes
            edge = TestUtils.createEdge(node1, node2, "modified", true, edgeSchema);
            byte[] bytes = serializer.serializeToBytes(edge);
            logger.debug("New edge serialized to bytes "+ Arrays.toString(bytes));

            // then use the serializer to serialize to json again
            jsonString = serializer.serializeToJson(edge, true);
            logger.debug("New edge serialized to Json " + jsonString);

            // now deserialize
            record = (GenericData.Record)deserializer.deserializeJson(jsonString);
            assertTrue(record!= null && record.toString().equals(edge.toString()));


        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }


    }

}
