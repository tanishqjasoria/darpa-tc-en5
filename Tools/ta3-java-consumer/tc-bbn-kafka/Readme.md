# Pre-requisites (download and install kafka)

### Install kafka binaries 

 * Download the kafka kafka_2.11-0.9.0.0 binary from
 http://kafka.apache.org/downloads.html
 This version of kafka included the latest client APIs
 for efficiency and portability.

 * Untar the download to kafka_2.11-0.9.0.0/
  This is the kafka home directory (contains the binaries)
  You should be all set.

Alternatively, if you prefer to build kafka binaries
from source then follow the steps below:

### Build kafka from source

Here is how to build it from source and install it in maven locally

 * Get the latest kafka source
 ```
 git clone https://github.com/apache/kafka.git
 ```
 As of this writing, the version is 0.9.0.0
 
 * Build it using gradle
 ```
 cd kafka
 gradle -PscalaVersion=2.11.7 clean install releaseTarGz
 ```
  The install task will install kafka to local maven repo
  The releaseTarGz will generate kafka binaries under 
  core/build/distributions
  This is where the kafka and zookeeper executables can be found but 
  you need to untar it first
 ```
 tar xvf core/build/distributions/kafka_2.11-0.9.0.0.tgz
 ```
 This is the kafka home directory which contains the binaries
 ```
 cd kafka_2.11-0.9.0.0
 ```

 Now we can use the generated kafka jars in our project directly using 
 maven


#  Building the Kafka clients (producer and consumer)

