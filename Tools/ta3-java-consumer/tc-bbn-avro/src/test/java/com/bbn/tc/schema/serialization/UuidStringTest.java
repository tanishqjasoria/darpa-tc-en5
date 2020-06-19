/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author acaro
 */
public class UuidStringTest {
    private final Logger logger = Logger.getLogger(UuidStringTest.class);
    final Random random = new Random();
    @Test
    public void doTest(){
        try{
            int LEN=16;  // 128-bit UUID
            byte [] bytes = new byte[LEN];
            random.nextBytes(bytes);
            String s = Utils.toUuidString(bytes, 0, LEN);
            logger.debug("UUID String: " + s + ", byte[]: "+ Arrays.toString(bytes));

            //now the opposite
            byte[] recoveredBytes = Utils.fromUuidString(s);
            logger.debug("UUID String: " + s + ", byte[]: "+ Arrays.toString(recoveredBytes));
            assertTrue(Arrays.equals(bytes, recoveredBytes));
        }catch (Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
