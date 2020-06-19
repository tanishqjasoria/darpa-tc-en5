package com.bbn.tc.schema.serialization.kafka;

import java.util.Map;

import org.apache.avro.generic.GenericContainer;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.log4j.Logger;

import com.bbn.tc.schema.serialization.AvroGenericSerializer;

/**
 * This "serializer" returns the same bytes every time
 * It's used for performance testing to remove serialization from the mix
 * It should not be used for anything else
 * Call loadPreserializedData with the bytes you want it to return for every serialize call
 * @author bbenyo
 *
 * @param <T>
 */
public class NoOpSerializer<T extends GenericContainer>
    extends AvroGenericSerializer implements Serializer<T> {

	static byte[] preSerializedData = null;
	
	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		try {
			initialize(configs, isKey);
		}catch(Exception e) {
			throw new ConfigException("Failed to configure NoOp serializer", e);
		}
	}
	
	public static void loadPreserializedData(byte[] data) {
		preSerializedData = data.clone();
	}
	
	@Override
	public byte[] serialize(String topic, T data) {
		if(data == null) throw new SerializationException("Can not serialize null data");
		if(preSerializedData == null) throw new SerializationException("Need to set preSerializedData first");
		return preSerializedData;
	}
	
	@Override
	public void close() {
		
	}
}
