tar xvfz ta3-java-consumer.tar.gz
cd ta3-java-consumer

cd ta3-serialization-schema
mvn clean exec:java
mvn install
cd ..

cd tc-bbn-avro
mvn clean install
cd ..

cd tc-bbn-kafka
mvn assembly:assembly
cd ..
cd ..
