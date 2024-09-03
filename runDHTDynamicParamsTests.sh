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
	NTTRANS=1000
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

if [ -z $4 ]
then
	NHTENTRIES=10
else
	NHTENTRIES=$4
fi

echo "Compiling all files needed for DHT..."
./compileDHT.sh
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

            delay=32
            while [[ $delay -le 128 ]];
            do
                intervals=4
                while [[ $intervals -le 16 ]];
                do
                    for i in $(seq 0 4);
                    do
                        echo "Test $i for TRMIKindergarten: $delay delay; $intervals intervals"
                        printf "TRMIKindergarten\t$NCLIENT\t"
                        ./runDHT_CMsParams.sh $NSERVER $NOBJSERVER $NCLIENT $WRITES $NTRANS $NOBJTRANS 5 $NHTENTRIES $delay $intervals
                    done
                    let "intervals*=2" #4 8 16
                done
                let "delay*=2" #32 64 128
            done
         let "NCLIENT*=2"
        done

        echo "NOBJSERVER: $NOBJSERVER WRITES: $WRITES NOBJTRANS: $NOBJTRANS"
        NCLIENT=2
        while [[ $NCLIENT -le $NMAXCLIENTS ]];
        do
            NTRANS=$(($NTTRANS/$NCLIENT))
            echo "clients: $NCLIENT, transactions per client: $NTRANS, NTTRANS: $NTTRANS"
            delay=32
            while [[ $delay -le 128 ]];
            do
                intervals=16
                while [[ $intervals -le 64 ]];
                do
                    for i in $(seq 0 4);
                    do
                        echo "Test $i for TRMITimestamp: $delay delay; $intervals intervals"
                        printf "TRMITimestamp\t$NCLIENT\t"
                        ./runDHT_CMsParams.sh $NSERVER $NOBJSERVER $NCLIENT $WRITES $NTRANS $NOBJTRANS 4 $NHTENTRIES $delay $intervals
                    done
                    let "intervals*=2" #16 32 64
                done
                let "delay*=2" #32 64 128
            done
            let "NCLIENT*=2"
        done

        echo "NOBJSERVER: $NOBJSERVER WRITES: $WRITES NOBJTRANS: $NOBJTRANS"
        NCLIENT=2
        while [[ $NCLIENT -le $NMAXCLIENTS ]];
        do
            NTRANS=$(($NTTRANS/$NCLIENT))
            echo "clients: $NCLIENT, transactions per client: $NTRANS, NTTRANS: $NTTRANS"

            min_delay=64
            while [[ $min_delay -le 256 ]];
            do
                for i in $(seq 0 4);
                do
                    echo "Test $i for TRMIPolka: $min_delay MIN_DELAY"
                    printf "TRMIPolka\t$NCLIENT\t"
                    ./runDHT_CMsParams.sh $NSERVER $NOBJSERVER $NCLIENT $WRITES $NTRANS $NOBJTRANS 3 $NHTENTRIES $min_delay
                done
                let "min_delay*=2" #64 128 256
            done
            let "NCLIENT*=2"
        done

        echo "NOBJSERVER: $NOBJSERVER, WRITES: $WRITES NOBJTRANS: $NOBJTRANS"
        NCLIENT=2
        while [[ $NCLIENT -le $NMAXCLIENTS ]];
        do
            NTRANS=$(($NTTRANS/$NCLIENT))
            echo "clients: $NCLIENT, transactions per client: $NTRANS, NTTRANS: $NTTRANS"

            delay=32
            while [[ $delay -le 128 ]];
            do
                for i in $(seq 0 4);
                do
                    echo "Test $i for TRMIKarma: $delay delay"
                    printf "TRMIKarma\t$NCLIENT\t"
                    ./runDHT_CMsParams.sh $NSERVER $NOBJSERVER $NCLIENT $WRITES $NTRANS $NOBJTRANS 2 $NHTENTRIES $delay
                done
                let "delay*=2" #32 64 128
            done
            let "NCLIENT*=2"
        done

        echo "NOBJSERVER: $NOBJSERVER, WRITES: $WRITES NOBJTRANS: $NOBJTRANS"
        NCLIENT=2
        while [[ $NCLIENT -le $NMAXCLIENTS ]];
        do
            NTRANS=$(($NTTRANS/$NCLIENT))
            echo "clients: $NCLIENT, transactions per client: $NTRANS, NTTRANS: $NTTRANS"

            min_delay=64
               while [[ $min_delay -le 256 ]];
               do
                   max_delay=2048
                   while [[ $max_delay -le 8192 ]];
                   do
                       for i in $(seq 0 4);
                       do
                           echo "Test $i for TRMIPolite: $min_delay min_delay; $max_delay max_delay"
                           printf "TRMIPolite\t$NCLIENT\t"
                           ./runDHT_CMsParams.sh $NSERVER $NOBJSERVER $NCLIENT $WRITES $NTRANS $NOBJTRANS 1 $NHTENTRIES $min_delay $max_delay
                       done
                       let "max_delay*=2" #2048 4096 8192
                   done
                   let "min_delay*=2" #64 128 256
               done
               let "NCLIENT*=2"
        done

        echo "NOBJSERVER: $NOBJSERVER, WRITES: $WRITES NOBJTRANS: $NOBJTRANS"
        NCLIENT=2
        while [[ $NCLIENT -le $NMAXCLIENTS ]];
        do
            NTRANS=$(($NTTRANS/$NCLIENT))
            echo "clients: $NCLIENT, transactions per client: $NTRANS, NTTRANS: $NTTRANS"

            max_aborts=8
            while [[ $max_aborts -le 32 ]];
            do
                for i in $(seq 0 4);
                do
                    echo "Test $i for TRMIPassive: $max_aborts max_aborts"
                    printf "TRMIPassive\t$NCLIENT\t"
                    ./runDHT_CMsParams.sh $NSERVER $NOBJSERVER $NCLIENT $WRITES $NTRANS $NOBJTRANS 0 $NHTENTRIES $max_aborts
                done
                let "max_aborts*=2" #8 16 32
            done
            let "NCLIENT*=2"
        done

        
     done
   done
done

