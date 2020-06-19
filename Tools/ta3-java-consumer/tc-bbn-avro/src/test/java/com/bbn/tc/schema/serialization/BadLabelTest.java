/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

import org.apache.avro.generic.GenericData;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for verifying proper behavior when unexpected labels are passed
 * into the Avro API for nodes and edges.
 * @author tupty
 */
public class BadLabelTest extends BaseTest {
    private static final Logger logger =
            Logger.getLogger(BadLabelTest.class);
    private GenericData.Record unitOfExecNode;
    private GenericData.Record agentNode;

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
     * Test unexpected labels being passed into Avro API for nodes.
     */
    @Test
    public void badNodeLabelTest() {
        // Create a node with a bad label value.
        String badLabel = "badNodeLabel";
        GenericData.Record badNode = TestUtils.createNode(1,
                badLabel, nodeSchema);
        logger.debug("created node " + badNode.toString());
        assertTrue(badNode.get("label").toString() == badLabel);

        // Make sure that validate calls return false for this record.
        boolean isValid = GenericData.get().validate(writerSchema, badNode);
        assertTrue(!isValid);
    }

    /**
     * Test unexpected labels being passed into Avro API for edges.
     */
    @Test
    public void badEdgeLabelTest() {
        // Create a few valid nodes which can be used for our edge.
        unitOfExecNode = TestUtils.createNode(1, "unitOfExecution", true,
                nodeSchema);
        logger.debug("created node " + unitOfExecNode.toString());
        agentNode = TestUtils.createNode(2, "agent", true, nodeSchema);
        logger.debug("created node " + agentNode.toString());

        // Create an edge with a bad label value.
        String badLabel = "badLabel";
        GenericData.Record badEdge = TestUtils.createEdge(unitOfExecNode,
                agentNode, badLabel, edgeSchema);
        logger.debug("created edge " + badEdge.toString());
        assertTrue(badEdge.get("label").toString() == badLabel);

        // Make sure that validate calls return false for this record.
        boolean isValid = GenericData.get().validate(writerSchema, badEdge);
        assertTrue(!isValid);
    }
}
