/*
 * Copyright (c) 2016 Raytheon BBN Technologies Corp.  All rights reserved.
 */

package com.bbn.tc.schema;

import com.bbn.tc.schema.avro.EDGE_LABELS;
import com.bbn.tc.schema.avro.LabeledEdge;
import com.bbn.tc.schema.avro.LabeledNode;
import com.bbn.tc.schema.avro.NODE_LABELS;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Base test class
 * @author jkhoury
 */
public abstract class BaseTest {

    private static final Logger logger = Logger.getLogger(BaseTest.class);

    protected String readerSchemaFilename = "LabeledEdge.avsc";
    protected Schema readerSchema;



    protected String path;

    protected EncoderFactory encoderFactory = EncoderFactory.get();
    protected DecoderFactory decoderFactory = DecoderFactory.get();

    protected void initialize() throws Exception {
        path = this.
                getClass().
                getClassLoader().getResource(".").getPath();
        // The schemas
        readerSchema = new Schema.Parser().parse(
                new File(path+"../../avro/"+readerSchemaFilename)
        );
    }


}
