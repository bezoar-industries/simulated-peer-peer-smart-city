#/bin/bash

export CLASSPATH=.:./build/classes/java/main

PORT=60000

REGISTRY=192.168.50.204
REGISTRY=10.5.28.6
REGISTRY=topeka

#LOGFILE=logs/csu.log
LOGFILE=

for i in {1..100}
do
  java -Xmx5M -Djava.util.logging.config.class=cs555.chiba.service.LogConfig -Dcsu.log.file=$LOGFILE cs555.chiba.node.Peer $REGISTRY $PORT 0 40 &
  sleep 0.1
done

