#!/bin/env bash

# This will send the SIGTERM signal to our peer processes.  They will catch the
# signal in the shutdown hook.  This will give them the time to clean up the
# sockets and threads.  This is the nice way to do it.

KILL_PROCESSES="PID=\\\$(jps | awk '/Peer|Registry/{print \\\$1}'); kill \\\${PID}"

for cluster in $(cat workers.txt)
do
  echo 'logging into '$cluster
  COMMAND="ssh $cluster \"$KILL_PROCESSES\""
  #echo $COMMAND
  eval $COMMAND &
done
