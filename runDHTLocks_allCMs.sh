#!/bin/bash

if [ -z $1 ]
then
	NSERVER=8
else
	NSERVER=$1
fi

if [ -z $2 ]
then
	NKEYS=10
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
	NOBJTRANS=20
else
	NOBJTRANS=$6
fi
#=$(expr $5 - 1)
#fi

if [ -z $7 ]
then
	NHTENTRIES=100
else
	NHTENTRIES=$7
fi


echo "Compiling all files needed..."
./compileDHT.sh


for CMSEL in $(seq 0 1 7); 
do
    for i in $(seq 0 0);
    do
      java DHT.DHTLocks.DHTCoordinator $NSERVER $NCLIENT $NKEYS&

      pid=$!
      printf "CM ID:\t$CMSEL\t"
      for i in $(seq 0 $(($NSERVER - 1)));
      do
    	taskset -c $(($i+$NCLIENT)) java DHT.DHTLocks.DHTServer $i $NHTENTRIES &
      done

      for i in $(seq 0 $(($NCLIENT-1)));
      do
    	#-Djava.rmi.server.logCalls=true
    	taskset -c $i java DHT.DHTLocks.DHTClient $i $NSERVER $NKEYS $WRITES $NTRANS $NOBJTRANS $NHTENTRIES &
     done
     wait $pid
    done
done






