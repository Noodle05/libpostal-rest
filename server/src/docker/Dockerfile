FROM noodle05/jpostal:latest
MAINTAINER Wei Gao<wgao@salesforce.com>

ADD server.tar.gz /opt
ADD start-server.sh /

ENV LP_SERVER_HOME=/opt/server

ENV JAVA_GC_OPTS="-XX:+UseG1GC"
ENV JAVA_HEAP_OPTS="-Xms1G -Xmx1G"
ENV JAVA_EXT_OPTS=""

EXPOSE 8080 8090

CMD [ "/start-server.sh" ]