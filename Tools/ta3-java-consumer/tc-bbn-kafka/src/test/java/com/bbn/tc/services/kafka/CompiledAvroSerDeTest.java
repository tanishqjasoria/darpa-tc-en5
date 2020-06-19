/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.services.kafka;

import com.bbn.tc.schema.avro.EDGE_LABELS;
import com.bbn.tc.schema.avro.LabeledEdge;
import com.bbn.tc.schema.avro.LabeledNode;
import com.bbn.tc.schema.avro.NODE_LABELS;
import com.bbn.tc.schema.serialization.AvroConfig;
import org.apache.avro.Schema;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Test avro serialization and deserialization of compiled schema
 * @author jkhoury
 */
public class CompiledAvroSerDeTest extends BaseTest{

    private static final Logger logger = Logger.getLogger(CompiledAvroSerDeTest.class);

    @Test
    public void doTest(){

        try {
            //first get the schema from the class path
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(schemaFileName);
            schema = new Schema.Parser().parse(is);

            // Build a compiled Edge
            LabeledNode from = LabeledNode.newBuilder().setId(1L).setLabel(NODE_LABELS.unitOfExecution).build();
            LabeledNode to = LabeledNode.newBuilder().setId(2L).setLabel(NODE_LABELS.artifact).build();
            LabeledEdge edge = LabeledEdge.newBuilder().setFromNode(from).setToNode(to).setLabel(EDGE_LABELS.read)
                    .build();

            logger.debug("Created edge " + edge);

            Map<String, Object> props = new HashMap();
            // additional serialization properties needed
            props.put(AvroConfig.SCHEMA_WRITER_SCHEMA, schema);
            props.put(AvroConfig.SCHEMA_SERDE_IS_SPECIFIC, true);

            // init the serializers
            initialize(props);

            //serialize
            byte[] bytes = serializer.serialize("topic", edge);
            logger.debug("Serialized compiled edge to "+ Arrays.toString(bytes));

            // deserialize
            LabeledEdge deserializedEdge = (LabeledEdge)deserializer.deserialize("topic", bytes);
            logger.debug("Deserialized edge " + deserializedEdge.toString());
            assertTrue(deserializedEdge != null);
            assertTrue(deserializedEdge.toString().equals(edge.toString()));


        }catch(Exception e){
            e.printStackTrace();
            assertTrue(false);
        }



    }
}
