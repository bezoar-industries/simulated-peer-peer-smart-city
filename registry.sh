#!/bin/bash

gradle assemble

PORT=60000
#LOGFILE=logs/csuServer.log
LOGFILE=



#java -Xdebug -Xrunjdwp:transport=dt_socket,address=12345,server=y,suspend=n -Djava.util.logging.config.class=cs555.chiba.service.LogConfig -Dcsu.log.file=$LOGFILE cs555.chiba.registry.RegistryNode $PORT

java -Djava.util.logging.config.class=cs555.chiba.service.LogConfig -Dcsu.log.file=$LOGFILE cs555.chiba.registry.RegistryNode $PORT
