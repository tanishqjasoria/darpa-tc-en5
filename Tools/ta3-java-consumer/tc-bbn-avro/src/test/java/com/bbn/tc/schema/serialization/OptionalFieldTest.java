/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

import com.bbn.tc.schema.SchemaNotInitializedException;
import org.apache.avro.generic.GenericData;
import org.apache.log4j.Logger;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for verifying proper behavior for optional fields when
 * serializing and deserializing using the Avro API.
 * @author tupty
 */
public class OptionalFieldTest extends BaseTest {

    private static final Logger logger =
            Logger.getLogger(OptionalFieldTest.class);

    /**
     * Initialize class for the optional field test cases.
     */
    @Before
    public void setup() {
        try {
            // Setup the schemas
            initialize();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            assertTrue(false);
        }
    }

    /**
     * Create an edge record with values set in the optional properties field.
     */
    @Test
    public void optionalFieldPresentTest() {
        // First, create our nodes and edges.  Make sure that the edge node
        // has at least one property value set.
        GenericData.Record unitOfExecNode = TestUtils.createNode(1,
                "unitOfExecution", true, nodeSchema);
        logger.debug("created node " + unitOfExecNode.toString());
        GenericData.Record agentNode = TestUtils.createNode(2, "agent", true,
                nodeSchema);
        logger.debug("created node " + agentNode.toString());
        GenericData.Record edge1 = TestUtils.createEdge(unitOfExecNode, agentNode,
                "wasAssociatedWith", true, edgeSchema);
        logger.debug("created edge " + edge1.toString());

        // Ensure that our properties value was set on the edge record
        // which has not been serialized yet.
        assertTrue(edge1.get("properties") != null);

        // Serialize the edge record.
        byte[] serializedEdge = null;
        try {
            serializedEdge = serializer.serializeToBytes(edge1);
            logger.debug("serializing edge record " + edge1.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            logger.error(ioe);
            assertTrue(false);
        }

        // Deserialize the edge record.
        try {
            GenericData.Record record =
                    (GenericData.Record) deserializer.deserializeBytes(serializedEdge);
            logger.debug("deserializing edge record " + edge1.toString());

            // Verify that the edge properties is still set after
            // deserialization.
            assertTrue(record.get("properties") != null);
        } catch (SchemaNotInitializedException snie) {
            snie.printStackTrace();
            logger.error(snie);
            assertTrue(false);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            logger.error(ioe);
            assertTrue(false);
        }
    }

    /**
     * Create an edge record without any values set in the optional
     * properties field.
     */
    @Test
    public void optionalFieldAbsentTest() {
        // First, create our nodes and edges.  Make sure that the edge node
        // has no property values set.
        GenericData.Record unitOfExecNode = TestUtils.createNode(1,
                "unitOfExecution", true, nodeSchema);
        logger.debug("created node " + unitOfExecNode.toString());
        GenericData.Record agentNode = TestUtils.createNode(2, "agent", true,
                nodeSchema);
        logger.debug("created node " + agentNode.toString());
        GenericData.Record edge1 = TestUtils.createEdge(unitOfExecNode, agentNode,
                "wasAssociatedWith", false, edgeSchema);
        logger.debug("created edge " + edge1.toString());

        // Ensure that our properties value was not set on the edge record
        // which has not been serialized yet.
        assertTrue(edge1.get("properties") == null);

        // Serialize the edge record.
        byte[] serializedEdge = null;
        try {
            serializedEdge = serializer.serializeToBytes(edge1);
            logger.debug("serializing edge record " + edge1.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            logger.error(ioe);
            assertTrue(false);
        }

        // Deserialize the edge record.
        try {
            GenericData.Record record =
                    (GenericData.Record)deserializer.deserializeBytes(serializedEdge);
            logger.debug("deserializing edge record " + edge1.toString());

            // Verify that the edge properties is still not set after
            // deserialization.
            assertTrue(record.get("properties") == null);
        } catch (SchemaNotInitializedException snie) {
            snie.printStackTrace();
            logger.error(snie);
            assertTrue(false);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            logger.error(ioe);
            assertTrue(false);
        }
    }
}
