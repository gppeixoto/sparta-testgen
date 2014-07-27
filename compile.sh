#!/bin/bash

BIN=`pwd`/bin

CP="$BIN"
for x in `ls libs/*.jar`
do
    CP="$CP:$x"
done

mkdir -p $BIN

find . -name "*.java" | \
    xargs javac -cp $CP -d $BIN
