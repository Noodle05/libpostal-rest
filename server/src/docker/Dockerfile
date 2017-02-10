FROM noodle05/jpostal:latest
MAINTAINER Wei Gao<wgao@salesforce.com>

ENV LP_SERVER_HOME=/opt/server

ENV JAVA_GC_OPTS="-XX:+UseG1GC"
ENV JAVA_HEAP_OPTS="-Xms1G -Xmx1G"
ENV JAVA_EXT_OPTS=""

ADD start-server.sh /
ADD server.tar.gz /opt

EXPOSE 8080 8090

CMD [ "/start-server.sh" ]
