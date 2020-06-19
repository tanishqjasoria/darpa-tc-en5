/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

/**
 * The avro configurations
 *
 * Created by jkhoury on 9/30/15.
 */
public final class AvroConfig {

    /**
     * Kafka properties
     */
    public static final String SCHEMA_WRITER_FILE = "com.bbn.tc.schema.writer.file";
    public static final String SCHEMA_WRITER_SCHEMA = "com.bbn.tc.schema.writer.schema";
    public static final String SCHEMA_READER_FILE = "com.bbn.tc.schema.reader.file";
    public static final String SCHEMA_READER_SCHEMA = "com.bbn.tc.schema.reader.schema";
    public static final String SCHEMA_SERDE_IS_SPECIFIC = "com.bbn.tc.schema.serialization.isspecific";

}
