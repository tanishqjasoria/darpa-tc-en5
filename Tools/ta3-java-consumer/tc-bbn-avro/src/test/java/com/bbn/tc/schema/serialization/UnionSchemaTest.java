/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.bbn.tc.schema.utils.SchemaUtils;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class UnionSchemaTest {

    private static final Logger logger = Logger.getLogger(BaseTest.class);

    protected String readerSchemaFilename = "schemas/test/LabeledGraph.avsc";
    protected String writerSchemaFilename = "schemas/test/LabeledGraph.avsc";

    protected Schema readerSchema, writerSchema, nodeSchema, edgeSchema;

    protected AvroGenericSerializer serializer;
    protected AvroGenericDeserializer deserializer;

    private ByteArrayOutputStream out;
    private BinaryEncoder encoder;
    private BinaryDecoder decoder;
    private final DecoderFactory decoderFactory = DecoderFactory.get();

    @Before
    public void setup() throws Exception {
        // The schemas
        readerSchema = Utils.loadSchema(
                new File(this.
                        getClass().
                        getClassLoader().
                        getResource(readerSchemaFilename).
                        getFile()));
        writerSchema = Utils.loadSchema(
                new File(this.getClass().getClassLoader().getResource(writerSchemaFilename).getFile()));

        // These are only for records creation
        nodeSchema = SchemaUtils.getTypeSchemaByName(writerSchema, TestUtils.NODE_SCHEMA_FULLNAME, true);
        edgeSchema = SchemaUtils.getTypeSchemaByName(writerSchema, TestUtils.EDGE_SCHEMA_FULLNAME, true);

        // The serializers
        serializer = new AvroGenericSerializer(writerSchema);
        deserializer = new AvroGenericDeserializer(readerSchema, writerSchema);
        out = new ByteArrayOutputStream();
        encoder = EncoderFactory.get().directBinaryEncoder(out, null);
    }

    @Test
    public void test() {
        // First, create our nodes and edges.  Make sure that the edge node
        // has at least one property value set.
        GenericData.Record unitOfExecNode = TestUtils.createNode(1,
                "unitOfExecution", true, nodeSchema);
        logger.debug("created node " + unitOfExecNode.toString());
        GenericData.Record agentNode = TestUtils.createNode(2, "agent", true,
                nodeSchema);
        logger.debug("created node " + agentNode.toString());
        GenericData.Record edge1 = TestUtils.createEdge(unitOfExecNode, agentNode,
                "wasAssociatedWith", true, edgeSchema);
        logger.debug("created edge " + edge1.toString());

        // Serialize a node record.
        byte[] serializedNode = null;
        try {
            DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(writerSchema);
            datumWriter.write(agentNode, encoder);
            encoder.flush();
            serializedNode =  out.toByteArray();
            logger.debug("serialized agent bytes "+ Arrays.toString(serializedNode)
                    + ", len "+serializedNode.length);

            out.reset();

            datumWriter.write(unitOfExecNode, encoder);
            encoder.flush();
            serializedNode =  out.toByteArray();
            logger.debug("serialized uoe bytes "+ Arrays.toString(serializedNode)
                    + ", len "+serializedNode.length);

        } catch (IOException ioe) {
            ioe.printStackTrace();
            logger.error(ioe);
            assertTrue(false);
        }

        // Serialize the edge record.
        byte[] serializedEdge = null;
        try {
            DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(writerSchema);
            out = new ByteArrayOutputStream();
            encoder = EncoderFactory.get().directBinaryEncoder(out, null);
            datumWriter.write(edge1, encoder);
            encoder.flush();
            serializedEdge =  out.toByteArray();
            logger.debug("serialized bytes "+ Arrays.toString(serializedEdge)
                    + ", len "+serializedEdge.length);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            logger.error(ioe);
            assertTrue(false);
        }

        // Deserialize the node record
        DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(writerSchema, readerSchema);
        decoder = decoderFactory.binaryDecoder(serializedNode, decoder);
        try {
            GenericRecord node = datumReader.read(null, decoder);
            logger.debug(node.toString());
            assertTrue(node.toString().equals(unitOfExecNode.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Deserialize the edge record.
        datumReader = new GenericDatumReader<GenericRecord>(writerSchema, readerSchema);
        decoder = decoderFactory.binaryDecoder(serializedEdge, decoder);
        try {
            GenericRecord edge = datumReader.read(null, decoder);
            logger.debug(edge.toString());
            assertTrue(edge.toString().equals(edge1.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
