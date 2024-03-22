rm TRMILess/TinyTM/*.class
rm TRMILess/TinyTM/contention/*.class
#rm TRMILess/TinyTM/DBank/*.class
rm TRMILess/TinyTM/exceptions/*.class
rm TRMILess/TinyTM/ofree/*.class
javac -classpath .:./TRMILess TRMILess/TinyTM/*.java
javac -classpath .:./TRMILess TRMILess/TinyTM/contention/*.java
#javac -Xlint:unchecked -classpath .:./TRMILess TRMILess/TinyTM/DBank/*.java
javac -classpath .:./TRMILess TRMILess/TinyTM/exceptions/*.java
javac -classpath .:./TRMILess TRMILess/TinyTM/ofree/*.java
rm DSTMBenchmark/*.class
rm DSTMBenchmark/GenericDSTM/*.class
javac DSTMBenchmark/*.java
javac -classpath .:./TRMILess DSTMBenchmark/GenericDSTM/*.java