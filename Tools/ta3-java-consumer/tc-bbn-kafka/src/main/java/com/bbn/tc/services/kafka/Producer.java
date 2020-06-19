/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.services.kafka;



import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericContainer;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.log4j.Logger;

import com.bbn.tc.schema.avro.cdm20.Event;
import com.bbn.tc.schema.avro.cdm20.TCCDMDatum;
import com.bbn.tc.schema.serialization.AvroConfig;
import com.bbn.tc.schema.serialization.AvroGenericSerializer;
import com.bbn.tc.schema.serialization.Utils;
import com.bbn.tc.schema.serialization.kafka.KafkaAvroGenericSerializer;
import com.bbn.tc.schema.serialization.kafka.NoOpSerializer;
import com.bbn.tc.schema.utils.RecordGenerator;
import com.bbn.tc.services.kafka.Stats.PerfCallback;

/**
 * A generic kafka producer that publishes records
 *   and is time bounded. It allows both generic records or
 *   compiled
 * @author jkhoury
 */
public class Producer extends Thread {

    protected static final Logger logger = Logger.getLogger(Producer.class.getCanonicalName());
	protected final KafkaProducer<String, GenericContainer> producer;
	protected final String topic;
	protected final Boolean isAsync;
	protected final int duration;
	protected final int delay;
	protected String securityProtocol;
	protected String truststoreLocation;
	protected String truststorePassword;
	protected String keystoreLocation;
	protected String keystorePassword;
	protected String sslKey;
	// private long messageNo;
	protected final String producerSchemaFilename;
	protected final int numKVPairs;
	protected boolean isSpecific;
	protected boolean withJson = false;
	protected boolean nullKey = false;
	public AtomicBoolean shutdown = new AtomicBoolean(false);
	protected int count;
	// for file serialization
	protected final File file;
	protected AvroGenericSerializer<GenericContainer> fileSerializer;
	// for json serialization
	protected AvroGenericSerializer<GenericContainer> jsonSerializer;

	protected long maxRecords = -1;
	protected boolean randomRecords = true;
	protected boolean noavro = false;
        protected int estimatedRecordSize;
    
	static protected boolean Instrumented = true;
	
	public Producer(String serverAddress, String producerID, String topic, Boolean isAsync,
			int duration, long maxRecords, int delay, boolean isSpecific,  File avroFile,
			String producerSchemaFilename, int numKVPairs, int recordSize, boolean randomRecords) {
		this(serverAddress, producerID, topic, isAsync, duration, maxRecords, delay, isSpecific, avroFile,
				producerSchemaFilename, numKVPairs, recordSize, randomRecords, false, false);
	}
	
	public Producer(String serverAddress, String producerID, String topic, Boolean isAsync,
			int duration, long maxRecords, int delay, boolean isSpecific,  File avroFile,
			String producerSchemaFilename, int numKVPairs, int recordSize, boolean randomRecords, 
			boolean withJson) {
		this(serverAddress, producerID, topic, isAsync, duration, maxRecords, delay, isSpecific, avroFile,
				producerSchemaFilename, numKVPairs, recordSize, randomRecords, false, withJson);
	}
	
	public Producer(String serverAddress, String producerID, String topic, Boolean isAsync,
			int duration, long maxRecords, int delay, boolean isSpecific,  File avroFile,
			String producerSchemaFilename, int numKVPairs, int recordSize, boolean randomRecords, 
			boolean noavro, boolean withJson) {
		this.topic = topic;
		this.isAsync = isAsync;
		this.duration = duration;
		this.delay = delay;
		this.isSpecific = isSpecific;
		this.file = avroFile;
		// this.messageNo = 0;
		this.producerSchemaFilename = producerSchemaFilename;
		this.numKVPairs = numKVPairs;
		this.randomRecords = randomRecords;
		this.maxRecords = maxRecords;
		this.noavro = noavro;
		this.estimatedRecordSize = recordSize;
		this.withJson = withJson;

		logger.info("Estimated Record Size: "+estimatedRecordSize);
		
		//init the jsonSerializer
		try {
			jsonSerializer = new AvroGenericSerializer<>(this.producerSchemaFilename, this.isSpecific, null, this.withJson);
		}catch (Exception e){
			e.printStackTrace();
			logger.error(e);
		}

		if (this.file != null) {
			producer = null;
			try {
				assert !file.exists(); // we already verified this in Main
				fileSerializer = new AvroGenericSerializer<>(this.producerSchemaFilename, this.isSpecific, file, this.withJson);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				try{
					if(fileSerializer != null) fileSerializer.close();
					fileSerializer = null;
				}catch (Exception ee){}
			}
		}else {
			Properties props = new Properties();
			props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddress);
			props.put(ProducerConfig.CLIENT_ID_CONFIG, producerID);
			props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
					"org.apache.kafka.common.serialization.StringSerializer");

