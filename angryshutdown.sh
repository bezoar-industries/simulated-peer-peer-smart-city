#!/bin/env bash

# This will kill -9 the process which means it won't clean up its sockets.
# That is a resource leak that takes the OS a very long time to clean up.
# Use the regular shutdown.sh first.  It uses regular kill that allows our
# shutdown hook to clean up our sockets and threads.

if [ "$#" -ne 1 ]; then
echo "Usage:sh startupNoShell.sh <machine_list>"
exit
fi

KILL_PROCESSES="PID=\\\$(jps | awk '/Peer|Registry/{print \\\$1}'); kill -9 \\\${PID}"

for cluster in $(cat $1)
do
  echo 'logging into '$cluster
  COMMAND="ssh $cluster \"$KILL_PROCESSES\""
  #echo $COMMAND
  eval $COMMAND &
done
