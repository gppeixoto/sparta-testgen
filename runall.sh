#!/bin/bash

## example programs
PGMS=(
"instrumentation.examples.Arith"
"instrumentation.examples.IntArrays"
"instrumentation.examples.ControlFlow"
"instrumentation.examples.ObjectAllocation"
"instrumentation.examples.RefArrayAllocation"
"instrumentation.examples.RefArrays"
"instrumentation.examples.Enum"
"instrumentation.examples.Switch"
"instrumentation.examples.StaticRef"
#"instrumentation.examples.LibraryClasses"
)

## compile all the code outside Eclipse
./compile.sh

## generate instrumentation agent iagent.jar
./genAgent.sh


for var in "${PGMS[@]}"
do
  echo "${var}"
  ./run.sh "${var}"
done


