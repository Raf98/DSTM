#16 SERVERS
#UP TO 16 CLIENTS (2, 4, 8, 16)
#5000 TRANSACTIONS DIVIDED BETWEEN CLIENTS AT LAUNCH TIME
#LOWER CONTENTION – KEYSPACE OF 10000 FOR ALL SERVERS, PRIME NUMBER FOR THE NUMBER OF HASHTABLE ENTRIES
#HIGHER CONTENTION – KEYSPACE OF 1000 FOR ALL SERVERS, POWER OF 2 NUMBER FOR THE NUMBER OF HASHTABLE ENTRIES
#WRITES PER TRANSACTION – 20% OR 50%
#SIZE OF TRANSACTIONS – SHORT (5 OBJECTS) OR LONG (20 OBJECTS)

# NSERVER SHOULD BE 16 FOR OVERALL TESTS
if [ -z $1 ]
then
	NSERVER=16
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

# NMAXCLIENTS SHOULD BE 16 FOR OVERALL TESTS
if [ -z $3 ]
then
	NMAXCLIENTS=16
else
	NMAXCLIENTS=$3
fi

# WRITES - should loop first through 20 then through 50
#WRITES=20
for WRITES in $(seq 20 30 50); 
do
    #SHORT CASE?? 5 OBJECTS - should loop first through 5 OBJS (SHORT) then through 10 OBJS (LONG)
    NOBJTRANS=5
    for NOBJTRANS in $(seq 5 15 20);
    do
        # CONTENTION- should loop first using a key space limited to 1000 keys, and a number of entries for a hash table
        # that is a power of 2, for more collision, thus, greater contention
        # and then move to a case in which there are 10000 keys available and a lower number of entries but of a prime number
        # which would render less collisions and, thus, lower contention
        NKEYS=1000
        NHTENTRIES=128
        for NKEYS in $(seq 1000 9000 10000);
        do
            echo "NKEYS: $NKEYS NHTENTRIES: $NHTENTRIES WRITES: $WRITES NOBJTRANS: $NOBJTRANS"
            NCLIENT=2
            while [[ $NCLIENT -le $NMAXCLIENTS ]];
            do
                NTRANS=$(($NTTRANS/$NCLIENT))
                echo "clients: $NCLIENT, transactions per client: $NTRANS, NTTRANS: $NTTRANS"
                    for i in $(seq 0 9);
                    do
                        echo "Test $i for TRMILocks"
                        printf "TRMILocks\t$NCLIENT\t"
                        ./runDHTLocks.sh $NSERVER $NKEYS $NCLIENT $WRITES $NTRANS $NOBJTRANS $NHTENTRIES
                    done
                let "NCLIENT*=2"
            done
            let "NHENTRIES=97"
        done
    done
done
