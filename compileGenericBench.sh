rm TinyTM/*.class
rm TinyTM/contention/*.class
#rm TinyTM/DBank/*.class
rm TinyTM/exceptions/*.class
rm TinyTM/ofree/*.class
javac TinyTM/*.java
javac TinyTM/contention/*.java
#javac -Xlint:unchecked -classpath .:. TinyTM/DBank/*.java
javac TinyTM/exceptions/*.java
javac TinyTM/ofree/*.java
rm DSTMBenchmark/*.class
rm DSTMBenchmark/GenericDSTM/*.class
javac DSTMBenchmark/*.java
javac DSTMBenchmark/GenericDSTM/*.java