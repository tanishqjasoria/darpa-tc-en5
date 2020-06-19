/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

import org.apache.avro.Schema;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Abstract root class
 * Created by jkhoury on 9/25/15.
 */
public abstract class AvroGenericAbstractDeSerializer {
    private final Logger logger = Logger.getLogger(AvroGenericAbstractDeSerializer.class);
    protected Schema readerSchema, writerSchema;
    protected boolean sameSchema=true;
    protected boolean isSpecific = false;

    public AvroGenericAbstractDeSerializer(){}

    public AvroGenericAbstractDeSerializer(String schemaFileName)
            throws IOException, IllegalArgumentException{
        this(schemaFileName, schemaFileName, false);
    }

    public AvroGenericAbstractDeSerializer(String readerSchemaFileName, String writerSchemaFileName, boolean isSpecific)
            throws IOException, IllegalArgumentException{
        this.isSpecific = isSpecific;
        initialize(readerSchemaFileName, writerSchemaFileName);
    }

    public AvroGenericAbstractDeSerializer(Schema schema){
        this(schema, schema, false);
    }

    public AvroGenericAbstractDeSerializer(Schema readerSchema, Schema writerSchema, boolean isSpecific)
            throws IllegalArgumentException{
        this.isSpecific = isSpecific;
        initialize(readerSchema, writerSchema);
    }

    /**
     * This is a kafka initialization method, typically used with default constructor
     * @param configs the kafka configuration parameters
     * @param isKey we dont care about whether it is key or value
     */
    protected void initialize(Map<String, ?> configs, boolean isKey) throws Exception{

        Object writerSchemaFileName = configs.get(AvroConfig.SCHEMA_WRITER_FILE);
        Object readerSchemaFileName = configs.get(AvroConfig.SCHEMA_READER_FILE);
        Object _writerSchema = configs.get(AvroConfig.SCHEMA_WRITER_SCHEMA);
        Object _readerSchema = configs.get(AvroConfig.SCHEMA_READER_SCHEMA);
        Object _isSpecific = configs.get(AvroConfig.SCHEMA_SERDE_IS_SPECIFIC);

        if(_isSpecific != null && (boolean)_isSpecific)
            isSpecific = true;

        if(writerSchemaFileName == null) {
            // check if the actual schema object was passed in instead
            if(_writerSchema == null)
                throw new Exception("Writer schema config is null. You must specify either  "
                + AvroConfig.SCHEMA_WRITER_FILE + " or " + AvroConfig.SCHEMA_WRITER_SCHEMA);

            if(_readerSchema == null) _readerSchema = _writerSchema;
            logger.debug("Configs: SCHEMA_WRITER_SCHEMA "+_writerSchema.toString()
                    +", SCHEMA_READER_SCHEMA "+_readerSchema.toString()
                    + ", isSpecific "+isSpecific);

            initialize((Schema)_readerSchema, (Schema)_writerSchema);
        }else{
            if(readerSchemaFileName == null) readerSchemaFileName = writerSchemaFileName;
            logger.debug("Configs: SCHEMA_WRITER_FILE "+writerSchemaFileName
                    +", SCHEMA_READER_FILE "+readerSchemaFileName
                    + ", isSpecific "+isSpecific);

            initialize((String)readerSchemaFileName, (String)writerSchemaFileName);
        }
    }

    protected void initialize(String readerSchemaFileName, String writerSchemaFileName)
            throws IOException, IllegalArgumentException{
        if(readerSchemaFileName == null || writerSchemaFileName == null)
            throw new IllegalArgumentException("SchemaFile can not be null");

        File readerSchemaFile = new File(readerSchemaFileName);
        File writerSchemaFile = (writerSchemaFileName != null &&
                !writerSchemaFileName.equals(readerSchemaFileName))
                ? new File(writerSchemaFileName) : readerSchemaFile;
        if(!readerSchemaFile.exists())
            throw new IllegalArgumentException("ReaderSchemaFile does not exist "+readerSchemaFileName);
        if(!writerSchemaFile.exists())
            throw new IllegalArgumentException("WriterSchemaFile does not exist "+writerSchemaFileName);

        Schema rschema = new Schema.Parser().parse(readerSchemaFile);
        initialize(rschema, writerSchemaFile == readerSchemaFile ? rschema
                : new Schema.Parser().parse(writerSchemaFile));
    }

    protected void initialize(Schema readerSchema, Schema writerSchema)
            throws IllegalArgumentException{
        if(readerSchema == null || writerSchema == null)
            throw new IllegalArgumentException("Schema can not be null");
        this.readerSchema = readerSchema;
        this.writerSchema = writerSchema;
        sameSchema = readerSchema == writerSchema;
    }

    public Schema getReaderSchema() {
        return readerSchema;
    }

    public Schema getWriterSchema() {
        return writerSchema;
    }

    /**
     * This is called by the garbage collector when freeing the object
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    /**
     * Cleanup non java resources, must be overriden by all objects
     */
    public abstract  void close();
}
