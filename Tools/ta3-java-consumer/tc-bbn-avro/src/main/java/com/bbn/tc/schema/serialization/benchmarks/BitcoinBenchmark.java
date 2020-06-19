/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema.serialization.benchmarks;

import com.bbn.tc.schema.serialization.Utils;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;

/**
 * A serialization benchmark for the bitcoin dataset <br>
 * Reads a large csv dataset of bitcoin edges of the form <br>
 *  from,to,amount <br>
 *  0,1,5000000000 <br>
 *  ... <br>
 *  creates edges according to the provided schema and serializes
 *  those to an output file, then reads the serialized file and
 *  deserializes it back and prints to stdout
 * @author jkhoury
 */
public class BitcoinBenchmark {
    public static final Logger logger = Logger.getLogger(BitcoinBenchmark.class);
    public static final int SAMPLES = 10000;

    private final String bitcoinFilename;
    private final String schemaFilename = "LabeledEdge.avsc";
    private final String serializedFilename;
    private final boolean deserializeFile;
    private final boolean hasHeader;
    private final int n;

    // state variables
    GenericDatumWriter genericDatumWriter;
    DataFileWriter<GenericData.Record> fileWriter;
    GenericDatumReader genericDatumReader;
    DataFileReader<GenericData.Record> fileReader;
    Schema edgeSchema;
    File bitcoinFile, serializedFile;

    //statistics
    Utils.Stats recordCreationStat, recordSerializationStat, recordBytesStat, recordDeserStat;


    public BitcoinBenchmark(String bitcoinFilename, String serializedFilename,
                            boolean deserializeFile, boolean hasHeader, int numRecords) {
        this.bitcoinFilename = bitcoinFilename;
        this.serializedFilename = serializedFilename;
        this.deserializeFile = deserializeFile;
        this.hasHeader = hasHeader;
        this.n = numRecords;
        recordCreationStat = new Utils.Stats(SAMPLES, "Edge Creation", "ms");
        recordSerializationStat = new Utils.Stats(SAMPLES, "Edge Serialization", "ms");
        recordDeserStat = new Utils.Stats(SAMPLES, "Edge Deserialization", "ms");
        recordBytesStat = new Utils.Stats(SAMPLES, "Serialized Record Bytes", "bytes");
    }

    public void init() throws Exception{
        bitcoinFile = new File(this.bitcoinFilename);
        serializedFile = new File(this.serializedFilename);

        if(bitcoinFile == null || !bitcoinFile.exists()) {
            logger.error("File does not exist "+bitcoinFile.getAbsolutePath());
            return;
        }
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(schemaFilename);
        if(is == null || is.available() <= 0) {
            logger.error("Schema file not available "+schemaFilename);
            return;
        }
        if(serializedFile == null) {
            logger.error("Serialized file is null ");
            return;
        }
        if (!serializedFile.exists()) serializedFile.createNewFile();

        edgeSchema = Utils.loadSchema(is);

        // Set up the edge serialization
        genericDatumWriter = new GenericDatumWriter(edgeSchema);
        genericDatumReader = new GenericDatumReader(edgeSchema); //same schema as writer
        fileWriter = new DataFileWriter(genericDatumWriter);
        fileWriter.create(edgeSchema, serializedFile);

    }

