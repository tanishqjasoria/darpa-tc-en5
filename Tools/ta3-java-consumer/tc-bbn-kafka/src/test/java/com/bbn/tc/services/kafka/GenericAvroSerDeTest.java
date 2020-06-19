/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.services.kafka;

import com.bbn.tc.schema.serialization.AvroConfig;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * @author jkhoury
 */
public class GenericAvroSerDeTest extends BaseTest{

    private static final Logger logger = Logger.getLogger(GenericAvroSerDeTest.class);

    @Test
    public void doTest(){

        try {

            //first get the schema from the class path
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(schemaFileName);
            schema = new Schema.Parser().parse(is);

            // Build a generic Edge record
            GenericRecordBuilder erb = new GenericRecordBuilder(schema);
            Schema nodeSchema = schema.getField("fromNode").schema();
            GenericRecordBuilder nrb = new GenericRecordBuilder(nodeSchema);

            GenericData.Record from
                    = nrb.set("label", new GenericData.EnumSymbol(nodeSchema, "unitOfExecution")).set("id", 1L).build();
            GenericData.Record to
                    = nrb.set("label", new GenericData.EnumSymbol(nodeSchema, "artifact")).set("id", 2L).build();
            GenericData.Record edge = erb.set("fromNode", from).set("toNode", to).
                    set("label", new GenericData.EnumSymbol(schema, "generated")).build();

            logger.debug("Created generic edge " + edge);

            Map<String, Object> props = new HashMap();
            // additional serialization properties needed
            props.put(AvroConfig.SCHEMA_WRITER_SCHEMA, schema);
            // note below we are explicitly telling the serializers that it is generic
            props.put(AvroConfig.SCHEMA_SERDE_IS_SPECIFIC, false);//generic

            // init the serializers
            initialize(props);

            //serialize
            byte[] bytes = serializer.serialize("topic", edge);
            logger.debug("Serialized generic edge to "+ Arrays.toString(bytes));

            // deserialize
            GenericData.Record deserializedEdge = (GenericData.Record)deserializer.deserialize("topic", bytes);
            logger.debug("Deserialized generic edge " + deserializedEdge.toString());
            assertTrue(deserializedEdge != null);
            assertTrue(deserializedEdge.toString().equals(edge.toString()));


        }catch(Exception e){
            e.printStackTrace();
            assertTrue(false);
        }



    }

}
