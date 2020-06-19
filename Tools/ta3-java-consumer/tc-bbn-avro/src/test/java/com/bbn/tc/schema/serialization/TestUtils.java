/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization;

import com.bbn.tc.schema.avro.cdm20.*;
import com.bbn.tc.schema.avro.cdm20.UUID;
import com.bbn.tc.schema.utils.SchemaUtils;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.util.Utf8;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Utilities for testing
 * @author jkhoury
 */
public class TestUtils {

    public static final char SUB_RECORD_SEPARATOR = ',';
    public static final char SEP = ':';
    public static final String NODE_SCHEMA_FULLNAME = "com.bbn.tc.schema.avro.LabeledNode";
    public static final String EDGE_SCHEMA_FULLNAME = "com.bbn.tc.schema.avro.LabeledEdge";
    final static Random random;

    static {
        random = new Random();
    }

    public static Event generateEvent() {
        EventType[] eventTypes = EventType.values();
        InstrumentationSource[] instrumentationSources = InstrumentationSource.values();

        // flip a coin, and either create a simple event (no parameters) or one with tagged parameters
        UUID uuid = SchemaUtils.toUUID(random.nextLong());
        long sequence = random.nextLong();
        EventType type = eventTypes[random.nextInt(eventTypes.length)];
        UUID subject = SchemaUtils.toUUID(random.nextLong());
        UUID predicateObject = SchemaUtils.toUUID(random.nextLong());
        InstrumentationSource source = instrumentationSources[random.nextInt(instrumentationSources.length)];
        long timestampNanos = random.nextLong();
        Event.Builder builder = Event.newBuilder()
                .setUuid(uuid)
                .setSequence(sequence)
                .setType(type)
                .setThreadId(random.nextInt())
                .setSubject(subject)
                .setPredicateObject(predicateObject)
                .setTimestampNanos(timestampNanos);

        // create tagged params
        List<Value> vals = new ArrayList<>();

        // first value is an int[10] (10 element array of 4 bytes per element)
        byte[] val1Bytes = ByteBuffer.allocate(40).putInt(random.nextInt()).array();
        List<TagRunLengthTuple> val1Tag = new ArrayList<>();
        val1Tag.add(new TagRunLengthTuple(5, SchemaUtils.toUUID(random.nextLong())));
        val1Tag.add(new TagRunLengthTuple(5, SchemaUtils.toUUID(random.nextLong())));
        Value value1 = Value.newBuilder()
                .setType(ValueType.VALUE_TYPE_SRC)
                .setValueDataType(ValueDataType.VALUE_DATA_TYPE_INT)
                .setSize(10) // int[10] even though it is converted to a 40 byte array underneath
                .setValueBytes(ByteBuffer.wrap(val1Bytes)) // this is the actual bytes BIG_ENDIAN, each char is 4 bytes
                .setTag(val1Tag).build();
        vals.add(value1);

        // second value is a long
        byte[] val2Bytes = ByteBuffer.allocate(8).putLong(random.nextLong()).array();
        List<TagRunLengthTuple> val2Tag = new ArrayList<>();
        val2Tag.add(new TagRunLengthTuple(1, SchemaUtils.toUUID(random.nextLong())));
        Value value2 = Value.newBuilder()
                .setType(ValueType.VALUE_TYPE_SINK)
                .setName("val2Name")
                .setValueDataType(ValueDataType.VALUE_DATA_TYPE_LONG) // a long value
                .setSize(0) // a primitive long value has size 0
                .setValueBytes(ByteBuffer.wrap(val2Bytes))
                .setTag(val2Tag).build();
        vals.add(value2);

        builder.setParameters(vals);
        Event event = builder.build();
        return event;
    }

    public static GenericData.Record createNode(long id, String label, Schema nodeSchema) {
        return createNode(id, label, false, nodeSchema);
    }

    public static GenericData.Record createNode(long id, String label, boolean addTs, Schema nodeSchema) {
        GenericRecordBuilder builder = new GenericRecordBuilder(nodeSchema);
        builder.set("id", id).set("label", new GenericData.EnumSymbol(nodeSchema, label));
        if(addTs) {
            Map<String, String> properties = new HashMap<>();
            properties.put("timestamp", "" + System.currentTimeMillis());
            builder.set("properties", properties);
        }
        return builder.build();
    }

    public static GenericData.Record createEdge(GenericData.Record from, GenericData.Record to,
                                                String label, Schema edgeSchema) {
        return  createEdge(from, to, label, false, edgeSchema);
    }

    public static GenericData.Record createEdge(GenericData.Record from, GenericData.Record to,
                                                String label, boolean addTs, Schema edgeSchema) {
        GenericRecordBuilder builder = new GenericRecordBuilder(edgeSchema);
        builder.set("label", new GenericData.EnumSymbol(edgeSchema, label)).set("fromNode", from).set("toNode", to);
        if(addTs) {
            Map<String, String> properties = new HashMap<>();
            properties.put("timestamp", "" + System.currentTimeMillis());
            builder.set("properties", properties);
        }
        return builder.build();
    }

    public static String prettyEdge(GenericData.Record record){

        if(record == null) return null;
        if(record.getSchema() == null) return record.toString();
        StringBuffer buffer = new StringBuffer();
        String label = record.get("label").toString();
        if(record.getSchema().getFullName().equals(EDGE_SCHEMA_FULLNAME)){
            GenericData.Record from = (GenericData.Record)record.get("fromNode");
            GenericData.Record to = (GenericData.Record)record.get("toNode");
            buffer.append("(").append(prettyNode(from)).append(")")
                    .append(" ===").append(label).append("===> ")
                    .append("(").append(prettyNode(to)).append(")");
            return buffer.toString();

        }else if(record.getSchema().getFullName().equals(NODE_SCHEMA_FULLNAME)){
            return prettyNode(record);
        }
        return record.toString();
    }

    public static String prettyNode(GenericData.Record node){
        if(node == null) return null;
        if(!node.getSchema().getFullName().equals(NODE_SCHEMA_FULLNAME))
            throw new IllegalArgumentException("Bad node. Expecting node, got "+node.getSchema().getName());
        String label = node.get("label").toString();
        StringBuffer buffer = new StringBuffer();
        if(label == null || node.get("properties") == null) return node.toString();
        buffer.append(label).append(SEP);
        Map<Utf8, String> properties = (HashMap<Utf8, String>)node.get("properties");
        if(label.equals("Artifact")){
            String path = properties.get(new Utf8("path"));
            if(path !=null) {
                if (path.length() > 30) {
                    buffer.append(path.substring(0, 15)).append("...").append(path.substring(path.length() - 15));
                }else{
                    buffer.append(path);
                }
            }else{
                buffer.append(properties.get(new Utf8("filename")));
            }
        }else if(label.equals("Process")){
            buffer.append("[").append("uid").append(SEP).append(properties.get(new Utf8("uid"))).append(",").append("pid").append(SEP)
                    .append(properties.get(new Utf8("pid")));
            if(properties.get(new Utf8("user")) != null) buffer.append(",user").append(SEP).append(properties.get(new Utf8("user")));
            if(properties.get(new Utf8("pidname")) != null) buffer.append(",pidname").append(SEP).append(properties.get(new Utf8("pidname")));
            buffer.append("]");
        }
        return buffer.toString();
    }
}
