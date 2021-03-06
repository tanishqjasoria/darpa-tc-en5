/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.bbn.tc.schema.avro.cdm20;

import org.apache.avro.specific.SpecificData;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@SuppressWarnings("all")
/** *  Objects, in general, represent data sources and sinks which
     *  could include sockets, files, memory, and any data in general
     *  that can be an input and/or output to an event.  This record
     *  is intended to be abstract i.e., one should not instantiate an
     *  Object but rather instantiate one of its sub types (ie,
     *  encapsulating records) FileObject, UnnamedPipeObject,
     *  RegistryKeyObject, NetFlowObject, MemoryObject, or
     *  SrcSinkObject. */
@org.apache.avro.specific.AvroGenerated
public class AbstractObject extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 227021588454675197L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"AbstractObject\",\"namespace\":\"com.bbn.tc.schema.avro.cdm20\",\"doc\":\"*  Objects, in general, represent data sources and sinks which\\n     *  could include sockets, files, memory, and any data in general\\n     *  that can be an input and/or output to an event.  This record\\n     *  is intended to be abstract i.e., one should not instantiate an\\n     *  Object but rather instantiate one of its sub types (ie,\\n     *  encapsulating records) FileObject, UnnamedPipeObject,\\n     *  RegistryKeyObject, NetFlowObject, MemoryObject, or\\n     *  SrcSinkObject.\",\"fields\":[{\"name\":\"permission\",\"type\":[\"null\",{\"type\":\"fixed\",\"name\":\"SHORT\",\"size\":2}],\"doc\":\"Permission bits defined over the object (Optional)\",\"default\":null},{\"name\":\"epoch\",\"type\":[\"null\",\"int\"],\"doc\":\"* Used to track when an object is deleted and a new one is\\n         * created with the same identifier. This is useful for when\\n         * UUIDs are based on something not likely to be unique, such\\n         * as file path.\",\"default\":null},{\"name\":\"properties\",\"type\":[\"null\",{\"type\":\"map\",\"values\":\"string\"}],\"doc\":\"* Arbitrary key, value pairs describing the entity.\\n         * NOTE: This attribute is meant as a temporary place holder for items that\\n         * will become first-class attributes in the next CDM version.\",\"default\":null,\"order\":\"ignore\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<AbstractObject> ENCODER =
      new BinaryMessageEncoder<AbstractObject>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<AbstractObject> DECODER =
      new BinaryMessageDecoder<AbstractObject>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   */
  public static BinaryMessageDecoder<AbstractObject> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   */
  public static BinaryMessageDecoder<AbstractObject> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<AbstractObject>(MODEL$, SCHEMA$, resolver);
  }

  /** Serializes this AbstractObject to a ByteBuffer. */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /** Deserializes a AbstractObject from a ByteBuffer. */
  public static AbstractObject fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  /** Permission bits defined over the object (Optional) */
  @Deprecated public com.bbn.tc.schema.avro.cdm20.SHORT permission;
  /** * Used to track when an object is deleted and a new one is
         * created with the same identifier. This is useful for when
         * UUIDs are based on something not likely to be unique, such
         * as file path. */
  @Deprecated public java.lang.Integer epoch;
  /** * Arbitrary key, value pairs describing the entity.
         * NOTE: This attribute is meant as a temporary place holder for items that
         * will become first-class attributes in the next CDM version. */
  @Deprecated public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> properties;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public AbstractObject() {}

  /**
   * All-args constructor.
   * @param permission Permission bits defined over the object (Optional)
   * @param epoch * Used to track when an object is deleted and a new one is
         * created with the same identifier. This is useful for when
         * UUIDs are based on something not likely to be unique, such
         * as file path.
   * @param properties * Arbitrary key, value pairs describing the entity.
         * NOTE: This attribute is meant as a temporary place holder for items that
         * will become first-class attributes in the next CDM version.
   */
  public AbstractObject(com.bbn.tc.schema.avro.cdm20.SHORT permission, java.lang.Integer epoch, java.util.Map<java.lang.CharSequence,java.lang.CharSequence> properties) {
    this.permission = permission;
    this.epoch = epoch;
    this.properties = properties;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return permission;
    case 1: return epoch;
    case 2: return properties;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: permission = (com.bbn.tc.schema.avro.cdm20.SHORT)value$; break;
    case 1: epoch = (java.lang.Integer)value$; break;
    case 2: properties = (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'permission' field.
   * @return Permission bits defined over the object (Optional)
   */
  public com.bbn.tc.schema.avro.cdm20.SHORT getPermission() {
    return permission;
  }

  /**
   * Sets the value of the 'permission' field.
   * Permission bits defined over the object (Optional)
   * @param value the value to set.
   */
  public void setPermission(com.bbn.tc.schema.avro.cdm20.SHORT value) {
    this.permission = value;
  }

  /**
   * Gets the value of the 'epoch' field.
   * @return * Used to track when an object is deleted and a new one is
         * created with the same identifier. This is useful for when
         * UUIDs are based on something not likely to be unique, such
         * as file path.
   */
  public java.lang.Integer getEpoch() {
    return epoch;
  }

  /**
   * Sets the value of the 'epoch' field.
   * * Used to track when an object is deleted and a new one is
         * created with the same identifier. This is useful for when
         * UUIDs are based on something not likely to be unique, such
         * as file path.
   * @param value the value to set.
   */
  public void setEpoch(java.lang.Integer value) {
    this.epoch = value;
  }

  /**
   * Gets the value of the 'properties' field.
   * @return * Arbitrary key, value pairs describing the entity.
         * NOTE: This attribute is meant as a temporary place holder for items that
         * will become first-class attributes in the next CDM version.
   */
  public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getProperties() {
    return properties;
  }

  /**
   * Sets the value of the 'properties' field.
   * * Arbitrary key, value pairs describing the entity.
         * NOTE: This attribute is meant as a temporary place holder for items that
         * will become first-class attributes in the next CDM version.
   * @param value the value to set.
   */
  public void setProperties(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
    this.properties = value;
  }

  /**
   * Creates a new AbstractObject RecordBuilder.
   * @return A new AbstractObject RecordBuilder
   */
  public static com.bbn.tc.schema.avro.cdm20.AbstractObject.Builder newBuilder() {
    return new com.bbn.tc.schema.avro.cdm20.AbstractObject.Builder();
  }

  /**
   * Creates a new AbstractObject RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new AbstractObject RecordBuilder
   */
  public static com.bbn.tc.schema.avro.cdm20.AbstractObject.Builder newBuilder(com.bbn.tc.schema.avro.cdm20.AbstractObject.Builder other) {
    return new com.bbn.tc.schema.avro.cdm20.AbstractObject.Builder(other);
  }

  /**
   * Creates a new AbstractObject RecordBuilder by copying an existing AbstractObject instance.
   * @param other The existing instance to copy.
   * @return A new AbstractObject RecordBuilder
   */
  public static com.bbn.tc.schema.avro.cdm20.AbstractObject.Builder newBuilder(com.bbn.tc.schema.avro.cdm20.AbstractObject other) {
    return new com.bbn.tc.schema.avro.cdm20.AbstractObject.Builder(other);
  }

  /**
   * RecordBuilder for AbstractObject instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<AbstractObject>
    implements org.apache.avro.data.RecordBuilder<AbstractObject> {

    /** Permission bits defined over the object (Optional) */
    private com.bbn.tc.schema.avro.cdm20.SHORT permission;
    /** * Used to track when an object is deleted and a new one is
         * created with the same identifier. This is useful for when
         * UUIDs are based on something not likely to be unique, such
         * as file path. */
    private java.lang.Integer epoch;
    /** * Arbitrary key, value pairs describing the entity.
         * NOTE: This attribute is meant as a temporary place holder for items that
         * will become first-class attributes in the next CDM version. */
    private java.util.Map<java.lang.CharSequence,java.lang.CharSequence> properties;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(com.bbn.tc.schema.avro.cdm20.AbstractObject.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.permission)) {
        this.permission = data().deepCopy(fields()[0].schema(), other.permission);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.epoch)) {
        this.epoch = data().deepCopy(fields()[1].schema(), other.epoch);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.properties)) {
        this.properties = data().deepCopy(fields()[2].schema(), other.properties);
        fieldSetFlags()[2] = true;
      }
    }

    /**
     * Creates a Builder by copying an existing AbstractObject instance
     * @param other The existing instance to copy.
     */
    private Builder(com.bbn.tc.schema.avro.cdm20.AbstractObject other) {
            super(SCHEMA$);
      if (isValidValue(fields()[0], other.permission)) {
        this.permission = data().deepCopy(fields()[0].schema(), other.permission);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.epoch)) {
        this.epoch = data().deepCopy(fields()[1].schema(), other.epoch);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.properties)) {
        this.properties = data().deepCopy(fields()[2].schema(), other.properties);
        fieldSetFlags()[2] = true;
      }
    }

    /**
      * Gets the value of the 'permission' field.
      * Permission bits defined over the object (Optional)
      * @return The value.
      */
    public com.bbn.tc.schema.avro.cdm20.SHORT getPermission() {
      return permission;
    }

    /**
      * Sets the value of the 'permission' field.
      * Permission bits defined over the object (Optional)
      * @param value The value of 'permission'.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.AbstractObject.Builder setPermission(com.bbn.tc.schema.avro.cdm20.SHORT value) {
      validate(fields()[0], value);
      this.permission = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'permission' field has been set.
      * Permission bits defined over the object (Optional)
      * @return True if the 'permission' field has been set, false otherwise.
      */
    public boolean hasPermission() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'permission' field.
      * Permission bits defined over the object (Optional)
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.AbstractObject.Builder clearPermission() {
      permission = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'epoch' field.
      * * Used to track when an object is deleted and a new one is
         * created with the same identifier. This is useful for when
         * UUIDs are based on something not likely to be unique, such
         * as file path.
      * @return The value.
      */
    public java.lang.Integer getEpoch() {
      return epoch;
    }

    /**
      * Sets the value of the 'epoch' field.
      * * Used to track when an object is deleted and a new one is
         * created with the same identifier. This is useful for when
         * UUIDs are based on something not likely to be unique, such
         * as file path.
      * @param value The value of 'epoch'.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.AbstractObject.Builder setEpoch(java.lang.Integer value) {
      validate(fields()[1], value);
      this.epoch = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'epoch' field has been set.
      * * Used to track when an object is deleted and a new one is
         * created with the same identifier. This is useful for when
         * UUIDs are based on something not likely to be unique, such
         * as file path.
      * @return True if the 'epoch' field has been set, false otherwise.
      */
    public boolean hasEpoch() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'epoch' field.
      * * Used to track when an object is deleted and a new one is
         * created with the same identifier. This is useful for when
         * UUIDs are based on something not likely to be unique, such
         * as file path.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.AbstractObject.Builder clearEpoch() {
      epoch = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'properties' field.
      * * Arbitrary key, value pairs describing the entity.
         * NOTE: This attribute is meant as a temporary place holder for items that
         * will become first-class attributes in the next CDM version.
      * @return The value.
      */
    public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getProperties() {
      return properties;
    }

    /**
      * Sets the value of the 'properties' field.
      * * Arbitrary key, value pairs describing the entity.
         * NOTE: This attribute is meant as a temporary place holder for items that
         * will become first-class attributes in the next CDM version.
      * @param value The value of 'properties'.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.AbstractObject.Builder setProperties(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
      validate(fields()[2], value);
      this.properties = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'properties' field has been set.
      * * Arbitrary key, value pairs describing the entity.
         * NOTE: This attribute is meant as a temporary place holder for items that
         * will become first-class attributes in the next CDM version.
      * @return True if the 'properties' field has been set, false otherwise.
      */
    public boolean hasProperties() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'properties' field.
      * * Arbitrary key, value pairs describing the entity.
         * NOTE: This attribute is meant as a temporary place holder for items that
         * will become first-class attributes in the next CDM version.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.AbstractObject.Builder clearProperties() {
      properties = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AbstractObject build() {
      try {
        AbstractObject record = new AbstractObject();
        record.permission = fieldSetFlags()[0] ? this.permission : (com.bbn.tc.schema.avro.cdm20.SHORT) defaultValue(fields()[0]);
        record.epoch = fieldSetFlags()[1] ? this.epoch : (java.lang.Integer) defaultValue(fields()[1]);
        record.properties = fieldSetFlags()[2] ? this.properties : (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>) defaultValue(fields()[2]);
        return record;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<AbstractObject>
    WRITER$ = (org.apache.avro.io.DatumWriter<AbstractObject>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<AbstractObject>
    READER$ = (org.apache.avro.io.DatumReader<AbstractObject>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}
