#!/bin/env bash


KILL_PROCESSES="PID=\\\$(jps | awk '/Peer|Registry/{print \\\$1}'); kill -9 \\\${PID}"

for cluster in $(cat workers.txt)
do
  echo 'logging into '$cluster
  COMMAND="ssh $cluster \"$KILL_PROCESSES\""
  #echo $COMMAND
  eval $COMMAND &
done
