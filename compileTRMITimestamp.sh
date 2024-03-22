rm TRMITimestamp/TinyTM/*.class
rm TRMITimestamp/TinyTM/contention/*.class
#rm TRMITimestamp/TinyTM/DBank/*.class
rm TRMITimestamp/TinyTM/exceptions/*.class
rm TRMITimestamp/TinyTM/ofree/*.class
javac -Xlint:unchecked -classpath .:./TRMITimestamp TRMITimestamp/TinyTM/*.java
javac -classpath .:./TRMITimestamp TRMITimestamp/TinyTM/contention/*.java
#javac -classpath .:./TRMITimestamp TRMITimestamp/TinyTM/DBank/*.java
javac -classpath .:./TRMITimestamp TRMITimestamp/TinyTM/exceptions/*.java
javac -classpath .:./TRMITimestamp TRMITimestamp/TinyTM/ofree/*.java
rm DSTMBenchmark/*.class
rm DSTMBenchmark/GenericDSTM/*.class
javac DSTMBenchmark/*.java
javac -Xlint:unchecked -classpath .:./TRMITimestamp DSTMBenchmark/GenericDSTM/*.java
