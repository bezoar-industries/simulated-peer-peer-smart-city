#/bin/bash

export CLASSPATH=.:./build/classes/java/main

gradle assemble

PORT=60000

REGISTRY=192.168.50.204
REGISTRY=10.5.28.6
REGISTRY=topeka

#LOGFILE=logs/csu.log
LOGFILE=

#java -Xdebug -Xrunjdwp:transport=dt_socket,address=12346,server=y,suspend=y -Djava.util.logging.config.class=cs555.chiba.service.LogConfig -Dcsu.log.file=$LOGFILE cs555.chiba.node.Peer $REGISTRY $PORT 0

java -Xmx5M -Djava.util.logging.config.class=cs555.chiba.service.LogConfig -Dcsu.log.file=$LOGFILE cs555.chiba.node.Peer $REGISTRY $PORT 0 40
