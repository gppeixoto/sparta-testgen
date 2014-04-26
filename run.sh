#!/bin/bash

## example program
#PGM="instrumentation.examples.Arith"
#PGM="instrumentation.examples.ControlFlow"
#PGM="instrumentation.examples.ObjectAllocation"
#PGM="instrumentation.examples.IntArrays"
#PGM="instrumentation.examples.RefArrayAllocation"
#PGM="instrumentation.examples.RefArrays"
PGM="instrumentation.examples.Switch"


## compile all the code outside Eclipse
./compile.sh

## generate instrumentation agent iagent.jar
./genAgent.sh

## adds all necessary jar file in CP variable
CP="bin"
for x in `ls libs/*.jar`
do
    CP="$CP:$x"
done

## TRACE GENERATION
##  - executes the program with instrumentation agent
##  - produce execution trace on output file trace.out
java -cp $CP \
    -javaagent:iagent.jar \
    instrumentation.Wrapper ${PGM}  > trace.out

java -cp $CP replayer.Main