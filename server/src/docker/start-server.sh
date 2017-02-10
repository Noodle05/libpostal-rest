#!/bin/sh

exec \
    /usr/bin/java -jar -server ${JAVA_GC_OPTS} \
    ${JAVA_HEAP_OPTS} -DhttpPort=8080 \
    -DtcpPort=8090 ${JAVA_EXT_OPTS} \
    "${LP_SERVER_HOME}/server.jar"