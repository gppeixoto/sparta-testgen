#!/bin/bash

## example programs
PGMS=(
#"examples.Arith"
#"examples.IntArrays"
#"examples.ControlFlow"
#"examples.ObjectAllocation"
#"examples.RefArrayAllocation"
#"examples.RefArrays"
#"examples.Enum"
#"examples.Switch"
#"examples.StaticRef"
"examples.Retangulo"
#"examples.Celular"
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


