rm TRMIKarma/TinyTM/*.class
rm TRMIKarma/TinyTM/contention/*.class
rm TRMIKarma/TinyTM/DBank/*.class
rm TRMIKarma/TinyTM/exceptions/*.class
rm TRMIKarma/TinyTM/ofree/*.class
javac -classpath .:./TRMIKarma TRMIKarma/TinyTM/*.java
javac -classpath .:./TRMIKarma TRMIKarma/TinyTM/contention/*.java
javac -classpath .:./TRMIKarma TRMIKarma/TinyTM/DBank/*.java
javac -classpath .:./TRMIKarma TRMIKarma/TinyTM/exceptions/*.java
javac -classpath .:./TRMIKarma TRMIKarma/TinyTM/ofree/*.java
rm DSTMBenchmark/*.class
rm DSTMBenchmark/GenericDSTM/*.class
javac DSTMBenchmark/*.java
javac -classpath .:./TRMIKarma DSTMBenchmark/GenericDSTM/*.java
