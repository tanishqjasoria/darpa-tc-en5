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
 * @author jkhoury
 */
public class HexStringTest {
    private final Logger logger = Logger.getLogger(HexStringTest.class);
    final Random random = new Random();
    @Test
    public void doTest(){
        try{
            int LEN=256;
            byte [] bytes = new byte[LEN];
            random.nextBytes(bytes);
            //String trueHexString = "0108107F";
            String s = Utils.toHexString(bytes, 0, LEN);
            logger.debug("hexString: " + s + ", byte[]: "+ Arrays.toString(bytes));
            //assertTrue(s.equals(trueHexString));

            //now the opposite
            byte[] recoveredBytes = Utils.fromHexString(s);
            logger.debug("hexString: " + s + ", byte[]: "+ Arrays.toString(recoveredBytes));
            assertTrue(Arrays.equals(bytes, recoveredBytes));

            //test 0 size string
            s = "";
            byte [] sb = s.getBytes("UTF_32BE");
            String ss = Utils.toHexString(sb, 0, sb.length);
            logger.debug("size 0 hexString: " + ss + ", byte[]: "+ Arrays.toString(sb));
            assertTrue(ss != null && ss.isEmpty());

        }catch (Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
