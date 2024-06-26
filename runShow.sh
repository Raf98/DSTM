#!/bin/bash

if [ -z $1 ]
then
	NSERVER=3
else
	NSERVER=$1
fi

if [ -z $2 ]
then
	NTTRANS=500
else
	NTTRANS=$2
fi
#=$(expr $5 - 1)
#fi

for WRITES in 20; 
do
   for NOBJTRANS in 5;
   do
     for NOBJSERVER in 500;
     do
        echo "NOBJSERVER: $NOBJSERVER WRITES: $WRITES NOBJTRANS: $NOBJTRANS"
        ./compileTRMITimestamp.sh
        for NCLIENT in 4;
        do
        NTRANS=$(($NTTRANS/$NCLIENT))
        #echo "clients: $NCLIENT transacoes por client: $NTRANS, NTTRANS: $NTTRANS"
        ./runTRMITimestamp.sh $NSERVER $NOBJSERVER $NCLIENT $WRITES $NTRANS $NOBJTRANS
        done

        echo "NOBJSERVER: $NOBJSERVER WRITES: $WRITES NOBJTRANS: $NOBJTRANS"
        ./compileTRMIKindergarten.sh
        for NCLIENT in 4;
        do
        NTRANS=$(($NTTRANS/$NCLIENT))
        #echo "clients: $NCLIENT transacoes por client: $NTRANS, NTTRANS: $NTTRANS"
        ./runTRMIKindergarten.sh $NSERVER $NOBJSERVER $NCLIENT $WRITES $NTRANS $NOBJTRANS
        done

        echo "NOBJSERVER: $NOBJSERVER WRITES: $WRITES NOBJTRANS: $NOBJTRANS"
        ./compileTRMIPolka.sh
        for NCLIENT in 4;
        do
        NTRANS=$(($NTTRANS/$NCLIENT))
        #echo "clients: $NCLIENT transacoes por client: $NTRANS, NTTRANS: $NTTRANS"
        ./runTRMIPolka.sh $NSERVER $NOBJSERVER $NCLIENT $WRITES $NTRANS $NOBJTRANS
        done

        ./compileTRMIKarma.sh
        for NCLIENT in 4;
        do
        NTRANS=$(($NTTRANS/$NCLIENT))
        #echo "clients: $NCLIENT transacoes por client: $NTRANS, NTTRANS: $NTTRANS"
        ./runTRMIKarma.sh $NSERVER $NOBJSERVER $NCLIENT $WRITES $NTRANS $NOBJTRANS
        done 

        ./compileTRMIPolite.sh
        for NCLIENT in 4;
        do
        NTRANS=$(($NTTRANS/$NCLIENT))
        #echo "clients: $NCLIENT transacoes por client: $NTRANS, NTTRANS: $NTTRANS"
        ./runTRMIPolite.sh $NSERVER $NOBJSERVER $NCLIENT $WRITES $NTRANS $NOBJTRANS
        done 

        ./compileTRMIPassive.sh
        for NCLIENT in 4;
        do
        NTRANS=$(($NTTRANS/$NCLIENT))
        #echo "clients: $NCLIENT transacoes por client: $NTRANS, NTTRANS: $NTTRANS"
        ./runTRMIPassive.sh $NSERVER $NOBJSERVER $NCLIENT $WRITES $NTRANS $NOBJTRANS 
        done

        
        ./compileLocks.sh
        for NCLIENT in 4;
        do
        NTRANS=$(($NTTRANS/$NCLIENT))
        #echo "clients: $NCLIENT transacoes por client: $NTRANS, NTTRANS: $NTTRANS"
        ./runLocks.sh $NSERVER $NOBJSERVER $NCLIENT $WRITES $NTRANS $NOBJTRANS 
        done 

     done
   done
done


