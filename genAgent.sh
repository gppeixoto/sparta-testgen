#!/bin/bash

CURRDIR=`pwd`

(cd ${CURRDIR}/bin
    find . -name "*.class" | \
        grep "callret" | \
        xargs jar cvfm ${CURRDIR}/iagent.jar ${CURRDIR}/manifest.mf 
)
