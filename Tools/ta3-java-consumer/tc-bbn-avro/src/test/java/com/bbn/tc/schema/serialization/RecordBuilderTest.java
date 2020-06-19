/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * @author jkhoury
 */
public class RecordBuilderTest extends BaseTest {

    private static final Logger logger = Logger.getLogger(RecordBuilderTest.class);

    @Test
    public void doTest() {
        execute(true);
    }

    @Test
    public void doTest2() {
        // update the reader schema
        setWriterSchemaFilename("schemas/test/LabeledEdge.avsc");
        setReaderSchemaFilename("schemas/test/LabeledEdgev2.avsc");
        execute(false);
    }

    protected void execute(boolean unionSchema){

        try{
            initialize();

            //create a node using generic builder
            GenericRecordBuilder rb = new GenericRecordBuilder(nodeSchema);
            GenericRecordBuilder erb = new GenericRecordBuilder(edgeSchema);
            // create a node
            Map<String, String> properties = new HashMap<>();
            properties.put("timestamp", ""+System.currentTimeMillis());
            properties.put("path", this.getClass().getClassLoader().getResource("./").getPath());
            rb.set("id", 1L).set("label", "artifact").set("properties", properties);
            GenericData.Record node = rb.build();
            logger.debug("Node " + node);

            properties.put("uname", "jkhoury");
            properties.remove("path");
            rb.set("id", 2L).set("label", "agent").set("properties", properties);
            GenericData.Record agent = rb.build();
            logger.debug("Agent " + agent);

            assertTrue(node.get("id").equals(1L));
            assertTrue(agent.get("id").equals(2L));
            HashMap<String, String> props = (HashMap<String, String>)node.get("properties");
            assertTrue(props.size() == 2);
            for(String s:props.keySet()){
                logger.debug("key:"+s+", value:"+props.get(s));
            }

            // same for an edge
            properties.remove("uname");
            properties.put("weight", "1");
            erb.set("label", "read").set("properties", properties).set("fromNode", node).set("toNode", agent);
            GenericData.Record edge = erb.build();
            logger.debug("Edge "+edge);

        }catch (Exception e){
            e.printStackTrace();
            assertTrue(false);
        }

    }
}
