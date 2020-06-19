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
public class ByteSerializationTest extends BaseTest {

    private static final Logger logger = Logger.getLogger(ByteSerializationTest.class);

    public ByteSerializationTest(){super();}

    @Test
    public void doTest() {
        logger.debug("doTest");
        execute(true);
    }

    @Test
    public void doTest2() {
        logger.debug("doTest2");
        // test again with the non union schema
        setReaderSchemaFilename("schemas/test/LabeledEdge.avsc");
        setWriterSchemaFilename("schemas/test/LabeledEdge.avsc");
        execute(false);
    }

    protected void execute(boolean unionSchema){

        try {
            //same writer and reader schema
            initialize();
            assertTrue(nodeSchema != null);
            assertTrue(edgeSchema != null);
            // Create the records
            GenericData.Record node1 = TestUtils.createNode(1, "unitOfExecution", true, nodeSchema);
            logger.debug("created node "+node1.toString());
            GenericData.Record node2 = TestUtils.createNode(2, "artifact", true, nodeSchema);
            logger.debug("created node "+node2.toString());
            GenericData.Record edge = TestUtils.createEdge(node1, node2, "read", true, edgeSchema);
            logger.debug("created edge "+edge.toString());

            /**
             * serialize them using the original schema (with which records were created)
             * We use the same serializer to serialize both nodes and edges!!
             * byte serialization does *not* write the schema
             * so the reader needs to correctly know what schema should be used for deserialization
             */
            // First serialize to bytes (and deserialize from bytes)
            byte [] bytes = null;
            GenericData.Record record = null;

            if(unionSchema) {
                bytes = serializer.serializeToBytes(node1);
                logger.debug("Serialized node to " + bytes.length + " bytes: " + Arrays.toString(bytes));
                record = (GenericData.Record)deserializer.deserializeBytes(bytes);
                logger.debug("Deserialized node from bytes: " + record.toString());

                compareNodes(node1, record);

                bytes = serializer.serializeToBytes(node2);
                logger.debug("Serialized node to " + bytes.length + " bytes: " + Arrays.toString(bytes));
                record = (GenericData.Record)deserializer.deserializeBytes(bytes);
                logger.debug("Deserialized node from bytes: " + record.toString());

                compareNodes(node2, record);
            }

            bytes = serializer.serializeToBytes(edge);
            logger.debug("Serialized edge to "+ bytes.length + " bytes: " + Arrays.toString(bytes));
            record = (GenericData.Record)deserializer.deserializeBytes(bytes);
            logger.debug("Deserialized edge from bytes: "+record.toString());

            compareEdges(edge, record);

        }catch (Exception e){
            e.printStackTrace();
            logger.error(e);
            assertTrue(false);
        }
    }


}
