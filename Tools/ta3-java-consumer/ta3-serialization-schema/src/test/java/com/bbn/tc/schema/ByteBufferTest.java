/*
 * Copyright (c) 2016 Raytheon BBN Technologies Corp.  All rights reserved.
 */

package com.bbn.tc.schema;

import org.apache.log4j.Logger;
import org.junit.Test;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertTrue;

/**
 * @author jkhoury
 */
public class ByteBufferTest extends BaseTest{

    private static final Logger logger = Logger.getLogger(ByteBufferTest.class);

    @Test
    public void doTest(){
        try {
            int iX = Integer.MAX_VALUE;
            char cX = 'ß'; //U+00DF, Latin Small Letter sharp S; WE CONVERT TO INT (UTF32)
            float fX = Float.MAX_VALUE;
            double dX = Double.MAX_VALUE;
            byte bX = 8;
            long lX = Long.MAX_VALUE;

            ByteBuffer bb = ByteBuffer.allocate(4 + 4 + 4 + 8 + 1 + 8)
                    .putInt(iX)
                    .putInt(cX) // NOTE HOW WE USED 4 bytes for the character by converting to INT (UTF32)
                    .putFloat(fX)
                    .putDouble(dX)
                    .put(bX)
                    .putLong(lX);
            byte[] bytes = bb.array();
            //convert the byte array to hex to visualize
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02X ", b));
            }
            logger.debug(sb.toString());
            assertTrue(bytes[0] == (byte) 0x7F);
            assertTrue(bytes[6] == (byte) 0x00);
            assertTrue(bytes[7] == (byte) 0xDF);

            // test the UTF_32 encoding for string characters
            bytes = "abcdefghiß".getBytes("UTF_32BE");
            sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02X ", b));
            }
            logger.debug(sb.toString());
            assertTrue(bytes.length == 40);
            assertTrue(bytes[3] == (byte) 0x61);
            assertTrue(bytes[38] == (byte) 0x00);
            assertTrue(bytes[39] == (byte) 0xDF);

        }catch (Exception e){
            e.printStackTrace();
            assert(false);
        }

    }
}
