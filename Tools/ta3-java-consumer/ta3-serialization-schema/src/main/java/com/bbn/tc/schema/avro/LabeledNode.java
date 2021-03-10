/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.bbn.tc.schema.avro;

import org.apache.avro.specific.SpecificData;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@SuppressWarnings("all")
/** A labeled node in a labeled property graph with properties */
@org.apache.avro.specific.AvroGenerated
public class LabeledNode extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 8745568330029243956L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"LabeledNode\",\"namespace\":\"com.bbn.tc.schema.avro\",\"doc\":\"A labeled node in a labeled property graph with properties\",\"fields\":[{\"name\":\"id\",\"type\":\"long\",\"doc\":\"Node's unique identifier.\"},{\"name\":\"label\",\"type\":{\"type\":\"enum\",\"name\":\"NODE_LABELS\",\"symbols\":[\"unitOfExecution\",\"artifact\",\"agent\"]},\"doc\":\"Node's label, role in the domain.\",\"default\":\"artifact\"},{\"name\":\"properties\",\"type\":[\"null\",{\"type\":\"map\",\"values\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}],\"doc\":\"Arbitrary key, value pairs describing the node.\",\"default\":null,\"order\":\"ignore\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<LabeledNode> ENCODER =
      new BinaryMessageEncoder<LabeledNode>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<LabeledNode> DECODER =
      new BinaryMessageDecoder<LabeledNode>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   */
  public static BinaryMessageDecoder<LabeledNode> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   */
  public static BinaryMessageDecoder<LabeledNode> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<LabeledNode>(MODEL$, SCHEMA$, resolver);
  }

  /** Serializes this LabeledNode to a ByteBuffer. */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /** Deserializes a LabeledNode from a ByteBuffer. */
  public static LabeledNode fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  /** Node's unique identifier. */
  @Deprecated public long id;
  /** Node's label, role in the domain. */
  @Deprecated public com.bbn.tc.schema.avro.NODE_LABELS label;
  /** Arbitrary key, value pairs describing the node. */
  @Deprecated public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> properties;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public LabeledNode() {}

  /**
   * All-args constructor.
   * @param id Node's unique identifier.
   * @param label Node's label, role in the domain.
   * @param properties Arbitrary key, value pairs describing the node.
   */
  public LabeledNode(java.lang.Long id, com.bbn.tc.schema.avro.NODE_LABELS label, java.util.Map<java.lang.CharSequence,java.lang.CharSequence> properties) {
    this.id = id;
    this.label = label;
    this.properties = properties;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return id;
    case 1: return label;
    case 2: return properties;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: id = (java.lang.Long)value$; break;
    case 1: label = (com.bbn.tc.schema.avro.NODE_LABELS)value$; break;
    case 2: properties = (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'id' field.
   * @return Node's unique identifier.
   */
  public java.lang.Long getId() {
    return id;
  }

  /**
   * Sets the value of the 'id' field.
   * Node's unique identifier.
   * @param value the value to set.
   */
  public void setId(java.lang.Long value) {
    this.id = value;
  }

  /**
   * Gets the value of the 'label' field.
   * @return Node's label, role in the domain.
   */
  public com.bbn.tc.schema.avro.NODE_LABELS getLabel() {
    return label;
  }

  /**
   * Sets the value of the 'label' field.
   * Node's label, role in the domain.
   * @param value the value to set.
   */
  public void setLabel(com.bbn.tc.schema.avro.NODE_LABELS value) {
    this.label = value;
  }

  /**
   * Gets the value of the 'properties' field.
   * @return Arbitrary key, value pairs describing the node.
   */
  public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getProperties() {
    return properties;
  }

  /**
   * Sets the value of the 'properties' field.
   * Arbitrary key, value pairs describing the node.
   * @param value the value to set.
   */
  public void setProperties(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
    this.properties = value;
  }

  /**
   * Creates a new LabeledNode RecordBuilder.
   * @return A new LabeledNode RecordBuilder
   */
  public static com.bbn.tc.schema.avro.LabeledNode.Builder newBuilder() {
    return new com.bbn.tc.schema.avro.LabeledNode.Builder();
  }

  /**
   * Creates a new LabeledNode RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new LabeledNode RecordBuilder
   */
  public static com.bbn.tc.schema.avro.LabeledNode.Builder newBuilder(com.bbn.tc.schema.avro.LabeledNode.Builder other) {
    return new com.bbn.tc.schema.avro.LabeledNode.Builder(other);
  }

  /**
   * Creates a new LabeledNode RecordBuilder by copying an existing LabeledNode instance.
   * @param other The existing instance to copy.
   * @return A new LabeledNode RecordBuilder
   */
  public static com.bbn.tc.schema.avro.LabeledNode.Builder newBuilder(com.bbn.tc.schema.avro.LabeledNode other) {
    return new com.bbn.tc.schema.avro.LabeledNode.Builder(other);
  }

  /**
   * RecordBuilder for LabeledNode instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<LabeledNode>
    implements org.apache.avro.data.RecordBuilder<LabeledNode> {

    /** Node's unique identifier. */
    private long id;
    /** Node's label, role in the domain. */
    private com.bbn.tc.schema.avro.NODE_LABELS label;
    /** Arbitrary key, value pairs describing the node. */
    private java.util.Map<java.lang.CharSequence,java.lang.CharSequence> properties;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(com.bbn.tc.schema.avro.LabeledNode.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.label)) {
        this.label = data().deepCopy(fields()[1].schema(), other.label);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.properties)) {
        this.properties = data().deepCopy(fields()[2].schema(), other.properties);
        fieldSetFlags()[2] = true;
      }
    }

    /**
     * Creates a Builder by copying an existing LabeledNode instance
     * @param other The existing instance to copy.
     */
    private Builder(com.bbn.tc.schema.avro.LabeledNode other) {
            super(SCHEMA$);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.label)) {
        this.label = data().deepCopy(fields()[1].schema(), other.label);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.properties)) {
        this.properties = data().deepCopy(fields()[2].schema(), other.properties);
        fieldSetFlags()[2] = true;
      }
    }

    /**
      * Gets the value of the 'id' field.
      * Node's unique identifier.
      * @return The value.
      */
    public java.lang.Long getId() {
      return id;
    }

    /**
      * Sets the value of the 'id' field.
      * Node's unique identifier.
      * @param value The value of 'id'.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.LabeledNode.Builder setId(long value) {
      validate(fields()[0], value);
      this.id = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'id' field has been set.
      * Node's unique identifier.
      * @return True if the 'id' field has been set, false otherwise.
      */
    public boolean hasId() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'id' field.
      * Node's unique identifier.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.LabeledNode.Builder clearId() {
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'label' field.
      * Node's label, role in the domain.
      * @return The value.
      */
    public com.bbn.tc.schema.avro.NODE_LABELS getLabel() {
      return label;
    }

    /**
      * Sets the value of the 'label' field.
      * Node's label, role in the domain.
      * @param value The value of 'label'.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.LabeledNode.Builder setLabel(com.bbn.tc.schema.avro.NODE_LABELS value) {
      validate(fields()[1], value);
      this.label = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'label' field has been set.
      * Node's label, role in the domain.
      * @return True if the 'label' field has been set, false otherwise.
      */
    public boolean hasLabel() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'label' field.
      * Node's label, role in the domain.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.LabeledNode.Builder clearLabel() {
      label = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'properties' field.
      * Arbitrary key, value pairs describing the node.
      * @return The value.
      */
    public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getProperties() {
      return properties;
    }

    /**
      * Sets the value of the 'properties' field.
      * Arbitrary key, value pairs describing the node.
      * @param value The value of 'properties'.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.LabeledNode.Builder setProperties(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
      validate(fields()[2], value);
      this.properties = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'properties' field has been set.
      * Arbitrary key, value pairs describing the node.
      * @return True if the 'properties' field has been set, false otherwise.
      */
    public boolean hasProperties() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'properties' field.
      * Arbitrary key, value pairs describing the node.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.LabeledNode.Builder clearProperties() {
      properties = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public LabeledNode build() {
      try {
        LabeledNode record = new LabeledNode();
        record.id = fieldSetFlags()[0] ? this.id : (java.lang.Long) defaultValue(fields()[0]);
        record.label = fieldSetFlags()[1] ? this.label : (com.bbn.tc.schema.avro.NODE_LABELS) defaultValue(fields()[1]);
        record.properties = fieldSetFlags()[2] ? this.properties : (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>) defaultValue(fields()[2]);
        return record;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<LabeledNode>
    WRITER$ = (org.apache.avro.io.DatumWriter<LabeledNode>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<LabeledNode>
    READER$ = (org.apache.avro.io.DatumReader<LabeledNode>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}