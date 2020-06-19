/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

import com.bbn.tc.schema.SchemaNotInitializedException;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Serialize an avro schema to a file or to a byte[] or to a json string
 * This serializer allows serializing either a GenericData.REcord (without schema compilation)
 *   or a SpecificREcord (with schema compilation). It does the right thing underneath
 *   automatically
 *
 * One mode of serializer allow Avro binary file serialization by speifying the outputFile
 *   to serialize to. This automatically makes it a file serializer. We also support file
 *   serialization to json, in this case each record is serialized to a json string and
 *   written to a file.
 *
 * **This class is NOT thread safe**
 *
 * Created by jkhoury
 */
public class AvroGenericSerializer<T extends GenericContainer> extends AvroGenericAbstractDeSerializer {

    private final Logger logger = Logger.getLogger(AvroGenericSerializer.class);
    private ByteArrayOutputStream out;
    private BinaryEncoder encoder;
    private ReadableJsonEncoder jsonEncoder;
    private DatumWriter<Object> datumWriter;
    private DataFileWriter fileWriter;
    private BufferedWriter bufferedWriter;
    private File outputFile;
    private boolean isJsonFileSerializer;
    private boolean isFileWriterInitialized = false;

    public AvroGenericSerializer(){super();}

    public AvroGenericSerializer(Schema schema) throws IllegalArgumentException{
        super(schema);
    }

    public AvroGenericSerializer(String schemaFileName) throws IOException, IllegalArgumentException{
        super(schemaFileName);
    }

    public AvroGenericSerializer(String schemaFileName, File outputFile) throws IOException, IllegalArgumentException{
        this(schemaFileName, false, outputFile);
    }

    public AvroGenericSerializer(String schemaFileName, boolean isSpecific, File outputFile)
            throws IOException, IllegalArgumentException{
        this(schemaFileName, isSpecific, outputFile, false);
    }

    public AvroGenericSerializer(String schemaFileName, boolean isSpecific, File outputFile, boolean isJsonFileSerializer)
            throws IOException, IllegalArgumentException{
        super(schemaFileName, schemaFileName, isSpecific);
        this.outputFile = outputFile;
        this.isJsonFileSerializer = isJsonFileSerializer;
    }

    public AvroGenericSerializer(Schema schema, File outputFile) throws IllegalArgumentException {
        this(schema, false, outputFile);
    }

    public AvroGenericSerializer(Schema schema, boolean isSpecific, File outputFile) throws IllegalArgumentException{
        this(schema, isSpecific, outputFile, false);
    }

    public AvroGenericSerializer(Schema schema, boolean isSpecific, File outputFile, boolean isJsonFileSerializer)
            throws IllegalArgumentException{
        super(schema, schema, isSpecific);
        this.outputFile = outputFile;
        this.isJsonFileSerializer = isJsonFileSerializer;
    }

    @Override
    protected void initialize(Schema readerSchema, Schema writerSchema){
        super.initialize(readerSchema, writerSchema);
        out = new ByteArrayOutputStream();
        encoder = EncoderFactory.get().directBinaryEncoder(out, null);
        if(isSpecific)
            datumWriter = new SpecificDatumWriter<>(writerSchema);
        else
            datumWriter = new GenericDatumWriter<>(writerSchema);
    }

