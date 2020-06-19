#!/bin/bash
# // Copyright (c) 2016 Raytheon BBN Technologies Corp. All rights reserved.

# Arguments:
#  1) Ambolute pathname to avro bin file
#  2) Arguments

. java_consumer.sh $1 "-check -mr 1000 -vv"
