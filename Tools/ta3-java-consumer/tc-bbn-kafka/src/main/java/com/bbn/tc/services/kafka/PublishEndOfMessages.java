package com.bbn.tc.services.kafka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.bbn.tc.schema.avro.cdm20.InstrumentationSource;
import com.bbn.tc.schema.avro.cdm20.Principal;
import com.bbn.tc.schema.avro.cdm20.PrincipalType;
import com.bbn.tc.schema.avro.cdm20.TCCDMDatum;
import com.bbn.tc.schema.serialization.AvroConfig;
import com.bbn.tc.schema.utils.SchemaUtils;

public class PublishEndOfMessages {
	
	private static final Logger logger = Logger.getLogger(PublishEndOfMessages.class.getName());
	
    // Common properties
    protected static String topic;
    protected static boolean verbose = false;
    protected static String kafkaServer = "localhost:9094";
    protected static String producerSchemaFilename = "/opt/starc/avro/TCCDMDatum.avsc";
    protected static String producerID = "demoProducer";
    protected static String eom = "ENDOFMESSAGES";

    protected static String securityProtocol;
    protected static String truststoreLocation;
    protected static String truststorePassword;
    protected static String keystoreLocation;
    protected static String keystorePassword;
    protected static String sslKey;
    
    public static void main(String [] args){
        if(!parseArgs(args, false)) {
            usage();
            System.exit(1);
        }

        printConfig();
                
        KafkaProducer<String, TCCDMDatum> producer = createProducer();
        publishEOM(producer);
    }
    
    public static KafkaProducer<String, TCCDMDatum> createProducer() {
        Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
		props.put(ProducerConfig.CLIENT_ID_CONFIG, producerID);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringSerializer");
		// Use the graph serializer if a graph schema was passed in else use strings
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				com.bbn.tc.schema.serialization.kafka.KafkaAvroGenericSerializer.class);
    
		// additional serialization properties needed
		props.put(AvroConfig.SCHEMA_WRITER_FILE, producerSchemaFilename);
		props.put(AvroConfig.SCHEMA_SERDE_IS_SPECIFIC, true);

