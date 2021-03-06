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
/** * Represents a file on the file system. Instantiates an AbstractObject. */
@org.apache.avro.specific.AvroGenerated
public class FileObject extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -5988967448361149303L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"FileObject\",\"namespace\":\"com.bbn.tc.schema.avro.cdm20\",\"doc\":\"* Represents a file on the file system. Instantiates an AbstractObject.\",\"fields\":[{\"name\":\"uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"UUID\",\"doc\":\"* A host MUST NOT reuse UUIDs at all within their system, even\\n     * across restarts, and definitely not for 2 distinct objects\",\"size\":16},\"doc\":\"Universally unique identifier for the object\"},{\"name\":\"baseObject\",\"type\":{\"type\":\"record\",\"name\":\"AbstractObject\",\"doc\":\"*  Objects, in general, represent data sources and sinks which\\n     *  could include sockets, files, memory, and any data in general\\n     *  that can be an input and/or output to an event.  This record\\n     *  is intended to be abstract i.e., one should not instantiate an\\n     *  Object but rather instantiate one of its sub types (ie,\\n     *  encapsulating records) FileObject, UnnamedPipeObject,\\n     *  RegistryKeyObject, NetFlowObject, MemoryObject, or\\n     *  SrcSinkObject.\",\"fields\":[{\"name\":\"permission\",\"type\":[\"null\",{\"type\":\"fixed\",\"name\":\"SHORT\",\"size\":2}],\"doc\":\"Permission bits defined over the object (Optional)\",\"default\":null},{\"name\":\"epoch\",\"type\":[\"null\",\"int\"],\"doc\":\"* Used to track when an object is deleted and a new one is\\n         * created with the same identifier. This is useful for when\\n         * UUIDs are based on something not likely to be unique, such\\n         * as file path.\",\"default\":null},{\"name\":\"properties\",\"type\":[\"null\",{\"type\":\"map\",\"values\":\"string\"}],\"doc\":\"* Arbitrary key, value pairs describing the entity.\\n         * NOTE: This attribute is meant as a temporary place holder for items that\\n         * will become first-class attributes in the next CDM version.\",\"default\":null,\"order\":\"ignore\"}]},\"doc\":\"The base object attributes\"},{\"name\":\"type\",\"type\":{\"type\":\"enum\",\"name\":\"FileObjectType\",\"doc\":\"* These types enumerate the types of FileObjects\",\"symbols\":[\"FILE_OBJECT_BLOCK\",\"FILE_OBJECT_CHAR\",\"FILE_OBJECT_DIR\",\"FILE_OBJECT_FILE\",\"FILE_OBJECT_LINK\",\"FILE_OBJECT_PEFILE\",\"FILE_OBJECT_UNIX_SOCKET\"]},\"doc\":\"The type of FileObject\"},{\"name\":\"fileDescriptor\",\"type\":[\"null\",\"int\"],\"doc\":\"The file descriptor (Optional)\",\"default\":null},{\"name\":\"localPrincipal\",\"type\":[\"null\",\"UUID\"],\"doc\":\"UUID of local principal that owns this file object.  This\\n         * attribute is optional because there are times when \\n         * the owner of the file may not be known at the time the file\\n         * object is reported (e.g., missed open call). Otherwise,\\n         * the local principal SHOULD be included.\",\"default\":null},{\"name\":\"size\",\"type\":[\"null\",\"long\"],\"doc\":\"* The file size in bytes (Optional). This attribute reports\\n         * the file size at the time the FileObject is created. Since records\\n         * are not updated, changes in file size is trackable via the events\\n         * that changed the file size.\",\"default\":null},{\"name\":\"peInfo\",\"type\":[\"null\",\"string\"],\"doc\":\"* portable execution (PE) info for windows (Optional).\\n         * Note from FiveDirections: We will LIKELY change this type for engagement 3\",\"default\":null},{\"name\":\"hashes\",\"type\":[\"null\",{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"CryptographicHash\",\"doc\":\"* Cryptographic hash records represent one or more cryptographic hashes for\\n     * an object, typically, a FileObject.\",\"fields\":[{\"name\":\"type\",\"type\":{\"type\":\"enum\",\"name\":\"CryptoHashType\",\"doc\":\"Cryptographich hash types\",\"symbols\":[\"MD5\",\"SHA1\",\"SHA256\",\"SHA512\",\"AUTHENTIHASH\",\"SSDEEP\",\"IMPHASH\"]},\"doc\":\"The type of hash used\"},{\"name\":\"hash\",\"type\":\"string\",\"doc\":\"The base64 encoded hash value\"}]}}],\"doc\":\"(Optional) Zero or more cryptographic hashes over the FileObject\",\"default\":null}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<FileObject> ENCODER =
      new BinaryMessageEncoder<FileObject>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<FileObject> DECODER =
      new BinaryMessageDecoder<FileObject>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   */
  public static BinaryMessageDecoder<FileObject> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   */
  public static BinaryMessageDecoder<FileObject> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<FileObject>(MODEL$, SCHEMA$, resolver);
  }

  /** Serializes this FileObject to a ByteBuffer. */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /** Deserializes a FileObject from a ByteBuffer. */
  public static FileObject fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  /** Universally unique identifier for the object */
  @Deprecated public com.bbn.tc.schema.avro.cdm20.UUID uuid;
  /** The base object attributes */
  @Deprecated public com.bbn.tc.schema.avro.cdm20.AbstractObject baseObject;
  /** The type of FileObject */
  @Deprecated public com.bbn.tc.schema.avro.cdm20.FileObjectType type;
  /** The file descriptor (Optional) */
  @Deprecated public java.lang.Integer fileDescriptor;
  /** UUID of local principal that owns this file object.  This
         * attribute is optional because there are times when 
         * the owner of the file may not be known at the time the file
         * object is reported (e.g., missed open call). Otherwise,
         * the local principal SHOULD be included. */
  @Deprecated public com.bbn.tc.schema.avro.cdm20.UUID localPrincipal;
  /** * The file size in bytes (Optional). This attribute reports
         * the file size at the time the FileObject is created. Since records
         * are not updated, changes in file size is trackable via the events
         * that changed the file size. */
  @Deprecated public java.lang.Long size;
  /** * portable execution (PE) info for windows (Optional).
         * Note from FiveDirections: We will LIKELY change this type for engagement 3 */
  @Deprecated public java.lang.CharSequence peInfo;
  /** (Optional) Zero or more cryptographic hashes over the FileObject */
  @Deprecated public java.util.List<com.bbn.tc.schema.avro.cdm20.CryptographicHash> hashes;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public FileObject() {}

  /**
   * All-args constructor.
   * @param uuid Universally unique identifier for the object
   * @param baseObject The base object attributes
   * @param type The type of FileObject
   * @param fileDescriptor The file descriptor (Optional)
   * @param localPrincipal UUID of local principal that owns this file object.  This
         * attribute is optional because there are times when 
         * the owner of the file may not be known at the time the file
         * object is reported (e.g., missed open call). Otherwise,
         * the local principal SHOULD be included.
   * @param size * The file size in bytes (Optional). This attribute reports
         * the file size at the time the FileObject is created. Since records
         * are not updated, changes in file size is trackable via the events
         * that changed the file size.
   * @param peInfo * portable execution (PE) info for windows (Optional).
         * Note from FiveDirections: We will LIKELY change this type for engagement 3
   * @param hashes (Optional) Zero or more cryptographic hashes over the FileObject
   */
  public FileObject(com.bbn.tc.schema.avro.cdm20.UUID uuid, com.bbn.tc.schema.avro.cdm20.AbstractObject baseObject, com.bbn.tc.schema.avro.cdm20.FileObjectType type, java.lang.Integer fileDescriptor, com.bbn.tc.schema.avro.cdm20.UUID localPrincipal, java.lang.Long size, java.lang.CharSequence peInfo, java.util.List<com.bbn.tc.schema.avro.cdm20.CryptographicHash> hashes) {
    this.uuid = uuid;
    this.baseObject = baseObject;
    this.type = type;
    this.fileDescriptor = fileDescriptor;
    this.localPrincipal = localPrincipal;
    this.size = size;
    this.peInfo = peInfo;
    this.hashes = hashes;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return uuid;
    case 1: return baseObject;
    case 2: return type;
    case 3: return fileDescriptor;
    case 4: return localPrincipal;
    case 5: return size;
    case 6: return peInfo;
    case 7: return hashes;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: uuid = (com.bbn.tc.schema.avro.cdm20.UUID)value$; break;
    case 1: baseObject = (com.bbn.tc.schema.avro.cdm20.AbstractObject)value$; break;
    case 2: type = (com.bbn.tc.schema.avro.cdm20.FileObjectType)value$; break;
    case 3: fileDescriptor = (java.lang.Integer)value$; break;
    case 4: localPrincipal = (com.bbn.tc.schema.avro.cdm20.UUID)value$; break;
    case 5: size = (java.lang.Long)value$; break;
    case 6: peInfo = (java.lang.CharSequence)value$; break;
    case 7: hashes = (java.util.List<com.bbn.tc.schema.avro.cdm20.CryptographicHash>)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'uuid' field.
   * @return Universally unique identifier for the object
   */
  public com.bbn.tc.schema.avro.cdm20.UUID getUuid() {
    return uuid;
  }

  /**
   * Sets the value of the 'uuid' field.
   * Universally unique identifier for the object
   * @param value the value to set.
   */
  public void setUuid(com.bbn.tc.schema.avro.cdm20.UUID value) {
    this.uuid = value;
  }

  /**
   * Gets the value of the 'baseObject' field.
   * @return The base object attributes
   */
  public com.bbn.tc.schema.avro.cdm20.AbstractObject getBaseObject() {
    return baseObject;
  }

  /**
   * Sets the value of the 'baseObject' field.
   * The base object attributes
   * @param value the value to set.
   */
  public void setBaseObject(com.bbn.tc.schema.avro.cdm20.AbstractObject value) {
    this.baseObject = value;
  }

  /**
   * Gets the value of the 'type' field.
   * @return The type of FileObject
   */
  public com.bbn.tc.schema.avro.cdm20.FileObjectType getType() {
    return type;
  }

  /**
   * Sets the value of the 'type' field.
   * The type of FileObject
   * @param value the value to set.
   */
  public void setType(com.bbn.tc.schema.avro.cdm20.FileObjectType value) {
    this.type = value;
  }

  /**
   * Gets the value of the 'fileDescriptor' field.
   * @return The file descriptor (Optional)
   */
  public java.lang.Integer getFileDescriptor() {
    return fileDescriptor;
  }

  /**
   * Sets the value of the 'fileDescriptor' field.
   * The file descriptor (Optional)
   * @param value the value to set.
   */
  public void setFileDescriptor(java.lang.Integer value) {
    this.fileDescriptor = value;
  }

  /**
   * Gets the value of the 'localPrincipal' field.
   * @return UUID of local principal that owns this file object.  This
         * attribute is optional because there are times when 
         * the owner of the file may not be known at the time the file
         * object is reported (e.g., missed open call). Otherwise,
         * the local principal SHOULD be included.
   */
  public com.bbn.tc.schema.avro.cdm20.UUID getLocalPrincipal() {
    return localPrincipal;
  }

  /**
   * Sets the value of the 'localPrincipal' field.
   * UUID of local principal that owns this file object.  This
         * attribute is optional because there are times when 
         * the owner of the file may not be known at the time the file
         * object is reported (e.g., missed open call). Otherwise,
         * the local principal SHOULD be included.
   * @param value the value to set.
   */
  public void setLocalPrincipal(com.bbn.tc.schema.avro.cdm20.UUID value) {
    this.localPrincipal = value;
  }

  /**
   * Gets the value of the 'size' field.
   * @return * The file size in bytes (Optional). This attribute reports
         * the file size at the time the FileObject is created. Since records
         * are not updated, changes in file size is trackable via the events
         * that changed the file size.
   */
  public java.lang.Long getSize() {
    return size;
  }

  /**
   * Sets the value of the 'size' field.
   * * The file size in bytes (Optional). This attribute reports
         * the file size at the time the FileObject is created. Since records
         * are not updated, changes in file size is trackable via the events
         * that changed the file size.
   * @param value the value to set.
   */
  public void setSize(java.lang.Long value) {
    this.size = value;
  }

  /**
   * Gets the value of the 'peInfo' field.
   * @return * portable execution (PE) info for windows (Optional).
         * Note from FiveDirections: We will LIKELY change this type for engagement 3
   */
  public java.lang.CharSequence getPeInfo() {
    return peInfo;
  }

  /**
   * Sets the value of the 'peInfo' field.
   * * portable execution (PE) info for windows (Optional).
         * Note from FiveDirections: We will LIKELY change this type for engagement 3
   * @param value the value to set.
   */
  public void setPeInfo(java.lang.CharSequence value) {
    this.peInfo = value;
  }

  /**
   * Gets the value of the 'hashes' field.
   * @return (Optional) Zero or more cryptographic hashes over the FileObject
   */
  public java.util.List<com.bbn.tc.schema.avro.cdm20.CryptographicHash> getHashes() {
    return hashes;
  }

  /**
   * Sets the value of the 'hashes' field.
   * (Optional) Zero or more cryptographic hashes over the FileObject
   * @param value the value to set.
   */
  public void setHashes(java.util.List<com.bbn.tc.schema.avro.cdm20.CryptographicHash> value) {
    this.hashes = value;
  }

  /**
   * Creates a new FileObject RecordBuilder.
   * @return A new FileObject RecordBuilder
   */
  public static com.bbn.tc.schema.avro.cdm20.FileObject.Builder newBuilder() {
    return new com.bbn.tc.schema.avro.cdm20.FileObject.Builder();
  }

  /**
   * Creates a new FileObject RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new FileObject RecordBuilder
   */
  public static com.bbn.tc.schema.avro.cdm20.FileObject.Builder newBuilder(com.bbn.tc.schema.avro.cdm20.FileObject.Builder other) {
    return new com.bbn.tc.schema.avro.cdm20.FileObject.Builder(other);
  }

  /**
   * Creates a new FileObject RecordBuilder by copying an existing FileObject instance.
   * @param other The existing instance to copy.
   * @return A new FileObject RecordBuilder
   */
  public static com.bbn.tc.schema.avro.cdm20.FileObject.Builder newBuilder(com.bbn.tc.schema.avro.cdm20.FileObject other) {
    return new com.bbn.tc.schema.avro.cdm20.FileObject.Builder(other);
  }

  /**
   * RecordBuilder for FileObject instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<FileObject>
    implements org.apache.avro.data.RecordBuilder<FileObject> {

    /** Universally unique identifier for the object */
    private com.bbn.tc.schema.avro.cdm20.UUID uuid;
    /** The base object attributes */
    private com.bbn.tc.schema.avro.cdm20.AbstractObject baseObject;
    private com.bbn.tc.schema.avro.cdm20.AbstractObject.Builder baseObjectBuilder;
    /** The type of FileObject */
    private com.bbn.tc.schema.avro.cdm20.FileObjectType type;
    /** The file descriptor (Optional) */
    private java.lang.Integer fileDescriptor;
    /** UUID of local principal that owns this file object.  This
         * attribute is optional because there are times when 
         * the owner of the file may not be known at the time the file
         * object is reported (e.g., missed open call). Otherwise,
         * the local principal SHOULD be included. */
    private com.bbn.tc.schema.avro.cdm20.UUID localPrincipal;
    /** * The file size in bytes (Optional). This attribute reports
         * the file size at the time the FileObject is created. Since records
         * are not updated, changes in file size is trackable via the events
         * that changed the file size. */
    private java.lang.Long size;
    /** * portable execution (PE) info for windows (Optional).
         * Note from FiveDirections: We will LIKELY change this type for engagement 3 */
    private java.lang.CharSequence peInfo;
    /** (Optional) Zero or more cryptographic hashes over the FileObject */
    private java.util.List<com.bbn.tc.schema.avro.cdm20.CryptographicHash> hashes;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(com.bbn.tc.schema.avro.cdm20.FileObject.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.uuid)) {
        this.uuid = data().deepCopy(fields()[0].schema(), other.uuid);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.baseObject)) {
        this.baseObject = data().deepCopy(fields()[1].schema(), other.baseObject);
        fieldSetFlags()[1] = true;
      }
      if (other.hasBaseObjectBuilder()) {
        this.baseObjectBuilder = com.bbn.tc.schema.avro.cdm20.AbstractObject.newBuilder(other.getBaseObjectBuilder());
      }
      if (isValidValue(fields()[2], other.type)) {
        this.type = data().deepCopy(fields()[2].schema(), other.type);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.fileDescriptor)) {
        this.fileDescriptor = data().deepCopy(fields()[3].schema(), other.fileDescriptor);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.localPrincipal)) {
        this.localPrincipal = data().deepCopy(fields()[4].schema(), other.localPrincipal);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.size)) {
        this.size = data().deepCopy(fields()[5].schema(), other.size);
        fieldSetFlags()[5] = true;
      }
      if (isValidValue(fields()[6], other.peInfo)) {
        this.peInfo = data().deepCopy(fields()[6].schema(), other.peInfo);
        fieldSetFlags()[6] = true;
      }
      if (isValidValue(fields()[7], other.hashes)) {
        this.hashes = data().deepCopy(fields()[7].schema(), other.hashes);
        fieldSetFlags()[7] = true;
      }
    }

    /**
     * Creates a Builder by copying an existing FileObject instance
     * @param other The existing instance to copy.
     */
    private Builder(com.bbn.tc.schema.avro.cdm20.FileObject other) {
            super(SCHEMA$);
      if (isValidValue(fields()[0], other.uuid)) {
        this.uuid = data().deepCopy(fields()[0].schema(), other.uuid);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.baseObject)) {
        this.baseObject = data().deepCopy(fields()[1].schema(), other.baseObject);
        fieldSetFlags()[1] = true;
      }
      this.baseObjectBuilder = null;
      if (isValidValue(fields()[2], other.type)) {
        this.type = data().deepCopy(fields()[2].schema(), other.type);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.fileDescriptor)) {
        this.fileDescriptor = data().deepCopy(fields()[3].schema(), other.fileDescriptor);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.localPrincipal)) {
        this.localPrincipal = data().deepCopy(fields()[4].schema(), other.localPrincipal);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.size)) {
        this.size = data().deepCopy(fields()[5].schema(), other.size);
        fieldSetFlags()[5] = true;
      }
      if (isValidValue(fields()[6], other.peInfo)) {
        this.peInfo = data().deepCopy(fields()[6].schema(), other.peInfo);
        fieldSetFlags()[6] = true;
      }
      if (isValidValue(fields()[7], other.hashes)) {
        this.hashes = data().deepCopy(fields()[7].schema(), other.hashes);
        fieldSetFlags()[7] = true;
      }
    }

    /**
      * Gets the value of the 'uuid' field.
      * Universally unique identifier for the object
      * @return The value.
      */
    public com.bbn.tc.schema.avro.cdm20.UUID getUuid() {
      return uuid;
    }

    /**
      * Sets the value of the 'uuid' field.
      * Universally unique identifier for the object
      * @param value The value of 'uuid'.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.FileObject.Builder setUuid(com.bbn.tc.schema.avro.cdm20.UUID value) {
      validate(fields()[0], value);
      this.uuid = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'uuid' field has been set.
      * Universally unique identifier for the object
      * @return True if the 'uuid' field has been set, false otherwise.
      */
    public boolean hasUuid() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'uuid' field.
      * Universally unique identifier for the object
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.FileObject.Builder clearUuid() {
      uuid = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'baseObject' field.
      * The base object attributes
      * @return The value.
      */
    public com.bbn.tc.schema.avro.cdm20.AbstractObject getBaseObject() {
      return baseObject;
    }

    /**
      * Sets the value of the 'baseObject' field.
      * The base object attributes
      * @param value The value of 'baseObject'.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.FileObject.Builder setBaseObject(com.bbn.tc.schema.avro.cdm20.AbstractObject value) {
      validate(fields()[1], value);
      this.baseObjectBuilder = null;
      this.baseObject = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'baseObject' field has been set.
      * The base object attributes
      * @return True if the 'baseObject' field has been set, false otherwise.
      */
    public boolean hasBaseObject() {
      return fieldSetFlags()[1];
    }

    /**
     * Gets the Builder instance for the 'baseObject' field and creates one if it doesn't exist yet.
     * The base object attributes
     * @return This builder.
     */
    public com.bbn.tc.schema.avro.cdm20.AbstractObject.Builder getBaseObjectBuilder() {
      if (baseObjectBuilder == null) {
        if (hasBaseObject()) {
          setBaseObjectBuilder(com.bbn.tc.schema.avro.cdm20.AbstractObject.newBuilder(baseObject));
        } else {
          setBaseObjectBuilder(com.bbn.tc.schema.avro.cdm20.AbstractObject.newBuilder());
        }
      }
      return baseObjectBuilder;
    }

    /**
     * Sets the Builder instance for the 'baseObject' field
     * The base object attributes
     * @param value The builder instance that must be set.
     * @return This builder.
     */
    public com.bbn.tc.schema.avro.cdm20.FileObject.Builder setBaseObjectBuilder(com.bbn.tc.schema.avro.cdm20.AbstractObject.Builder value) {
      clearBaseObject();
      baseObjectBuilder = value;
      return this;
    }

    /**
     * Checks whether the 'baseObject' field has an active Builder instance
     * The base object attributes
     * @return True if the 'baseObject' field has an active Builder instance
     */
    public boolean hasBaseObjectBuilder() {
      return baseObjectBuilder != null;
    }

    /**
      * Clears the value of the 'baseObject' field.
      * The base object attributes
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.FileObject.Builder clearBaseObject() {
      baseObject = null;
      baseObjectBuilder = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'type' field.
      * The type of FileObject
      * @return The value.
      */
    public com.bbn.tc.schema.avro.cdm20.FileObjectType getType() {
      return type;
    }

    /**
      * Sets the value of the 'type' field.
      * The type of FileObject
      * @param value The value of 'type'.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.FileObject.Builder setType(com.bbn.tc.schema.avro.cdm20.FileObjectType value) {
      validate(fields()[2], value);
      this.type = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'type' field has been set.
      * The type of FileObject
      * @return True if the 'type' field has been set, false otherwise.
      */
    public boolean hasType() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'type' field.
      * The type of FileObject
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.FileObject.Builder clearType() {
      type = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /**
      * Gets the value of the 'fileDescriptor' field.
      * The file descriptor (Optional)
      * @return The value.
      */
    public java.lang.Integer getFileDescriptor() {
      return fileDescriptor;
    }

    /**
      * Sets the value of the 'fileDescriptor' field.
      * The file descriptor (Optional)
      * @param value The value of 'fileDescriptor'.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.FileObject.Builder setFileDescriptor(java.lang.Integer value) {
      validate(fields()[3], value);
      this.fileDescriptor = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'fileDescriptor' field has been set.
      * The file descriptor (Optional)
      * @return True if the 'fileDescriptor' field has been set, false otherwise.
      */
    public boolean hasFileDescriptor() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'fileDescriptor' field.
      * The file descriptor (Optional)
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.FileObject.Builder clearFileDescriptor() {
      fileDescriptor = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /**
      * Gets the value of the 'localPrincipal' field.
      * UUID of local principal that owns this file object.  This
         * attribute is optional because there are times when 
         * the owner of the file may not be known at the time the file
         * object is reported (e.g., missed open call). Otherwise,
         * the local principal SHOULD be included.
      * @return The value.
      */
    public com.bbn.tc.schema.avro.cdm20.UUID getLocalPrincipal() {
      return localPrincipal;
    }

    /**
      * Sets the value of the 'localPrincipal' field.
      * UUID of local principal that owns this file object.  This
         * attribute is optional because there are times when 
         * the owner of the file may not be known at the time the file
         * object is reported (e.g., missed open call). Otherwise,
         * the local principal SHOULD be included.
      * @param value The value of 'localPrincipal'.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.FileObject.Builder setLocalPrincipal(com.bbn.tc.schema.avro.cdm20.UUID value) {
      validate(fields()[4], value);
      this.localPrincipal = value;
      fieldSetFlags()[4] = true;
      return this;
    }

    /**
      * Checks whether the 'localPrincipal' field has been set.
      * UUID of local principal that owns this file object.  This
         * attribute is optional because there are times when 
         * the owner of the file may not be known at the time the file
         * object is reported (e.g., missed open call). Otherwise,
         * the local principal SHOULD be included.
      * @return True if the 'localPrincipal' field has been set, false otherwise.
      */
    public boolean hasLocalPrincipal() {
      return fieldSetFlags()[4];
    }


    /**
      * Clears the value of the 'localPrincipal' field.
      * UUID of local principal that owns this file object.  This
         * attribute is optional because there are times when 
         * the owner of the file may not be known at the time the file
         * object is reported (e.g., missed open call). Otherwise,
         * the local principal SHOULD be included.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.FileObject.Builder clearLocalPrincipal() {
      localPrincipal = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    /**
      * Gets the value of the 'size' field.
      * * The file size in bytes (Optional). This attribute reports
         * the file size at the time the FileObject is created. Since records
         * are not updated, changes in file size is trackable via the events
         * that changed the file size.
      * @return The value.
      */
    public java.lang.Long getSize() {
      return size;
    }

    /**
      * Sets the value of the 'size' field.
      * * The file size in bytes (Optional). This attribute reports
         * the file size at the time the FileObject is created. Since records
         * are not updated, changes in file size is trackable via the events
         * that changed the file size.
      * @param value The value of 'size'.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.FileObject.Builder setSize(java.lang.Long value) {
      validate(fields()[5], value);
      this.size = value;
      fieldSetFlags()[5] = true;
      return this;
    }

    /**
      * Checks whether the 'size' field has been set.
      * * The file size in bytes (Optional). This attribute reports
         * the file size at the time the FileObject is created. Since records
         * are not updated, changes in file size is trackable via the events
         * that changed the file size.
      * @return True if the 'size' field has been set, false otherwise.
      */
    public boolean hasSize() {
      return fieldSetFlags()[5];
    }


    /**
      * Clears the value of the 'size' field.
      * * The file size in bytes (Optional). This attribute reports
         * the file size at the time the FileObject is created. Since records
         * are not updated, changes in file size is trackable via the events
         * that changed the file size.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.FileObject.Builder clearSize() {
      size = null;
      fieldSetFlags()[5] = false;
      return this;
    }

    /**
      * Gets the value of the 'peInfo' field.
      * * portable execution (PE) info for windows (Optional).
         * Note from FiveDirections: We will LIKELY change this type for engagement 3
      * @return The value.
      */
    public java.lang.CharSequence getPeInfo() {
      return peInfo;
    }

    /**
      * Sets the value of the 'peInfo' field.
      * * portable execution (PE) info for windows (Optional).
         * Note from FiveDirections: We will LIKELY change this type for engagement 3
      * @param value The value of 'peInfo'.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.FileObject.Builder setPeInfo(java.lang.CharSequence value) {
      validate(fields()[6], value);
      this.peInfo = value;
      fieldSetFlags()[6] = true;
      return this;
    }

    /**
      * Checks whether the 'peInfo' field has been set.
      * * portable execution (PE) info for windows (Optional).
         * Note from FiveDirections: We will LIKELY change this type for engagement 3
      * @return True if the 'peInfo' field has been set, false otherwise.
      */
    public boolean hasPeInfo() {
      return fieldSetFlags()[6];
    }


    /**
      * Clears the value of the 'peInfo' field.
      * * portable execution (PE) info for windows (Optional).
         * Note from FiveDirections: We will LIKELY change this type for engagement 3
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.FileObject.Builder clearPeInfo() {
      peInfo = null;
      fieldSetFlags()[6] = false;
      return this;
    }

    /**
      * Gets the value of the 'hashes' field.
      * (Optional) Zero or more cryptographic hashes over the FileObject
      * @return The value.
      */
    public java.util.List<com.bbn.tc.schema.avro.cdm20.CryptographicHash> getHashes() {
      return hashes;
    }

    /**
      * Sets the value of the 'hashes' field.
      * (Optional) Zero or more cryptographic hashes over the FileObject
      * @param value The value of 'hashes'.
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.FileObject.Builder setHashes(java.util.List<com.bbn.tc.schema.avro.cdm20.CryptographicHash> value) {
      validate(fields()[7], value);
      this.hashes = value;
      fieldSetFlags()[7] = true;
      return this;
    }

    /**
      * Checks whether the 'hashes' field has been set.
      * (Optional) Zero or more cryptographic hashes over the FileObject
      * @return True if the 'hashes' field has been set, false otherwise.
      */
    public boolean hasHashes() {
      return fieldSetFlags()[7];
    }


    /**
      * Clears the value of the 'hashes' field.
      * (Optional) Zero or more cryptographic hashes over the FileObject
      * @return This builder.
      */
    public com.bbn.tc.schema.avro.cdm20.FileObject.Builder clearHashes() {
      hashes = null;
      fieldSetFlags()[7] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FileObject build() {
      try {
        FileObject record = new FileObject();
        record.uuid = fieldSetFlags()[0] ? this.uuid : (com.bbn.tc.schema.avro.cdm20.UUID) defaultValue(fields()[0]);
        if (baseObjectBuilder != null) {
          record.baseObject = this.baseObjectBuilder.build();
        } else {
          record.baseObject = fieldSetFlags()[1] ? this.baseObject : (com.bbn.tc.schema.avro.cdm20.AbstractObject) defaultValue(fields()[1]);
        }
        record.type = fieldSetFlags()[2] ? this.type : (com.bbn.tc.schema.avro.cdm20.FileObjectType) defaultValue(fields()[2]);
        record.fileDescriptor = fieldSetFlags()[3] ? this.fileDescriptor : (java.lang.Integer) defaultValue(fields()[3]);
        record.localPrincipal = fieldSetFlags()[4] ? this.localPrincipal : (com.bbn.tc.schema.avro.cdm20.UUID) defaultValue(fields()[4]);
        record.size = fieldSetFlags()[5] ? this.size : (java.lang.Long) defaultValue(fields()[5]);
        record.peInfo = fieldSetFlags()[6] ? this.peInfo : (java.lang.CharSequence) defaultValue(fields()[6]);
        record.hashes = fieldSetFlags()[7] ? this.hashes : (java.util.List<com.bbn.tc.schema.avro.cdm20.CryptographicHash>) defaultValue(fields()[7]);
        return record;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<FileObject>
    WRITER$ = (org.apache.avro.io.DatumWriter<FileObject>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<FileObject>
    READER$ = (org.apache.avro.io.DatumReader<FileObject>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}
