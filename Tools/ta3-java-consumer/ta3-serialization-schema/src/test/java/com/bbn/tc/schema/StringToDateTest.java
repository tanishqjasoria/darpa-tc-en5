/*
 * Copyright (c) 2016 Raytheon BBN Technologies Corp.  All rights reserved.
 */

package com.bbn.tc.schema;

import com.bbn.tc.schema.utils.Constants;
import com.bbn.tc.schema.utils.SchemaUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Simple test to verify date and timezone parsing works
 * @author jkhoury
 */
public class StringToDateTest {

    private static final Logger logger = Logger.getLogger(StringToDateTest.class);

    @Test
    public void doTest(){
        String strDate  = "2016-02-01T16:15:37-05:00";
        String strDate2 = "2016-02-01T13:15:37-08:00";
        try {

            Date date = SchemaUtils.stringToDate(strDate, Constants.DATE_FORMAT_A, null);
            Date date2 = SchemaUtils.stringToDate(strDate2, Constants.DATE_FORMAT_A, null);
            logger.debug(date.toString() + "; " + date.getTime());
            logger.debug(date2.toString()+ "; " + date.getTime());
            assert date.getTime()  == date2.getTime();
            assert date.toString().equals(date2.toString());

            // convert a Date to string
            Date now = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_B);
            String strDateNow = dateFormat.format(now);
            logger.debug(strDateNow);

        }catch (Exception e){
            e.printStackTrace();
            assert false;
        }

    }
}
