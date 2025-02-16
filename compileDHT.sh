rm TinyTM/*.class
rm TinyTM/contention/*.class
#rm TinyTM/DBank/*.class
rm TinyTM/exceptions/*.class
rm TinyTM/ofree/*.class
javac TinyTM/*.java
javac TinyTM/contention/*.java
#javac -Xlint:unchecked TinyTM/DBank/*.java
javac TinyTM/exceptions/*.java
javac TinyTM/ofree/*.java
rm DHT/*.class
rm DHT/DHTLocks/*.class
rm DHT/DHTBuckets/*.class
rm DSTMBenchmark/*.class
javac DSTMBenchmark/*.java
javac -Xlint:unchecked DHT/*.java
javac DHT/DHTLocks/*.java
javac DHT/DHTBuckets/*.java