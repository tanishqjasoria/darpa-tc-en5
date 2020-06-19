Why Avro?
-------
Avro has several interesting properties (advantages)
relative to other mainstream serialization frameworks
out there (like thrift and protobuf)

First it decouples the schema from the serialized data.
This means the serialized data is smaller in size since it
 does not have to have the typing information. Any time
 the data is read, the schema is also available so
 the data is self-descriptive.
 This also means that schema can evolve independent of
 the data (which is not itself typed). Separation of data
 structure and data itself is very powerful.
 An importatnt concept in Avro is that the reader schema
 (schema used to read the data) can be different than the
 writer schema (schema used to write the data). Avro takes
 care of schema resolution.
 The implication of the above is that we always need to know
 the schema
 with which the data was written. Typically, a schema-registry
 can be used to do so for record byte serialization (when the
 schema is not actually persisted with the data).
 The fact that you need to know the reader and writer schemas
 makes schemas central to the operation and the idea of a
 central registry keeps everyone synchronized.

Second, the above means that we dont need schema compilers
 (code generation) such as with thrift and protobuf.
 Instead the data is dynamically typed using generic and
 simple APIs. Note that Avro also supports compilation
 to generate typed records if that is desired instead for
 increased performance.

Most importantly, schemas can evolve very easily over time
 without code generation -- again since the data and schema
 are independent and data with schema are self-descriptive
 and this happens at deserialization time.

****************
* About this test
****************

This test is intended to demonstrate avro serialization
and deserialization to bytes and files
We highlight many of the intricacies and important subtle
APIs. Specifically, we demonstrate
 - schema creation, and serialization and deserialization
 - how data is serialized and deserialized to/from bytes
   and files
 - serialization of batch of records (file contains schema)
 - serialization of bytes (do not contain schema), so here
   is is critical to specify the writer and the reader schemas
 - we show how avro automatically does schema resolution
   given the writer schema and the reader schema (for both
   bytes and files)

The single record byte serialization is important because we
 intend to use it in kafka
We will build the kafka hooks independently

We recommend that developers examine the unit tests
to get a feel of the functionality

We have also implemented a test that may be run
with this module as explained next

*****************
* BUILD / INSTALL
*****************
Change to the project directory
 $ mvn clean install

To create the jar and run the example
 $ mvn assembly:assembly

****************
* Benchmarking
***************
To run the serialization benchmark on the bitcoin dataset
 bitcoin_with_header.csv

 $ java -jar \
   target/tc-avro-1.0-SNAPSHOT-jar-with-dependencies.jar \
   ~/Downloads/bitcoin_with_header.csv \
   /tmp/bitcoin.avro

  This will read the csv bitcoin edge data, parse it, create edges
  according to the bundled schema LabeledEdge.avsc and serialize
  the records to file bitcoin.avro. In the process we measure and
  report stats.
  To limit the number of records to process, use the -n parameter
  (e.g., "-n 1000000" processes only the first million records).

In order to benchmark deserialization as well set -d flag. Here
 is it recommended to set the -n as well since deserialization
 writes the deserialized records to stdout. So add
      -d -n 1000000

****************
* TODO
***************
- Implement the schema registry for automatic lookup
  of record schema instead of having to instantiate
  the serializer with a schema (it will auto detect)

****************
* References
***************

Apache avro documentation is including APIs
 https://avro.apache.org/docs/current/gettingstartedjava.html

For schema evolution (resolution), see
 https://issues.apache.org/jira/browse/AVRO-1661
 This describes how to set the reader and writer schemas

Also look at Flume data sink avro, and Confluent.io avro

