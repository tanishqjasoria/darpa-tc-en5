/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.bbn.tc.schema.avro.cdm20;
@SuppressWarnings("all")
/** * HostType enumerates the host roles or device types */
@org.apache.avro.specific.AvroGenerated
public enum HostType {
  HOST_MOBILE, HOST_SERVER, HOST_DESKTOP, HOST_OTHER  ;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"enum\",\"name\":\"HostType\",\"namespace\":\"com.bbn.tc.schema.avro.cdm20\",\"doc\":\"* HostType enumerates the host roles or device types\",\"symbols\":[\"HOST_MOBILE\",\"HOST_SERVER\",\"HOST_DESKTOP\",\"HOST_OTHER\"]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
}
