#!/bin/bash

if [ -z $1 ]
then
	NSERVER=8
else
	NSERVER=$1
fi

if [ -z $2 ]
then
	NOBJSERVER=10
else NOBJSERVER=$2
fi

if [ -z $3 ]
then
	NCLIENT=4
else
	NCLIENT=$3
fi

if [ -z $4 ]
then
	WRITES=30
else
	WRITES=$4
fi

if [ -z $5 ]
then
	NTRANS=500
else
	NTRANS=$5
fi


if [ -z $6 ]
then
	NOBJTRANS=2
else
	NOBJTRANS=$6
fi
#=$(expr $5 - 1)
#fi

if [ -z $7 ]
then
	CM=2 # KARMA
else
	CM=$7
fi

if [ -z $8 ]
then
	NHTENTRIES=10
else
	NHTENTRIES=$8
fi

if [ -z $9 ]
then
	MAXABORTS_MINDELAY_DELAY=64
else
	MAXABORTS_MINDELAY_DELAY=$9
fi

if [ -z $10 ]
then
	MAXDELAY_INTERVALS=256
else
	MAXDELAY_INTERVALS=$10
fi

for i in $(seq 0 0);
do
  java DHT.DHTCoordinator $NSERVER $NCLIENT $NOBJSERVER&

  pid=$!
  printf "CM ID:\t$CM\t"
  for i in $(seq 0 $(($NSERVER - 1)));
  do
	taskset -c $(($i+$NCLIENT)) java DHT.DHTServer $i $NHTENTRIES $CM $MAXABORTS_MINDELAY_DELAY $MAXDELAY_INTERVALS &
  done

  for i in $(seq 0 $(($NCLIENT-1)));
  do
	#-Djava.rmi.server.logCalls=true
	taskset -c $i java DHT.DHTClient $i $NSERVER $NOBJSERVER $WRITES $NTRANS $NOBJTRANS $NHTENTRIES &
 done
 wait $pid
done