    public void start(){

        BufferedReader br = null;
        String line=null;
        GenericData.Record [] edgeRecords = null;
        long start, ssample, rsample, nbytes;
        try {

            init();

            br = new BufferedReader(new FileReader(bitcoinFile));
            edgeRecords = new GenericData.Record[SAMPLES];

            if(this.hasHeader) line = br.readLine(); // skip first line

            if(line == null) {
                logger.info("Empty file, exiting");
                return;
            }
            /**
             * Serialization
             */
            // reset
            int count = 0, totalcount=0;
            start = System.currentTimeMillis();

            while((line=br.readLine()) != null){
                edgeRecords[count] = parseLine(line);
                if(edgeRecords[count] == null)
                    logger.warn("Null record generated from " + line);
                count ++;
                totalcount++;
                if(count == SAMPLES){
                    rsample = System.currentTimeMillis() - start;
                    recordCreationStat.sample(rsample);
                    //flush the batch of records and determine how many bytes we wrote
                    nbytes = serializedFile.length();
                    start = System.currentTimeMillis();
                    flushRecords(edgeRecords, count);
                    ssample = System.currentTimeMillis() - start;
                    recordSerializationStat.sample(ssample);
                    nbytes = serializedFile.length() - nbytes;
                    recordBytesStat.sample(nbytes);
                    logger.info("Processed "+totalcount + " records: serSample="+ssample
                            +", createSample="+rsample +", bytes written="+nbytes);
                    // reset
                    count =0;
                    start = System.currentTimeMillis();
                }
                if(totalcount == this.n) break;
            }
            //flush the remaining batch
            flushRecords(edgeRecords, count);

            // print the serialization stats
            logger.info(recordSerializationStat);
            logger.info(recordCreationStat);
            logger.info(recordBytesStat);


            if(!deserializeFile) return;



            fileReader = new DataFileReader<GenericData.Record>(serializedFile, genericDatumReader);
            /**
             * Deserialization
             */
            logger.info("Starting deserialization of avro file ");
            count = 0;
            totalcount=0;
            start = System.currentTimeMillis();
            while(fileReader.hasNext()){
                edgeRecords[count] = fileReader.next(null);
                count ++;
                totalcount ++;

                if(count == SAMPLES){
                    ssample = System.currentTimeMillis() - start;
                    recordDeserStat.sample(ssample);
                    // print  the records to stdout
                    //printRecords(edgeRecords, count);
                    logger.info("Processed "+totalcount + " records: deserSample="+ssample);
                    // reset
                    count = 0;
                    start = System.currentTimeMillis();
                }
            }
            logger.info(recordDeserStat);


        }catch(Exception e){
            logger.error(e);
        }finally{
            try {
                if (br != null) br.close();
                if (fileWriter != null) fileWriter.close();
            }catch(Exception e){logger.error(e);}
        }

    }

    private void printRecords(GenericData.Record[] edgeRecords, int count) throws Exception{
        for(int i=0;i<count;i++){
            System.out.println(edgeRecords[i]);
        }
    }

    private void flushRecords(GenericData.Record [] edgeRecords, int count) throws Exception{
        for(int i=0;i<count;i++)
            fileWriter.append(edgeRecords[i]);
        fileWriter.flush();
    }

    private GenericData.Record parseLine(String line){
        String [] parts = line.split(",");
        if(parts == null || parts.length != 3) {
            logger.warn("Bad record " + line);
            return null;
        }
        GenericRecordBuilder erb = new GenericRecordBuilder(edgeSchema);
        GenericRecordBuilder nrb = new GenericRecordBuilder(edgeSchema.getField("fromNode").schema());
        return erb.set("label", "generated").
                set("fromNode", nrb.set("id", Long.parseLong(parts[0])).set("label", "artifact").build()).
                set("toNode",   nrb.set("id", Long.parseLong(parts[1])).set("label", "artifact").build()).build();
    }

    public static void main (String [] args){
        String _bitcoinFile=null, _serializedFile=null, _schemaFile=null;
        boolean _deserialize=false, _header=true;
        int _n=0;

        // --- parse the arguments
        if(args.length < 2) {
            usage();
            System.exit(1);
        }
        if(args[0].startsWith("-") || args[1].startsWith("-")){
            usage();
            System.exit(1);
        }
        _bitcoinFile = args[0];
        _serializedFile = args[1];

        int index = 1;
        //parse the options
        while(index < args.length) {
            String option = args[index].trim();
            if (option.equals("-nh")) {
                _header = false;
            } else if (option.equals("-d")) {
                _deserialize = true;
            } else if (option.equals("-n")) {
                try {
                    _n = Integer.parseInt(args[++index]);
                }catch(Exception e){logger.error(e);}
            }
            index++;
        }

        // start the benchmark
        BitcoinBenchmark benchmark = new BitcoinBenchmark(_bitcoinFile,
                _serializedFile, _deserialize, _header, _n);
        benchmark.start();
    }

    public static void usage(){
        logger.error("BitcoinBenchmark <bitcoinFilename> <serializedFilename> <options> where options \n" +
                "     -nh  no header (default false)\n" +
                "     -n   max number of records to process (default 0=all)\n" +
                "     -d   also deserialize the avro serialized file (default false)\n");
    }
}
