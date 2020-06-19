/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

import com.bbn.tc.schema.SchemaNotInitializedException;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Deserialize an avro record from a file and from a byte[]
 * The deserialization returns a typed object which is either a SpecificRecord (for compiled schemas)
 * or a GenericRecord (for non compiled schemas)
 *
 * We intentionally do not support json file deserialization at this point since that requires
 * some thinking about how to delimit the json strings etc.
 *
 * Created by jkhoury
 */
public class AvroGenericDeserializer extends AvroGenericAbstractDeSerializer {
    private final Logger logger = Logger.getLogger(AvroGenericDeserializer.class);
    private final DecoderFactory decoderFactory = DecoderFactory.get();
    private BinaryDecoder decoder;
    private ReadableJsonDecoder jsonDecoder;
    private DatumReader<GenericContainer> datumReader;
    private File inputFile;
    DataFileReader<GenericContainer> dataFileReader;
    private boolean isInputFileInitialized = false;

    public AvroGenericDeserializer(){}

    public AvroGenericDeserializer(Schema schema)
            throws IllegalArgumentException{
        this(schema, schema);
    }

    public AvroGenericDeserializer(Schema readerSchema, Schema writerSchema)
            throws IllegalArgumentException{
        this(readerSchema, writerSchema, false);
    }

    public AvroGenericDeserializer(Schema readerSchema, Schema writerSchema, boolean isSpecific){
        this(readerSchema, writerSchema, isSpecific, null);
    }

    public AvroGenericDeserializer(Schema readerSchema, Schema writerSchema, boolean isSpecific, File inputFile)
            throws IllegalArgumentException{
        super(readerSchema, writerSchema, isSpecific);
        this.inputFile = inputFile;
    }

    public AvroGenericDeserializer(String readerSchemaFilename, String writerSchemaFilename,
                                   boolean isSpecific, File inputFile)
            throws IOException, IllegalArgumentException{
        super(readerSchemaFilename, writerSchemaFilename, isSpecific);
        this.inputFile = inputFile;
    }

    @Override
    protected void initialize(Schema readerSchema, Schema writerSchema){
        super.initialize(readerSchema, writerSchema);
        if(isSpecific) {
            datumReader = new SpecificDatumReader<>(writerSchema, readerSchema);
        }
        else
            datumReader = new GenericDatumReader<>(writerSchema, readerSchema);

    }

    /**
     * Cleanup non java resources that were opened
     */
    public void close(){
        if(dataFileReader != null) try {
            dataFileReader.close();
            isInputFileInitialized = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deserialize a single record from its bytes. The bytes in this case
     * do not include the schema. The reader is expected to know and initialize
     * the correct schema using an orthogonal means
     *
     * @param bytes the bytes to be deserialized
     * @return a record based on the schema
     * @throws SchemaNotInitializedException
     * @throws IOException
     */
    public GenericContainer deserializeBytes(byte[] bytes)
            throws SchemaNotInitializedException, IOException {
        if(readerSchema == null || writerSchema == null)
            throw new SchemaNotInitializedException("Schema is null");
        if(bytes == null)
            throw new IllegalArgumentException("Bytes can not be null");
        decoder = decoderFactory.binaryDecoder(bytes, 0, bytes.length, decoder);
        return datumReader.read(null, decoder);
    }

    /**
     * Deserialize a single record from its JSON String.
     *
     * @param jsonString the JSON string to be deserialized. The jsongString in this case
     * do not include the schema. The reader is expected to know and initialize
     * the correct schema using an orthogonal means
     * @return a record based on the schema
     * @throws SchemaNotInitializedException
     * @throws IOException
     */
    public GenericContainer deserializeJson(String jsonString)
            throws SchemaNotInitializedException, IOException {
        if(readerSchema == null || writerSchema == null)
            throw new SchemaNotInitializedException("Schema is null");
        if(jsonString == null)
            throw new IllegalArgumentException("Bytes can not be null");
        jsonDecoder = new ReadableJsonDecoder(readerSchema, jsonString);
        return datumReader.read(null, jsonDecoder);
    }

    /**
     * Deserialize the next N records from an avroFile and return list records objects.
     * The serialized input file in this case includes a schema
     * typically written by avro during serialization. The deserialization schema can be different (an
     * evolution of the serialization scehma)
     * @param N the number of records to deserialize and return, 0 means all the remaining
     * @return a list of deserialized records according to the initialized schema; if the list size is < N
     *          this means that the underlying iterator reached the end
     * @throws SchemaNotInitializedException
     * @throws IOException
     */
    public List<GenericContainer> deserializeNRecordsFromFile(int N)
            throws SchemaNotInitializedException, IOException{
        int n = N > 0 ? N : Integer.MAX_VALUE;
        int counter = 0;
        if(readerSchema == null || writerSchema == null)
            throw new SchemaNotInitializedException("Schema is null");
        if(inputFile == null || !inputFile.exists())
            throw new IllegalArgumentException("File can not be null and must exist");

        if(!isInputFileInitialized) {
            dataFileReader = new DataFileReader<>(inputFile, datumReader);
            isInputFileInitialized = true;
        }
        List<GenericContainer> records = new ArrayList<>();

        while(dataFileReader.hasNext() && counter < n) {
            records.add(dataFileReader.next(null));
            counter ++;
        }
        return records;
    }


    /**
     * See {@link #deserializeNRecordsFromFile(int)} with N=1
     */
    public GenericContainer deserializeNextRecordFromFile()
            throws SchemaNotInitializedException, IOException{
        List<GenericContainer> records = deserializeNRecordsFromFile(1);
        if(records == null || records.isEmpty()) return null;
        return records.get(0);
    }

    public boolean isSpecific() {
        return isSpecific;
    }

    public void setIsSpecific(boolean isSpecific) {
        this.isSpecific = isSpecific;
    }

    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }
}