If you dont care about kafka (for testing) and just wish to exercise
 avro by publishing and consuming to/from file, skip to section 
 [producing-and-consuming-tofrom-a-file](#producing-and-consuming-tofrom-a-file)
 
 * First, build the project
 
 ```
 cd <path to kafkaclients project>
 mvn assembly:assembly
 ```

 This creates the jar with all dependencies

# Testing: Producing and Consuming to/from kafka topic

We assume that we have downloaded and install kafka 0.9.0.0 
(instructions above)

 * cd to the kafka home directory
 ```
 cd kafka_2.11-0.9.0.0
 ```
 
 * Start zookeeper
 ```
 ./zookeeper-server-start.sh ../config/zookeeper.properties
 ```
 * Start kafka server
   ```
    ./kafka-server-start.sh ../config/server.properties
   ```
 * Create the kafka topic, 1 partition, no replication
   ```
   ./kafka-topics.sh --zookeeper localhost:2181 --create --topic test-1-1 --partitions 1 --replication-factor 1
   ```
 * In a separate terminal, cd to the project directory
  ```
  cd kafkaclient
  ```

 * Different command line params may be passed, to see the usage 
   configuration and options
 ```
 java -jar target/kafkaclient-1.0-SNAPSHOT-jar-with-dependencies.jar
 ```

We show first how to start a consumer process and a separate producer 
process. The producer publishes one record at a time synchronously.
The consumer fetches the records and displays the end-to-end latency 
statistics. A duration is passed to both processes to terminate after 
d seconds from start

* Start a consumer only process as follows

 ```
 java -jar target/kafkaclients-1.0-SNAPSHOT-jar-with-dependencies.jar \  
    test-1-1 -ks localhost:9092 -np -g group1 \  
    -csf ../../ta3-serialization-schema/avro/TCCDMDatum.avsc
 ```
  
  Parameter _-v_ for verbose to print received records (not recommended 
   when you are measuring latency). Note that we pass in the 
   writer/producer and reader/consumer graph schemas for de/serialization 
   of records In the above the reader schema is an evolved version of 
   the writer schema (avro handles conversion)
   The schemas can be checked out from our git repo.
   You can use parameter _-offset_ to specify what offset in the topic
    to start consuming from. So to consume all records from the beginning
    specify _-offset 0_

   Note that if .avsc files are nonexistent in 
   ../../ta3-serialization-schema/avro, the tc-schema package needs 
   to be built by executing the following commands in 
   the ../../ta3-serialization-schema directory. 
   ```
   mvn exec:java
   mvn install
   ```
   The first command builds the .avsc schema files. 
   The second one builds the java package based on the derived .avsc files.

  Note that if the consumer schema is different than the producer schema, 
  we can specify both and avro automatically handles resolution. 
  For example, assuming we have CDMv10 and CDMv11 schemas and the 
  producer produced the data with CDM11 and the consumer is trying to 
  consume it with CDM11:
  ```
   $ java -jar target/kafkaclients-1.0-SNAPSHOT-jar-with-dependencies.jar \  
       test-1-1 -ks localhost:9092 -np -g group1 \  
       -psf ../../ta3-serialization-schema/avro/TCCDMDatum10.avsc \  
       -csf ../../ta3-serialization-schema/avro/TCCDMDatum11.avsc
  ```
  Start a producer only process as follows
  ```
   java -jar target/kafkaclients-1.0-SNAPSHOT-jar-with-dependencies.jar \  
         test-1-1 -nc -ks localhost:9092  -d 10 \  
         -psf ../../ta3-serialization-schema/avro/TCCDMDatum.avsc -n 10
   ```
   The -n 10 means for each node record published, add 10 
   arbitrary k/v pairs (to test record size)
   
   You can start both a producer and a consumer in the same process
   ```
   java -jar target/kafkaclient-1.0-SNAPSHOT-jar-with-dependencies.jar test
   ```
   
   Increase memory of the JVM by passing -Xmx512M
   
   To run the consumer that ships with kafka
   
   * cd to the kafka bin directory
   
   ```
   ./kafka-console-consumer.sh --zookeeper localhost:2181 --topic test --from-beginning
   ```

# Producing and Consuming to/from a file

  This describes how to bypass kafka and produce and consume directly 
  to/from files. This can be useful for testing.
  
  If you the topic passed to the producer/consumer has format 
  _file:<path>_
  this will automatically instruct the producer to publish the records 
  to a file in binary Avro instead of publishing to a kafka topic. 
  For example, 
  
  * to start a producer to publish 10 random records to file testFile.avro:
  ```
  java -jar target/kafkaclients-1.0-SNAPSHOT-jar-with-dependencies.jar \  
           file:testFile.avro -nc -c -mr 10 \  
           -psf ../../ta3-serialization-schema/avro/TCCDMDatum.avsc -vv
  ```
  Here _mr_ means publish max of 10 records, _c_ means use compiled
    record generation (instead of generic)  
  By default this will generate avro binary. You can specify to generate
  json instead using -wj flag
  
  
  * To start a consumer to consume the same 10 records directly from 
  the file (instead of from kafka), _c_ means use compiled
   record deserialization (instead of generic) i.e., records will be
   compiled objects when deserialized instead of generic maps
  ```
  java -jar target/kafkaclients-1.0-SNAPSHOT-jar-with-dependencies.jar \  
    file:testFile.avro -np -c \  
    -csf ../../ta3-serialization-schema/avro/TCCDMDatum.avsc -vv
  ```
  
  * We extended the consumer, creating a NewCDMConsumer which is 
   specifically intended to process CDM records in case this is helpful
   starting point for the TA1/2 performers. To start the CDM consumer
   pass the _cdm_ flag on the command line as follows:
   ```
   java -jar target/kafkaclients-1.0-SNAPSHOT-jar-with-dependencies.jar file:testFile.avro -np -cdm -csf ../../ta3-serialization-schema/avro/TCCDMDatum.avsc -v
   ```

  * Consuming from the stored avro binary files on 
    files.tc.bbn.com:/data/avro_files

    Currently, you'll still need to provide the schema as an input, eventually
    there will be an API method that loads the schema from the binary file.
    For now, the schema version that these saved avro binary files were 
    serialized using is stored in the root /data/avro_files/EngagementX
    Pass in the -rsize 1 parameter to bypass benign error messages.
    
    ```
    java -jar target/kafkaclients-1.0-SNAPSHOT-jar-with-dependencies.jar file:ta1-cadets-cdm13_bovia.bin -np -csf TCCDMDatum13.avsc -rsize 1 -vv
    ```
  
  * Consuming from a stored avro binary file using the FileConsumer main 
  
    This method provides the functionality to write JSON
    See the help for other command line parameters (-help)
  
    ```
    java -cp target/kafkaclients-1.0-SNAPSHOT-jar-with-dependencies.jar com.bbn.tc.services.kafka.FileConsumer file:ta1-cadets-cdm13_bovia.bin -np -csf TCCDMDatum13.avsc -rsize 1 -vv -wj -odir /tmp
    ```
  
  


#   Our E2E LATENCY TEST

Run both a producer and a consumer on the same machine (same clock)

First consumer (runs for 20 seconds and prints latency output)
```
java -jar target/kafkaclient-1.0-SNAPSHOT-jar-with-dependencies.jar test-1-1 -ks localhost:9092 -np -g group1 -d 20 -csf ../schemas/test/LabeledGraph.avsc
```

Then producer quickly after
```
java -jar target/kafkaclient-1.0-SNAPSHOT-jar-with-dependencies.jar test-1-1 -nc -ks localhost:9092 -d 10 -psf ../schemas/test/LabeledGraph.avsc -n 10 
```
 When you stop the consumer (even using Ctrl+C), the stats will print.
 
 Play with the n to simulate larger record sizes

#   KAFKA's PERFORMANCE TESTING

To run the performance tests as described in
   http://kafka.apache.org/documentation.html#quickstart
   
 * cd to the kafka bin directory

 * Create the kafka topic, 6 partitions, no replication
 
  ```
  ./kafka-topics.sh --zookeeper localhost:2181 --create --topic test-6-1 --partitions 6 --replication-factor 1
  ```  
  
  In a cluster setting, we can see which partitions are on which brokers by running
  
  ```
  ./kafka-topics.sh --describe --zookeeper localhost:2181 --topic test-6-1
  ```
 

 * Run the producer performance test
  ```
  ./kafka-run-class.sh org.apache.kafka.clients.tools.ProducerPerformance test-6-1 50000000 100 -1 acks=1 bootstrap.servers=localhost:2181 buffer.memory=67108864 batch.size=8196
  ```
    Params in order are <topic> <numRecords> <recordSize> <throughput rec/sec> [<prop=value>]
    acks=1 means get an ack from leader for each publish (async replication)
    For producer params see http://kafka.apache.org/documentation.html#newproducerconfigs
    The above sends 50 million records each of size 100 bytes synchronously fastest possible
   
   # ----- Output ------
   4608294 records sent, 921658.8 records/sec (87.90 MB/sec), 298.3 ms avg latency, 605.0 max latency.
   7539709 records sent, 1507941.8 records/sec (143.81 MB/sec), 1.6 ms avg latency, 6.0 max latency.
   7696050 records sent, 1539210.0 records/sec (146.79 MB/sec), 1.5 ms avg latency, 5.0 max latency.
   7721707 records sent, 1544341.4 records/sec (147.28 MB/sec), 1.5 ms avg latency, 6.0 max latency.
   5674187 records sent, 1134837.4 records/sec (108.23 MB/sec), 10.1 ms avg latency, 91.0 max latency.
   


 * Run the consumer performance test
  ```
  ./kafka-consumer-perf-test.sh --zookeeper localhost:2181 --messages 50000000 --topic test-6-1 --threads 1
  ```
   run with no args to get explanation of the params you can pass
   
   ----- Output ------
   # start.time, end.time, fetch.size, data.consumed.in.MB, MB.sec, data.consumed.in.nMsg, nMsg.sec
   # 2015-09-09 18:20:42:007, 2015-09-09 18:21:23:929, 1048576, 4768.3716, 129.1472, 50000000, 1354206.1643
   

 * To test end-to-end latency
  ```
  ./kafka-run-class.sh kafka.tools.TestEndToEndLatency localhost:9092 localhost:2181 test-6-1 5000 10000 1
  ```
  params are <broker_list> <zookeeper_connect> <topic> <num_messages> <consumer_fetch_max_wait> <producer_acks>
  
   # ----- Output ------
   # 0	49.547
   # 1000	0.729
   # 2000	0.797
   # 3000	0.556
   # 4000	0.503
   # Avg latency: 0.6168 ms
  


#  Other Useful References
http://engineering.linkedin.com/kafka/benchmarking-apache-kafka-2-million-writes-second-three-cheap-machines