        // configure the following three settings for SSL encryption
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststoreLocation);
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword);

        // configure the following three settings for SSL authentication
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystoreLocation);
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePassword);
        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, sslKey);
		
		return new KafkaProducer<>(props);
    }
    
    public static boolean publishEOM(KafkaProducer<String, TCCDMDatum> producer) {
		logger.info("Publishing "+eom+" to "+topic);
		try {
			// Load the schema
			if (logger.isDebugEnabled()) {
				logger.debug("Using schema from "+producerSchemaFilename);
			}
			
			TCCDMDatum dummyRecord = createDummyRecord();
			ProducerRecord<String, TCCDMDatum> record = new ProducerRecord<>(topic, eom, dummyRecord);
			
			long sendStartMs = System.currentTimeMillis();
			if (logger.isDebugEnabled()) {
				logger.debug("Record published at "+sendStartMs);
			}
			producer.send(record).get();
			logger.info("Record successfully published");
			logger.info("Closing producer");
			producer.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			return false;
		}
    }

    public static TCCDMDatum createDummyRecord() {
    	Random random = new Random();
        com.bbn.tc.schema.avro.cdm20.UUID uuid = SchemaUtils.toUUID(random.nextLong());
    	PrincipalType type = PrincipalType.PRINCIPAL_LOCAL;

        InstrumentationSource source = InstrumentationSource.SOURCE_LINUX_SYSCALL_TRACE;
        String userId = "TA3-IGNORE";
        Principal principal = Principal.newBuilder()
                .setUuid(uuid)
                .setType(type)
                .setUserId(userId)
                .setUsername(userId)
                .setGroupIds(new ArrayList<CharSequence>())
                .setProperties(new HashMap<CharSequence, CharSequence>())
                .build();

    	TCCDMDatum datum = TCCDMDatum.newBuilder().setSource(source).setDatum(principal).build();
    	if (logger.isDebugEnabled()) {
    		logger.debug("Created Dummy record: "+datum.toString());
    	}
    	return datum;
    }
    	
    /**
     * Parse the argument list, doing some error checking for each specific argument type
     * If errorOnExtras is true, return false and abort processing if we get any unknown parameters
     * If errorOnExtras is false, allow unknown parameters by ignoring them.
     */
    protected static boolean parseArgs(String [] args, boolean errorOnExtras) {
        if(args.length < 1) {
            return false;
        }
        if(args[0].startsWith("-")){
            return false;
        }
        topic = args[0];
        if(topic == null || topic.isEmpty()) return false;
        
        int index = 1;
        //parse the options
        while(index < args.length){
            String option = args[index].trim();
            if (option.equals("-v")){
                /*
                 * Increase the log level for the kafka package logger to DEBUG.  We arbitrarily pick the
                 * Producer class to get the package name.
                 * TODO: if we run log4j 2, there is a different way to adjust the log level
                 */
                String kafkaPackage = Producer.class.getPackage().getName();
                Logger.getLogger(kafkaPackage).setLevel(Level.DEBUG);
                verbose = true;
            } else if (option.equals("-kv")) {
            	// Extra verbose, producers and consumers will write the content they're producing or consuming to the log file
            	// Also turn the Kafka logging to debug
            	Logger.getLogger("org.apache.kafka").setLevel(Level.DEBUG);
            	String kafkaPackage = Producer.class.getPackage().getName();
            	Logger.getLogger(kafkaPackage).setLevel(Level.TRACE);
            } else if(option.equals("-ks")){
                index++;
                kafkaServer = args[index];
                if(kafkaServer.startsWith("-")){
                    System.err.println("Bad kafka server");
                    return false;
                }
            } else if(option.equals("-psf")){
                index++;
                producerSchemaFilename = args[index];
                if(producerSchemaFilename.startsWith("-")){
                    System.err.println("Bad producer schema file name");
                    return false;
                } 
            } else if(option.equals("-pid")){
            	index++;
            	producerID = args[index];
            	if(producerID .startsWith("-")){
            		System.err.println("Bad producer id");
                        return false;
            	}
            } else if (option.equals("-eom")) {
            	index++;
            	eom = args[index];
            } else if (errorOnExtras) {
            	System.err.println("Bad option " + option);
            	return false;
            } else if (option.equals("-secproto")) {
                securityProtocol = args[index+1].trim();
            } else if (option.equals("-truststore")) {
                truststoreLocation = args[index+1].trim();
            } else if (option.equals("-trustpass")) {
                truststorePassword = args[index+1].trim();
            } else if (option.equals("-keystore")) {
                keystoreLocation = args[index+1].trim();
            } else if (option.equals("-keypass")) {
                sslKey = args[index+1].trim();
            } else if (option.equals("-sslkey")) {

            }
            index++;
        }
        return true;
    }

    protected static void usage() {
        logger.error("Main [<topics>] <options> where options \n" +
                "   <topics>  is a comma separated list of kafka topics to publish EndOfMessages to\n" +
                "   <options>  include the following:\n" +
                "     -ks   the kafka server needed by the consumer/producer for bootstrapping\n" +
                "     -psf  producer/writer avro graph record schema file \n" +
                "     -v    verbose (default false) \n" +
                "     -pid  producer id (default demoProducer)\n" +
                "     -eom  end of messages key (default ENDOFMESSAGES)\n" +
                "     -kv   kafka verbose (kafka logging to DEBUG)\n");               
    }

    protected static void printConfig(){
        char separator = '=';
        char fseparator = ',';

        StringBuffer buffer = new StringBuffer();
        buffer.append("topic").append(separator).append(topic).append(fseparator)
                .append("verbose").append(separator).append(verbose).append(fseparator)
                .append("kserver").append(separator).append(kafkaServer).append(fseparator)
                .append("produderSchema").append(separator).append(producerSchemaFilename).append(fseparator);

        logger.info(buffer.toString());
    }
    
}
