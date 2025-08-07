# Transactional RMI (TRMI): Distributed Software Transactional Memory (DSTM/DTM) based on Obstruction-Free algorithms

Transactional RMI, or TRMI, is an extension to RMI that supports the synchronization of objects distributed in a network through the usage of transactions, that is, it is a model of a control-flow and obstruction-free Distributed Transactional Memory (DTM).

This version supports multiple Contention Managers through the dynamic loading of the CM and its parameters. This is commonly done through command line arguments, passed to the execution of the applications developed with TRMI. The supported CMs include classic ones such as Passive, Polka and Timestamp.

To run the synthetic benchmark using TRMI's DTM under different contention managers, do the following:

- Open a terminal in the main folder, and enter ```cd GenericBenchFinalTests```
- Then, chose of the 8 shell scripts, where each of these run the synthetic bench under different scenarios in a single contention manager
- To run one of these shell files, enter ```./runGenericBench{CM}FinalTests.sh```, where ```{CM}``` is the name of the Contention Manager with which the tests will be run. 
- For instance, if you want to run the synth bench using the **Karma** CM, you would enter ```./runGenericBenchKarmaFinalTests.sh```.


Check this script file to see which scenarios are run.


To run the tests on the DHT application, the steps are:
- Open a terminal in the main folder, and enter ```cd DHTTests```
- Then, chose of the 8 shell scripts, where each of these run the synthetic bench under different scenarios in a single contention manager
- To run one of these shell files, enter ```./runDHT{CM}.sh```, where ```{CM}``` is the name of the Contention Manager with which the tests will be run. 
- For instance, if you want to run the synth bench using the **Polite** CM, you would enter ```./runDHTPolite.sh```.