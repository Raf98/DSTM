rm TRMIKindergarten/TinyTM/*.class
rm TRMIKindergarten/TinyTM/contention/*.class
rm TRMIKindergarten/TinyTM/DBank/*.class
rm TRMIKindergarten/TinyTM/exceptions/*.class
rm TRMIKindergarten/TinyTM/ofree/*.class
javac -classpath .:./TRMIKindergarten TRMIKindergarten/TinyTM/*.java
javac -classpath .:./TRMIKindergarten TRMIKindergarten/TinyTM/contention/*.java
javac -classpath .:./TRMIKindergarten TRMIKindergarten/TinyTM/DBank/*.java
javac -classpath .:./TRMIKindergarten TRMIKindergarten/TinyTM/exceptions/*.java
javac -classpath .:./TRMIKindergarten TRMIKindergarten/TinyTM/ofree/*.java
rm DSTMBenchmark/*.class
rm DSTMBenchmark/GenericDSTM/*.class
javac DSTMBenchmark/*.java
javac -classpath .:./TRMIKindergarten DSTMBenchmark/GenericDSTM/*.java
