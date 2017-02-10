#!/bin/bash

trap 'kill -TERM $PID' TERM INT
/usr/bin/java -jar -server ${JAVA_GC_OPTS} \
    ${JAVA_HEAP_OPTS} -DhttpPort=8080 \
    -DtcpPort=8090 ${JAVA_EXT_OPTS} \
    "${LP_SERVER_HOME}/server.jar" &
PID=$!
wait $PID
trap - TERM INT
wait $PID
EXIT_STATUS=$?
