#!/bin/bash

#32 SERVERS
#UP TO 32 CLIENTS (2, 4, 8, 16, 32)
#5000 TRANSACTIONS DIVIDED BETWEEN CLIENTS AT LAUNCH TIME
#LOW CONTENTION – 500 OBJECTS
#HIGHER CONTENTION – 100 OBJECTS
#WRITES PER TRANSACTION – 20% OR 50%
#SIZE OF TRANSACTIONS – SHORT (5 OBJECTS) OR LONG (20 OBJECTS)

# NSERVER SHOULD BE 32
if [ -z $1 ]
then
	NSERVER=8
else
	NSERVER=$1
fi

# NTTRANS SHOULD BE 5000 INSTEAD???
if [ -z $2 ]
then
	NTTRANS=5000
else
	NTTRANS=$2
fi
#=$(expr $5 - 1)
#fi

# NMAXCLIENTS SHOULD BE 32 INSTEAD
if [ -z $3 ]
then
	NMAXCLIENTS=8
else
	NMAXCLIENTS=$3
fi

echo "Compiling all files needed for the generic benchmark..."
./compileSyntheticBench.sh

# WRITES - should loop first through 20 then through 50
#WRITES=20
for WRITES in $(seq 20 30 50); 
do
    #SHORT CASE?? 5 OBJECTS - should loop first through 5 OBJS (SHORT) then through 10 OBJS (LONG)
    NOBJTRANS=5
    for NOBJTRANS in $(seq 5 15 20);
    do
        # LOW CONTENTION??? - should loop first through 100 then through 500
        NOBJSERVER=100
        for NOBJSERVER in $(seq 100 400 500);
        do

            echo "NOBJSERVER: $NOBJSERVER WRITES: $WRITES NOBJTRANS: $NOBJTRANS"
            NCLIENT=2
            while [[ $NCLIENT -le $NMAXCLIENTS ]];
            do
                NTRANS=$(($NTTRANS/$NCLIENT))
                echo "clients: $NCLIENT, transactions per client: $NTRANS, NTTRANS: $NTTRANS"

                for i in $(seq 0 9);
                do
                    echo "Test $i for TRMIAgressive"
                    printf "TRMIAgressive\t$NCLIENT\t"
                    ./runSyntheticBench_CMsParams.sh $NSERVER $NOBJSERVER $NCLIENT $WRITES $NTRANS $NOBJTRANS 7
                done
                
                let "NCLIENT*=2"
            done

        done
    done
done

