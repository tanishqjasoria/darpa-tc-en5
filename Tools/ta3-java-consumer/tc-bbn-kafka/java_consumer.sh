#!/bin/bash
# // Copyright (c) 2016 Raytheon BBN Technologies Corp. All rights reserved.

# Arguments:
#  1) Ambolute pathname to avro bin file
#  2) Arguments

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

java -Dlog4j.debug=true -Dlog4j.configuration=log4j.properties -cp .:target/kafkaclients-1.0-SNAPSHOT-jar-with-dependencies.jar com.bbn.tc.services.kafka.Main file://$topic -np -psf ../ta3-serialization-schema/avro/TCCDMDatum.avsc -csf ../ta3-serialization-schema/avro/TCCDMDatum.avsc -cdm -rg -co earliest $args
