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
	CM=0 # PASSIVE
else
	CM=$7
fi

if [ -z $8 ]
then
	NHTENTRIES=10
else
	NHTENTRIES=$8
fi


echo "Compiling all files needed..."
./compileDHT.sh


for CMSEL in $(seq 0 1 7); 
do
    for i in $(seq 0 0);
    do
      java DHT.DHTLocks.DHTCoordinator $NSERVER $NCLIENT $NOBJSERVER&

      pid=$!
      printf "CM ID:\t$CMSEL\t"
      for i in $(seq 0 $(($NSERVER - 1)));
      do
    	taskset -c $(($i+$NCLIENT)) java DHT.DHTLocks.DHTServer $i $NHTENTRIES $CMSEL &
      done

      for i in $(seq 0 $(($NCLIENT-1)));
      do
    	#-Djava.rmi.server.logCalls=true
    	taskset -c $i java DHT.DHTLocks.DHTClient $i $NSERVER $NOBJSERVER $WRITES $NTRANS $NOBJTRANS $CMSEL $NHTENTRIES &
     done
     wait $pid
    done
done





