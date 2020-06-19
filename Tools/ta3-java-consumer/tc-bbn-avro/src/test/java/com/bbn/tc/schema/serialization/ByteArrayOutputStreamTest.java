/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Test reusing the output stream across multiple invocations
 * @author jkhoury
 */
public class ByteArrayOutputStreamTest {

    private static final Logger logger = Logger.getLogger(ByteArrayOutputStreamTest.class);

    @Test
    public void doTest(){
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            // write some bytes
            out.write("hello".getBytes());
            out.flush();

            byte [] bytes = out.toByteArray();
            logger.debug("wrote "+out.size() + " bytes, serialized to "+ bytes.length);
            assertTrue(bytes.length == out.size());
            assertTrue(bytes.length == 5);

            // reset the output stream (count=0) to test reuse
            out.reset();
            out.write("hi".getBytes());
            byte [] nbytes = out.toByteArray();
            logger.debug("wrote "+out.size() + " bytes, serialized to "+ nbytes.length);
            assertTrue(nbytes.length == out.size());
            assertTrue(nbytes.length == 2);

            logger.debug("first "+ new String(bytes) + ", second "+new String(nbytes));
            logger.debug("Yes the outputstream is reusable!");


        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }

    }
}
