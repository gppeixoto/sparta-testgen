#!/bin/bash

##./compile.sh
./genAgent.sh

CP="bin"
for x in `ls libs/*.jar`
do
    CP="$CP:$x"
done

## Revisit this. -Marcelo
## This should be done all in one step but I got stuck in a classpath problem

PGM="instrumentation.examples.Foo"

java -cp $CP \
    -javaagent:iagent.jar \
    instrumentation.Wrapper \
    ${PGM} | awk '{$1=""; print $0}' > trace.out

java -cp $CP replayer.Main