    /**
     * Cleanup non java resources that were opened
     */
    public void close(){
        try {
            if(out != null) out.close();
            if(fileWriter != null) fileWriter.close();
            if(bufferedWriter !=null) bufferedWriter.close();
            isFileWriterInitialized = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Serialize the record to a byte[]
     * Note that in this case, the serialized bytes do not include
     * the schema. Hence, the reader must know what schema to use.
     * The record can be a GenericRecord (without schema compilation) or a SpecificRecord (compiled schema)
     *
     *
     * @param record the record to serialize
     * @return the compact byte [] representation (note that avro does not include typing, that is encoded
     *  separately in the schema)
     * @throws IOException see {@link DatumWriter}
     * @throws IllegalArgumentException if record is null, has null schema, or is not an instance of
     *  GenericRecord or SpecificRecord
     */
    public byte[] serializeToBytes(T record)
                throws IOException, IllegalArgumentException{
        checkRecord(record);
        try {
            // We could have checked the record type at runtime to decide on specific vs generic
            // but I am avoiding these runtime checks for now
            datumWriter.write(record, encoder);
            encoder.flush();
            return out.toByteArray();
        } finally{
            if(out!=null) out.reset();
        }
    }

    /**
     * Serialize the record to a Json string
     * Note that in this case, the serialized string does not include
     * the schema. Hence, the reader must know what schema to use.
     * The record can be a GenericRecord (without schema compilation)
     *   or a SpecificRecord (compiled schema)
     *
     *
     * @param record the record to serialize
     * @return the serialized json string representation
     * @throws IOException see {@link DatumWriter}
     * @throws IllegalArgumentException if record is null, has null schema, or is not an instance of
     *  GenericRecord or SpecificRecord
     */
    public String serializeToJson(T record, boolean pretty)
            throws IOException, IllegalArgumentException{
        checkRecord(record);
        try {
            // need to set at most once since schema is not supposed to change
            if(jsonEncoder == null)
                jsonEncoder = new ReadableJsonEncoder(writerSchema, out, pretty);
            // We could have checked the record type at runtime to decide on specific vs generic
            // but I am avoiding these runtime checks for now
            datumWriter.write(record, jsonEncoder);
            jsonEncoder.flush();
            return new String(out.toByteArray());
        } finally{
            if(out!=null) out.reset();
        }
    }

    private void checkRecord(T record)
        throws IllegalArgumentException{
        if(record == null)
            throw new IllegalArgumentException("Can not serialize null record.");
        if(! (record instanceof GenericRecord) && ! (record instanceof SpecificRecord))
            throw new IllegalArgumentException("Unknown record type, record must be either a Generic or " +
                    "a SpecificRecord.");
        if(record.getSchema() == null)
            throw new IllegalArgumentException("Can not serialize record with null schema");
    }

    /**
     * Serialize a single record to file
     *
     * @see #serializeToFile(List)
     * @param record the record to serialize
     * @throws SchemaNotInitializedException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public void serializeToFile(T record)
            throws SchemaNotInitializedException, IllegalArgumentException, IOException {
        List<T> records = new ArrayList<>();
        records.add(record);
        serializeToFile(records);
    }

    /**
     * Serialize either binary compact format or json string to a file.
     *
     * In case of Avro binary, the output file will include the record schema (self-descriptive) independent of the
     * data i.e., the data does not include typing.
     *
     * In case of Json, each record is converted to json string and appended to the file.
     *
     * The records can be of type GenericRecord (without schema compilation) or a SpecificRecord (compiled schema)
     *
     * @param records the records to serialize
     * @throws SchemaNotInitializedException
     * @throws IllegalArgumentException see {@link #serializeToBytes(T)}
     * @throws IOException
     */
    public void serializeToFile(List<T> records)
            throws SchemaNotInitializedException, IllegalArgumentException, IOException {
        if(writerSchema == null)
            throw new SchemaNotInitializedException("Schema is null");
        if(outputFile == null)
            throw new IllegalStateException("Instance not correctly initialized, outputFile can not be null");
        if(records == null)
            throw new IllegalArgumentException("Can not serialize null records");

        if(!isFileWriterInitialized) {
            if(isJsonFileSerializer){
                bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            }else { // Avro binary
                fileWriter = new DataFileWriter(datumWriter);
                fileWriter.create(writerSchema, outputFile);
            }
            isFileWriterInitialized = true;
        }
        if(isJsonFileSerializer) {
            for (T record : records) {
                checkRecord(record);
                bufferedWriter.append(serializeToJson(record, false));
            }
            bufferedWriter.flush();
        }else {
            for (T record : records) {
                checkRecord(record);
                fileWriter.append(record);
            }
            fileWriter.flush();
        }

    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }
}
