#./runDHTKindergarten.sh 2>&1 | tee dht_kindergarten.txt
./runDHTKarma.sh 2>&1 | tee dht_karma.txt
./runDHTPolka.sh 2>&1 | tee dht_polka.txt
./runDHTAgressive.sh 2>&1 | tee dht_agressive.txt
./runDHTPolite.sh 2>&1 | tee dht_polite.txt
./runDHTPassive.sh 2>&1 | tee dht_passive.txt
./runDHTLess.sh 2>&1 | tee dht_less.txt
./runDHTLocksTests.sh 2>&1 | tee dht_locks.txt