			// Use the graph serializer if a graph schema was passed in else use strings
			if (noavro) {
				props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
						com.bbn.tc.schema.serialization.kafka.NoOpSerializer.class);
			} else {
				props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
						com.bbn.tc.schema.serialization.kafka.KafkaAvroGenericSerializer.class);
			}
			// additional serialization properties needed
			props.put(AvroConfig.SCHEMA_WRITER_FILE, producerSchemaFilename);
			if (isSpecific)
				props.put(AvroConfig.SCHEMA_SERDE_IS_SPECIFIC, isSpecific);
			
			logger.info("Creating new Instrumented KafkaProducer");
			producer = new InstrumentedKafkaProducer<>(props);
			if (Instrumented) {
			    InstrumentationManager.turnOnInstrumentationServer();
			}
		}
	}

	public boolean isNullKey() {
        return nullKey;
    }

    public void setNullKey(boolean nullKey) {
        this.nullKey = nullKey;
    }

    public Producer(String serverAddress, String producerID, String topic, Boolean isAsync,
			int duration, long maxRecords, int delay, boolean isSpecific,  File avroFile,
			String producerSchemaFilename, int numKVPairs, int recordSize, boolean randomRecords,
			String securityProtocol, String truststoreLocation, String truststorePassword, 
			String keystoreLocation, String keystorePassword, String sslKey) {
		this(serverAddress, producerID, topic, isAsync, duration, maxRecords, delay, isSpecific, avroFile,
				producerSchemaFilename, numKVPairs, recordSize, randomRecords, false, false, securityProtocol,
				truststoreLocation, truststorePassword, keystoreLocation, keystorePassword, sslKey);
	}
	
	public Producer(String serverAddress, String producerID, String topic, Boolean isAsync,
			int duration, long maxRecords, int delay, boolean isSpecific,  File avroFile,
			String producerSchemaFilename, int numKVPairs, int recordSize, boolean randomRecords, 
			boolean withJson, String securityProtocol, String truststoreLocation, String truststorePassword,
			String keystoreLocation, String keystorePassword, String sslKey) {
		this(serverAddress, producerID, topic, isAsync, duration, maxRecords, delay, isSpecific, avroFile,
				producerSchemaFilename, numKVPairs, recordSize, randomRecords, false, withJson,
				securityProtocol, truststoreLocation, truststorePassword, keystoreLocation, keystorePassword,
				sslKey);
	}
	
	public Producer(String serverAddress, String producerID, String topic, Boolean isAsync,
			int duration, long maxRecords, int delay, boolean isSpecific,  File avroFile,
			String producerSchemaFilename, int numKVPairs, int recordSize, boolean randomRecords, 
			boolean noavro, boolean withJson, String securityProtocol, String truststoreLocation,
			String truststorePassword, String keystoreLocation, String keystorePassword, String sslKey) {
		this.topic = topic;
		this.isAsync = isAsync;
		this.duration = duration;
		this.delay = delay;
		this.isSpecific = isSpecific;
		this.file = avroFile;
		// this.messageNo = 0;
		this.producerSchemaFilename = producerSchemaFilename;
		this.numKVPairs = numKVPairs;
		this.randomRecords = randomRecords;
		this.maxRecords = maxRecords;
		this.noavro = noavro;
		this.estimatedRecordSize = recordSize;
		this.withJson = withJson;

		this.securityProtocol = securityProtocol;
		this.truststoreLocation = truststoreLocation;
		this.truststorePassword = truststorePassword;
		this.keystoreLocation = keystoreLocation;
		this.keystorePassword = keystorePassword;
		this.sslKey = sslKey;

		//init the jsonSerializer
		try {
			jsonSerializer = new AvroGenericSerializer<>(this.producerSchemaFilename, this.isSpecific, null, this.withJson);
		}catch (Exception e){
			e.printStackTrace();
			logger.error(e);
		}

		if (this.file != null) {
			producer = null;
			try {
				assert !file.exists(); // we already verified this in Main
				fileSerializer = new AvroGenericSerializer<>(this.producerSchemaFilename, this.isSpecific, file, this.withJson);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				try{
					if(fileSerializer != null) fileSerializer.close();
					fileSerializer = null;
				}catch (Exception ee){}
			}
		}else {
			Properties props = new Properties();
			props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddress);
			props.put(ProducerConfig.CLIENT_ID_CONFIG, producerID);
			props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
					"org.apache.kafka.common.serialization.StringSerializer");

                        if (securityProtocol != null) {
				// configure the following three settings for SSL encryption
				props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
				props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststoreLocation);
				props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword);

				// configure the following three settings for SSL authentication
				props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystoreLocation);
				props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePassword);
				props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, sslKey);
			}

			// Use the graph serializer if a graph schema was passed in else use strings
			
			if (noavro) {
				props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
						com.bbn.tc.schema.serialization.kafka.NoOpSerializer.class);
			} else {
				props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
						com.bbn.tc.schema.serialization.kafka.KafkaAvroGenericSerializer.class);
			}
			// additional serialization properties needed
			props.put(AvroConfig.SCHEMA_WRITER_FILE, producerSchemaFilename);
			if (isSpecific)
				props.put(AvroConfig.SCHEMA_SERDE_IS_SPECIFIC, isSpecific);
			
			logger.info("Creating new Instrumented KafkaProducer");
			producer = new InstrumentedKafkaProducer<>(props);
			if (Instrumented) {
			    InstrumentationManager.turnOnInstrumentationServer();
			}
		}
	}
	
	public static void setInstrumented(boolean flag) {
	    if (flag) {
	        if (!Instrumented) {
	            InstrumentationManager.turnOnInstrumentationServer();
	        }
	    } else if (Instrumented) {
	        InstrumentationManager.turnOffInstrumentationServer();
	    }
	    
	    Instrumented = flag;
	}
	
	public void setShutdown(){
		this.shutdown.set(true);
	}

	protected GenericContainer generateEdge(Schema writerSchema) throws Exception {
		return RecordGenerator.randomEdgeRecord(writerSchema, this.numKVPairs, isSpecific);
	}

	public void run() {
		logger.info("Starting producer");

		if(this.file != null && this.fileSerializer == null)
			throw new IllegalStateException("Instance is not initialized properly, fileSerialzer is null");

		Schema writerSchema;
		GenericContainer edge = null;
		count = 0;

		long endTime = (duration == 0) ? Long.MAX_VALUE :  System.currentTimeMillis() + duration * 1000;

		try {
			// Load the schema
			writerSchema = Utils.loadSchema(this.producerSchemaFilename);

			// Initialize statistics for the kafka producer
			Stats stats = null;
			if (maxRecords > 0 && producer != null) {
				stats = new Stats(maxRecords, 5000);
			}

			// If we're sending the same record each time, generate it here outside the loop
			// Furthermore, make sure it's a 'Event' type with no additional parameters
			//    to make sure we get consistent results
			if (!randomRecords ) {
				boolean gotIt = false;
				while (!gotIt) {
					edge = generateEdge(writerSchema);
					if (!(edge instanceof TCCDMDatum)) {
						gotIt = true; // For non TCCDMDatum, we don't need to do anything special
					} else {
						TCCDMDatum cdmRecord = (TCCDMDatum)edge;
						//logger.info("Generated a "+cdmRecord.getDatum().getClass());
						if (cdmRecord.getDatum() instanceof Event) {
							if (((Event)cdmRecord.getDatum()).getParameters() == null) {
								gotIt = true;
								// We want events with no extra params for a consistent size
							}
						}
					}
				}

				logger.info("Static record to publish: "+edge);

                KafkaAvroGenericSerializer<GenericContainer> testSerializer =
                    new KafkaAvroGenericSerializer<GenericContainer>();
		        Map<String, Object> props = new HashMap<String, Object>();
		        // additional serialization properties needed
		        props.put(AvroConfig.SCHEMA_WRITER_SCHEMA, writerSchema);
		        props.put(AvroConfig.SCHEMA_SERDE_IS_SPECIFIC, true);
		        testSerializer.configure(props, false);

		        byte[] bytes = testSerializer.serialize("topic", edge);
		        
		        if (noavro) {
		        	logger.info("Loading preSerialized bytes in the NoOpSerializer");
		        	NoOpSerializer.loadPreserializedData(bytes);
		        }
		        
		        // recordSize = bytes.length;
		        testSerializer.close();
			}

			logger.info("Max records to publish: "+maxRecords);
			// logger.info("Record size: "+recordSize);
			logger.info("Random record data: "+randomRecords);

			/**
			 * now the sending
			 */
			while (!shutdown.get() && System.currentTimeMillis() <= endTime && (maxRecords == -1 || count < maxRecords)) {

				/**
				 * Create an edge record to send.
				 * Note here we use a helper method {@link RecordGenerator.randomEdgeRecord}
				 * that we created to quickly generate random records.
				 * We use the helper so that the producer code is decoupled from the
				 *  schema itself. Ideally, it is recommended to use this modular
				 *  approach to separate the record creation logic from the producing logic.
				 * Take a look at the generator to see how to create compiled or generic records.
				 * The producer can handle either.
				 *
				 */

				// Generate a new random record if we're not doing throughput testing,
				// or if we are, but we want to generate a random record each time anyway'
				if (maxRecords == -1 || randomRecords) {
					logger.debug("Generating random record");
					edge = generateEdge(writerSchema);
					//logger.info("Generating random record: done");
				}

				/**
				 * Now send, note that since no partition is specified in the send, the key will
				 * be hashed to determine a partition to send to
				 */
				//String key = ""+(messageNo++);
				String key = ""+System.currentTimeMillis(); // we use timestamp to measure latency at the consumer
				if (nullKey) {
				    key = null;
				}

				if(fileSerializer != null){
					// =================== <File producer> ===================
					fileSerializer.serializeToFile(edge);
					// =================== </File producer> ===================
				}else {
					if (logger.isDebugEnabled()) {
						logger.debug("Attempting to send record k:" + key);
						logger.debug("value:" + toJsonString(edge));
					}
					// =================== <KAFKA producer> ===================
					ProducerRecord<String, GenericContainer> record = new ProducerRecord<>(topic, key, edge);
					long sendStartMs = System.currentTimeMillis();
					Callback cb = null;
					// If we're collecting throughput stats, use that callback
					// TODO: Do we need to set the callback every time here?
					if (stats != null) {
						cb = stats.nextCompletion(sendStartMs, stats);
					} else {
						cb = new DemoCallBack(System.currentTimeMillis(), key, edge);
					}
					if (isAsync) { // Send asynchronously
						producer.send(record, cb);
					} else { // Send synchronously (wait for an ack from the broker)
						try {
							producer.send(record).get();
							if (!(cb instanceof DemoCallBack)) {
							    if (estimatedRecordSize > 0 && cb instanceof PerfCallback) {
								((PerfCallback)cb).setEstimatedRecordSize(estimatedRecordSize);
							    } else {
								logger.info("Estimated record size: "+estimatedRecordSize);
							    }
							    cb.onCompletion(null, null);
							}
						} catch (InterruptedException e) {
							logger.error(e);
						} catch (ExecutionException e) {
							logger.error(e);
						}
					}
					// =================== </KAFKA producer> ===================
				}
				++count;
				if (logger.isDebugEnabled()) {
				    // The unit tests check for this line pattern in the log, edit the tests if you change this
				    logger.debug("Sent message: (" + count + ") with key " + key);
				}
				handleDelay();
			}

			// cleanup, close the producer
			cleanup();
			if (stats != null) {
				stats.printTotal();
			}
			logger.info("Closed producer.");

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

	// Use a method so subclasses can override the delay processing and add in things like nondeterminism
	protected void handleDelay() {
		//to pace
		if(delay > 0) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void cleanup() {
		if(producer != null) producer.close();
		if(fileSerializer != null) fileSerializer.close();
	}

	public String toJsonString(GenericContainer record) throws Exception{
		String jsonString = jsonSerializer != null ? jsonSerializer.serializeToJson(record, false)
				: record.toString();
		if(jsonString.startsWith("\n")) jsonString = jsonString.replaceFirst("\n", "");
		return jsonString;
	}

}

class DemoCallBack implements Callback {

	private long startTime;
	private String key;

	public DemoCallBack(long startTime, String key, GenericContainer record) {
		this.startTime = startTime;
		this.key = key;
	}

	/**
	 * A callback method the user can implement to provide asynchronous handling of request completion. This method will
	 * be called when the record sent to the server has been acknowledged. Exactly one of the arguments will be
	 * non-null.
	 *
	 * @param metadata  The metadata for the record that was sent (i.e. the partition and offset). Null if an error
	 *                  occurred.
	 * @param exception The exception thrown during processing of this record. Null if no error occurred.
	 */
	public void onCompletion(RecordMetadata metadata, Exception exception) {

		long elapsedTime = System.currentTimeMillis() - startTime;
		if (metadata != null) {
			System.out.println(
					"message(" + key + ") sent to partition(" + metadata.partition() +
					"), " +
					"offset(" + metadata.offset() + ") in " + elapsedTime + " ms");
		} else {
			exception.printStackTrace();
		}
	}

}
