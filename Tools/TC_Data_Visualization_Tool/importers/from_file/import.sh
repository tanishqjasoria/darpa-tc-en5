#!/bin/bash
# // Copyright (c) 2016 Raytheon BBN Technologies Corp. All rights reserved.

# Arguments:
#  1) Input file
#  2) Host
#  3) Port
#  4) Verbosity 
#  5) Output Filename

topic=Test
if [[ "$#" > 0 ]]; then
   topic=$1
fi
echo "Consuming from file $topic"

args=
if [[ "$#" > 1 ]]; then
   args="$2"
   echo "Using additional arguments $args"
fi

schema=TCCDMDatum.avsc

outfile=None
if [[ "$#" > 2 ]]; then
   outfile=$3
   echo "Using outfile from second arg: $3"
else
   prefix=ImportJob
   NOW=$(date +"%F_%H-%M-%S")
   outfile=$prefix-$NOW.txt
   echo "Using outfile: $outfile"
fi

java -Dlog4j.debug=true -cp .:tc-das-importer-1.0-SNAPSHOT-jar-with-dependencies.jar main.java.com.bbn.tc.DASImporter $1 $schema $2 $3 > $outfile 2>&1 &

sleep 1

tail -f $outfile

