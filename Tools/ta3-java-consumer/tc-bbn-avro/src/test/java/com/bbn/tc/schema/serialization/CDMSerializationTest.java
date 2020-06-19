/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

import com.bbn.tc.schema.avro.cdm20.*;
import org.apache.avro.Schema;
import org.apache.log4j.Logger;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Tests CDM record serialization to bytes and to Json using the
 *   ReadableJsonEncoder/Decoder to make sure that bytes and FIXEd
 *   are serialized to a readable format
 *
 * @author jkhoury
 */
public class CDMSerializationTest{

    private static final Logger logger = Logger.getLogger(CDMSerializationTest.class);
    AvroGenericSerializer serializer;
    AvroGenericDeserializer deserializer;

    @Test
    public void doJsonTest(){
        try{
            init(Event.getClassSchema());

            // create an Event with a Value record, to test the BYTES type, byte[] and FIXED type UUID
            Event event = TestUtils.generateEvent();

            // use serializer to serialize to jsonString
            String jsonString = serializer.serializeToJson(event, true);
            logger.debug("Event toString: " + event.toString());
            logger.debug("Event serialized toJson: " + jsonString);

            // now deserialize
            Event devent = (Event) deserializer.deserializeJson(jsonString);
            logger.debug("Deserialized event toString: " + devent.toString());

            assertTrue(devent!= null && devent.toString().equals(event.toString()));
            assertTrue(Arrays.equals(devent.getUuid().bytes() , event.getUuid().bytes()));
            assertTrue(devent.getParameters() != null);
            List<Value> vals = event.getParameters();
            List<Value> dvals = devent.getParameters();
            assertTrue(Arrays.equals(dvals.get(0).getValueBytes().array(), vals.get(0).getValueBytes().array()));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Specifically test the bug where ByteBuffer (corresponding to bytes data type) gets manipulated
     * by avro wrongly resulting in its position being reset.
     */
    @Test public void doJsonTest2(){
        try{
            init(Event.getClassSchema());

            // create an Event with a Value record, to test the BYTES type, byte[]
            Event event = TestUtils.generateEvent();
            logger.debug("Event toString before json: " + event.toString()); // PRINT THE EVENT BEFORE SERIALIZING IT

            // use serializer to serialize to jsonString
            String jsonString = serializer.serializeToJson(event, true);
            logger.debug("Event serialized toJson: " + jsonString);

            // now deserialize
            Event devent = (Event) deserializer.deserializeJson(jsonString);

            List<Value> params = event.getParameters();
            List<Value> dparams = devent.getParameters();
            for(int i=0; i< params.size();i++){
                Value v = params.get(i);
                Value dv = dparams.get(i);
                logger.debug(Utils.toHexString(v.getValueBytes().array(), 0, v.getValueBytes().array().length));
                logger.debug(Utils.toHexString(dv.getValueBytes().array(), 0, v.getValueBytes().array().length));
                assertTrue(v.getValueBytes() !=null && dv.getValueBytes() != null);
                assertTrue(Utils.toHexString(v.getValueBytes().array(), 0, v.getValueBytes().array().length) != null);
                assertTrue(Utils.toHexString(v.getValueBytes().array(), 0, v.getValueBytes().array().length).equals(
                        Utils.toHexString(dv.getValueBytes().array(), 0, dv.getValueBytes().array().length)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    protected void init(Schema schema) throws Exception{
        serializer = new AvroGenericSerializer(schema, true, null);
        deserializer = new AvroGenericDeserializer(schema, schema, true);
    }



}
