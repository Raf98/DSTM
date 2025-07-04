# Moves to parent directories to erase .class files and re-compile .java files
cd ../..

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

# Moves back to PaperTests/SyntheticBench
cd PaperTests/SyntheticBench