rm TRMIPolka/TinyTM/*.class
rm TRMIPolka/TinyTM/contention/*.class
#rm TRMIPolka/TinyTM/DBank/*.class
rm TRMIPolka/TinyTM/exceptions/*.class
rm TRMIPolka/TinyTM/ofree/*.class
javac -Xlint:unchecked -classpath .:./TRMIPolka TRMIPolka/TinyTM/*.java
javac -classpath .:./TRMIPolka TRMIPolka/TinyTM/contention/*.java
#javac -classpath .:./TRMIPolka TRMIPolka/TinyTM/DBank/*.java
javac -classpath .:./TRMIPolka TRMIPolka/TinyTM/exceptions/*.java
javac -classpath .:./TRMIPolka TRMIPolka/TinyTM/ofree/*.java
rm DSTMBenchmark/*.class
rm DSTMBenchmark/GenericDSTM/*.class
javac DSTMBenchmark/*.java
javac -Xlint:unchecked -classpath .:./TRMIPolka DSTMBenchmark/GenericDSTM/*.java
