/*
 * Copyright (c) 2016 Raytheon BBN Technologies Corp.  All rights reserved.
 */

package com.bbn.tc.schema;

import com.bbn.tc.schema.avro.cdm20.UUID;
import com.bbn.tc.schema.utils.SchemaUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * @author jkhoury
 */
public class UUIDTest {

    private static final Logger logger = Logger.getLogger(UUIDTest.class);

    @Test
    public void doTest(){
        try {
            long a = 1;
            byte[] uuidbytes = SchemaUtils.toUUID(a).bytes();
            logger.debug(a + ", " + Arrays.toString(uuidbytes));
            for(int i =0;i<uuidbytes.length-1;i++) assertTrue(uuidbytes[i] == 0);
            assertTrue(uuidbytes[uuidbytes.length - 1] == 1);

            int b = 1;
            uuidbytes = SchemaUtils.toUUID(b).bytes();
            logger.debug(b + ", " + Arrays.toString(uuidbytes));
            for(int i =0;i<uuidbytes.length-1;i++) assertTrue(uuidbytes[i] == 0);
            assertTrue(uuidbytes[uuidbytes.length - 1] == 1);

            a = Long.MAX_VALUE;
            uuidbytes = SchemaUtils.toUUID(a).bytes();
            byte[] abytes = ByteBuffer.allocate(8).putLong(a).array();
            logger.debug(Arrays.toString(abytes));
            logger.debug(a + ", " + Arrays.toString(uuidbytes));
            for(int i =0;i<uuidbytes.length-8;i++) assertTrue(uuidbytes[i] == 0);
            for(int i = uuidbytes.length-8, j=0; i<uuidbytes.length; i++, j++)
                assertTrue(uuidbytes[i] == abytes[j]);

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
