#!/bin/bash

if [ -z $1 ]
then
	NSERVER=10
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
	NCLIENT=2
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
	NTRANS=100
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
	CM=0
else
	CM=$7
fi

if [ -z $8 ]
then
	NHTENTRIES=10
else
	NHTENTRIES=$8
fi

for i in $(seq 0 0);
do
  java -classpath .:./TRMIKarma DHT.DHTCoordinator $NSERVER $NCLIENT $NOBJSERVER&

  pid=$!
  printf "TRMIKarma\t$NCLIENT\t"
  for i in $(seq 0 $(($NSERVER - 1)));
  do
	taskset -c $(($i+$NCLIENT)) java DHT.DHTServer $i $NHTENTRIES &
  done

  for i in $(seq 0 $(($NCLIENT-1)));
  do
	#-Djava.rmi.server.logCalls=true
	taskset -c $i java DHT.DHTClient $i $NSERVER $NOBJSERVER $WRITES $NTRANS $NOBJTRANS $CM $NHTENTRIES &
 done
 wait $pid
done



