#!/bin/env bash


CURRENT_WORKING_DIR=~/CS555-final

START_CLUSTER="cd $CURRENT_WORKING_DIR; nohup nice -n 19 ./lotsofpeers.sh 2>&1 &"

for cluster in $(cat workers.txt)
do
  echo 'logging into '$cluster
  COMMAND="ssh $cluster \"$START_CLUSTER\""
  echo $COMMAND
  eval $COMMAND &
done

