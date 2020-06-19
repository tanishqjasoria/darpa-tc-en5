/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

import org.apache.log4j.Logger;

/**
 * @author jkhoury
 */
public class NewSchemaFileSerializationTest extends FileSerializationTest {

    private static final Logger logger = Logger.getLogger(NewSchemaFileSerializationTest.class);

    public NewSchemaFileSerializationTest() {
        super();
    }

    @Override
    public void doTest(){

        // update the reader schema
        setReaderSchemaFilename("schemas/test/LabeledGraphv2.avsc");

        super.doTest(); // this initializes and runs the same test
    }

    @Override
    public void doTest2(){

        // update the reader schema
        setWriterSchemaFilename("schemas/test/LabeledEdge.avsc");
        setReaderSchemaFilename("schemas/test/LabeledEdgev2.avsc");

        execute(false);
    }
}
