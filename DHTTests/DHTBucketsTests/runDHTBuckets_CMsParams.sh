#!/bin/bash

if [ -z $1 ]
then
	NSERVER=8
else
	NSERVER=$1
fi

if [ -z $2 ]
then
	NKEYS=10000
else NKEYS=$2
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
	NHTENTRIES=1567
else
	NHTENTRIES=$8
fi

if [ -z $9 ]
then
	MAXABORTS_MINDELAY_DELAY=64
else
	MAXABORTS_MINDELAY_DELAY=$9
fi

if [ -z ${10} ]
then
	MAXDELAY_INTERVALS=256
else
	MAXDELAY_INTERVALS=${10}
fi

#echo $(basename $(pwd))

# Moves to previous directory to run and use the DHT .class files
cd ../../

#echo $(basename $(pwd))

for i in $(seq 0 0);
do
  	java DHT.DHTBuckets.DHTCoordinator $NSERVER $NCLIENT $NKEYS&

  	pid=$!
  	printf "CM ID:\t$CM\t"
  	for i in $(seq 0 $(($NSERVER - 1)));
  	do
		taskset -c $(($i+$NCLIENT)) java DHT.DHTBuckets.DHTServer $i $NHTENTRIES &
  	done

  	for i in $(seq 0 $(($NCLIENT-1)));
  	do
		#-Djava.rmi.server.logCalls=true
		taskset -c $i java DHT.DHTBuckets.DHTClient $i $NSERVER $NKEYS $WRITES $NTRANS $NOBJTRANS $NHTENTRIES $CM $MAXABORTS_MINDELAY_DELAY $MAXDELAY_INTERVALS &
 	done
 	wait $pid
done



