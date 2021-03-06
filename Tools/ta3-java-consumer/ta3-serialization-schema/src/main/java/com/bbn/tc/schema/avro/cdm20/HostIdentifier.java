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
/** Host identifier, such as serial number, IMEI number */
@org.apache.avro.specific.AvroGenerated
public class HostIdentifier extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 9218812333693580382L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"HostIdentifier\",\"namespace\":\"com.bbn.tc.schema.avro.cdm20\",\"doc\":\"Host identifier, such as serial number, IMEI number\",\"fields\":[{\"name\":\"idType\",\"type\":\"string\"},{\"name\":\"idValue\",\"type\":\"string\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<HostIdentifier> ENCODER =
      new BinaryMessageEncoder<HostIdentifier>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<HostIdentifier> DECODER =
      new BinaryMessageDecoder<HostIdentifier>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   */
  public static BinaryMessageDecoder<HostIdentifier> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   */
  public static BinaryMessageDecoder<HostIdentifier> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<HostIdentifier>(MODEL$, SCHEMA$, resolver);
  }

  /** Serializes this HostIdentifier to a ByteBuffer. */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /** Deserializes a HostIdentifier from a ByteBuffer. */
  public static HostIdentifier fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  @Deprecated public java.lang.CharSequence idType;
  @Deprecated public java.lang.CharSequence idValue;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public HostIdentifier() {}

  /**
   * All-args constructor.
   * @param idType The new value for idType
   * @param idValue The new value for idValue
   */
  public HostIdentifier(java.lang.CharSequence idType, java.lang.CharSequence idValue) {
    this.idType = idType;
    this.idValue = idValue;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return idType;
    case 1: return idValue;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: idType = (java.lang.CharSequence)value$; break;
    case 1: idValue = (java.lang.CharSequence)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'idType' field.
   * @return The value of the 'idType' field.
   */
  public java.lang.CharSequence getIdType() {
    return idType;
  }

  /**
   * Sets the value of the 'idType' field.
   * @param value the value to set.
   */
  public void setIdType(java.lang.CharSequence value) {
    this.idType = value;
  }

  /**
   * Gets the value of the 'idValue' field.
   * @return The value of the 'idValue' field.
   */
  public java.lang.CharSequence getIdValue() {
    return idValue;
  }

  /**
   * Sets the value of the 'idValue' field.
   * @param value the value to set.
   */
  public void setIdValue(java.lang.CharSequence value) {
    this.idValue = value;
  }

  /**
   * Creates a new HostIdentifier RecordBuilder.
   * @return A new HostIdentifier RecordBuilder
   */
  public static com.bbn.tc.schema.avro.cdm20.HostIdentifier.Builder newBuilder() {
    return new com.bbn.tc.schema.avro.cdm20.HostIdentifier.Builder();
  }

  /**
   * Creates a new HostIdentifier RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new HostIdentifier RecordBuilder
   */
  public static com.bbn.tc.schema.avro.cdm20.HostIdentifier.Builder newBuilder(com.bbn.tc.schema.avro.cdm20.HostIdentifier.Builder other) {
    return new com.bbn.tc.schema.avro.cdm20.HostIdentifier.Builder(other);
  }

  /**
   * Creates a new HostIdentifier RecordBuilder by copying an existing HostIdentifier instance.
   * @param other The existing instance to copy.
   * @return A new HostIdentifier RecordBuilder
   */
  public static com.bbn.tc.schema.avro.cdm20.HostIdentifier.Builder newBuilder(com.bbn.tc.schema.avro.cdm20.HostIdentifier other) {
    return new com.bbn.tc.schema.avro.cdm20.HostIdentifier.Builder(other);
  }

  /**
   * RecordBuilder for HostIdentifier instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<HostIdentifier>
    implements org.apache.avro.data.RecordBuilder<HostIdentifier> {

    private java.lang.CharSequence idType;
    private java.lang.CharSequence idValue;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(com.bbn.tc.schema.avro.cdm20.HostIdentifier.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.idType)) {
        this.idType = data().deepCopy(fields()[0].schema(), other.idType);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.idValue)) {
        this.idValue = data().deepCopy(fields()[1].schema(), other.idValue);
        fieldSetFlags()[1] = true;
      }
    }

    /**
     * Creates a Builder by copying an existing HostIdentifier instance
     * @param other The existing instance to copy.
     */
    private Builder(com.bbn.tc.schema.avro.cdm20.HostIdentifier other) {
            super(SCHEMA$);
      if (isValidValue(fields()[0], other.idType)) {
        this.idType = data().deepCopy(fields()[0].schema(), other.idType);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.idValue)) {
        this.idValue = data().deepCopy(fields()[1].schema(), other.idValue);
        fieldSetFlags()[1] = true;
      }
    }

    /**
      * Gets the value of the 'idType' field.
      * @return The value.
      */
    public java.lang.CharSequence getIdType() {
      return idType;
    }

    /**
      * Sets the value of the 'idType' field.
      * @param value The value of 'idType'.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.HostIdentifier.Builder setIdType(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.idType = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'idType' field has been set.
      * @return True if the 'idType' field has been set, false otherwise.
      */
    public boolean hasIdType() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'idType' field.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.HostIdentifier.Builder clearIdType() {
      idType = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'idValue' field.
      * @return The value.
      */
    public java.lang.CharSequence getIdValue() {
      return idValue;
    }

    /**
      * Sets the value of the 'idValue' field.
      * @param value The value of 'idValue'.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.HostIdentifier.Builder setIdValue(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.idValue = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'idValue' field has been set.
      * @return True if the 'idValue' field has been set, false otherwise.
      */
    public boolean hasIdValue() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'idValue' field.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.HostIdentifier.Builder clearIdValue() {
      idValue = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public HostIdentifier build() {
      try {
        HostIdentifier record = new HostIdentifier();
        record.idType = fieldSetFlags()[0] ? this.idType : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.idValue = fieldSetFlags()[1] ? this.idValue : (java.lang.CharSequence) defaultValue(fields()[1]);
        return record;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<HostIdentifier>
    WRITER$ = (org.apache.avro.io.DatumWriter<HostIdentifier>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<HostIdentifier>
    READER$ = (org.apache.avro.io.DatumReader<HostIdentifier>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}
