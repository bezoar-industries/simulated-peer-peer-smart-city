#!/bin/env bash

if [ "$#" -ne 1 ]; then
echo "Usage:sh startupNoShell.sh <machine_list>"
exit
fi

CURRENT_WORKING_DIR=~/CS555-final

START_CLUSTER="cd $CURRENT_WORKING_DIR; nohup nice -n 19 ./lotsofpeers.sh 2>&1 &"

for cluster in $(cat $1)
do
  echo 'logging into '$cluster
  COMMAND="ssh $cluster \"$START_CLUSTER\""
  echo $COMMAND
  eval $COMMAND &
done

