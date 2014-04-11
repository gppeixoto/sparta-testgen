#!/bin/bash


CP="bin"
for x in `ls libs/*.jar`
do
    CP="$CP:$x"
done

java -cp $CP \
    -javaagent:iagent.jar \
    instrumentation.examples.Sample