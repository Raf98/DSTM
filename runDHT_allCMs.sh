#!/bin/bash

if [ -z $1 ]
then
	NSERVER=8
else
	NSERVER=$1
fi

if [ -z $2 ]
then
	NKEYS=1000
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
	NOBJTRANS=10
else
	NOBJTRANS=$6
fi
#=$(expr $5 - 1)
#fi

if [ -z $7 ]
then
	CM=0 # PASSIVE
else
	CM=$7
fi

if [ -z $8 ]
then
	NHTENTRIES=128
else
	NHTENTRIES=$8
fi


echo "Compiling all files needed..."
./compileDHT.sh


for CMSEL in $(seq 0 1 7); 
do
    for i in $(seq 0 0);
    do
      	java DHT.DHTCoordinator $NSERVER $NCLIENT $NKEYS&

      	pid=$!
      	printf "CM ID:\t$CMSEL\t"
      	for i in $(seq 0 $(($NSERVER - 1)));
      	do
    		taskset -c $(($i+$NCLIENT)) java DHT.DHTServer $i $NHTENTRIES $CMSEL &
      	done

      	for i in $(seq 0 $(($NCLIENT-1)));
      	do
    		#-Djava.rmi.server.logCalls=true
    		taskset -c $i java DHT.DHTClient $i $NSERVER $NKEYS $WRITES $NTRANS $NOBJTRANS $NHTENTRIES &
     	done
     	wait $pid
    done
done






