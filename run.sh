#!/bin/bash

if [ $# -eq 0 ]; then
    echo "No arguments supplied"
    exit
fi

PGM=$1 ## provide main function to run

## adds all necessary jar file in CP variable
CP="bin"
for x in `ls libs/*.jar`
do
    CP="$CP:$x"
done

## TRACE GENERATION
##  - executes the program with instrumentation agent
##  - produce execution trace on output file trace.out
##bin:libs/asm-all-5.0.2.jar
java -cp ${CP} \
    -javaagent:iagent.jar \
    -noverify \
    instrumentation.Wrapper ${PGM}  > trace.out

java -cp $CP replayer.Main