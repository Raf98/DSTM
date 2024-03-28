rm TinyTM/*.class
rm TinyTM/contention/*.class
rm TinyTM/DBank/*.class
rm TinyTM/exceptions/*.class
rm TinyTM/ofree/*.class
javac TinyTM/*.java
javac TinyTM/contention/*.java
javac TinyTM/DBank/*.java
javac TinyTM/exceptions/*.java
javac TinyTM/ofree/*.java
rm DHT/*.class
rm DSTMBenchmark/*.class
javac DSTMBenchmark/*.java
javac -classpath .:./TRMIKarma DHT/*.java
