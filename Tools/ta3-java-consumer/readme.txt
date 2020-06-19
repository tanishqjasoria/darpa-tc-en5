# Avro Schema

Available at ta3-serialization-schema/avro/TCCDMDatum.avsc

Documentation is in the avdl file at ta3-serialization-schema/avro/CDM18.avdl

# Compiling Java consumers
## Requirments
1. maven
2. jdk 1.8

# Building
1. ta3-serialization-schema
  a. mvn clean exec:java
  b. mvn install

2. tc-bbn-avro
  a. mvn clean install

3. tc-bbn-kafka
  a. mvn assembly:assembly

# Running

0. Untar avro binary files (Engagement 5 data)

1. Convert to json
  a. cd tc-bbn-kafka
  b. ./json_consumer.sh /full/absolute/path/to/avro/binary/file

2. Quick semantic checker spot check
  a. cd tc-bbn-kafka
  b. ./spot-check-consumer.sh /full/absolute/path/to/avro/binary/file

3. Full java consumer with all options available
  a. cd tc-bbn-kafka
  b. ./java-consumer.sh /full/absolute/path/to/avro/binary/file
     Use -h for options
