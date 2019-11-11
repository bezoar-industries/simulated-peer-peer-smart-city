#/bin/bash

gradle assemble

PORT=60000
REGISTRY=192.168.50.204

#LOGFILE=logs/csu$NICKNAME.log
LOGFILE=

java -Xdebug -Xrunjdwp:transport=dt_socket,address=12346,server=y,suspend=y -Djava.util.logging.config.class=cs555.chiba.service.LogConfig -Dcsu.log.file=$LOGFILE cs555.chiba.node.Peer $REGISTRY $PORT

#java -Djava.util.logging.config.class=cs555.chiba.service.LogConfig -Dcsu.log.file=$LOGFILE cs555.chiba.node.Peer $REGISTRY $PORT
