#!/bin/bash
# // Copyright (c) 2016 Raytheon BBN Technologies Corp. All rights reserved.

# Arguments:
#  1) path/to/avro/bin/file
#  2) Extra Arguments (optional)

topic=Test
if [[ "$#" > 0 ]]; then
   topic=$1
fi
echo "Consuming from topic $topic"

args=
if [[ "$#" > 1 ]]; then
   args="$2"
   echo "Using additional arguments $args"
fi

java -Dlog4j.debug=true -Dlog4j.configuration=nochecker.log4j.properties -cp .:target/kafkaclients-1.0-SNAPSHOT-jar-with-dependencies.jar com.bbn.tc.services.kafka.FileConsumer file:///$topic -np -psf ../ta3-serialization-schema/avro/TCCDMDatum.avsc -csf ../ta3-serialization-schema/avro/TCCDMDatum.avsc -rg -call -co earliest -cdm -c -roll 5000000 -wj -d 10000000 $args
