#!/bin/bash

if [ -z $1 ]
then
	NSERVER=1
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
	CM=7
else
	CM=$7
fi

for i in $(seq 0 0);
do
  java DSTMBenchmark.GenericDSTM.GenericCoordinator $NSERVER $NCLIENT $NOBJSERVER&

  pid=$!
  for i in $(seq 0 $(($NSERVER - 1)));
  do
	taskset -c $(($i+$NCLIENT)) java DSTMBenchmark.GenericDSTM.GenericServer $i $NOBJSERVER &
  done

  printf "CM ID:\t$CM\t"
  for i in $(seq 0 $(($NCLIENT-1)));
  do
	taskset -c $i java DSTMBenchmark.GenericDSTM.GenericClient $i $NSERVER $NOBJSERVER $WRITES $NTRANS $NOBJTRANS $CM &
 done
 wait $pid
done



