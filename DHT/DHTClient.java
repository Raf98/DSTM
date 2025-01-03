package DHT;

import java.io.FileWriter;
import java.rmi.Naming;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import DSTMBenchmark.AppCoordinator;
import DSTMBenchmark.IDBarrier;
import DSTMBenchmark.RObject;
import TinyTM.Transaction;

public class DHTClient {
    public static void main(String[] args) throws Exception {
        System.out.println("começa");

        int clientid = Integer.parseInt(args[0]);               // i in NCLIENT
        int servers = Integer.parseInt(args[1]);                // NSERVER
        int numberOfKeys = Integer.parseInt(args[2]);           // NKEYS
        int writes = Integer.parseInt(args[3]);                 // WRITES
        int transactions = Integer.parseInt(args[4]);           // NTRANS
        int objectsPerTransaction = Integer.parseInt(args[5]);  // NOBJTRANS
        int hashTablesEntries = Integer.parseInt(args[6]);      // NHTENTRIES

        DHTTransaction transaction = new DHTTransaction();

        var saveData = new DHTSaveData();
        //var cs = new MyChoiceOfObjects();

        IDBarrier barrier = AppCoordinator.connectToBarrier("barrier"); // (IDBarrier) Naming.lookup("barrier");
        barrier.await();

        RObject[] robjects;
        int op;
        
        OperationsShuffler opsShuffler = new OperationsShuffler();
        Integer[] shuffledOps = opsShuffler.shuffledArray(transactions, writes);

        for (int i = 0; i < transactions; i++) {
            //robjects = cs.chooseObjects(servers, objects, objectsPerTransaction, random);
            op = shuffledOps[i]; //cop.chooseOP(writes, random);
            //transaction.execTransaction(robjects, op);
            transaction.execTransaction(servers, numberOfKeys, objectsPerTransaction, hashTablesEntries, op);
        }

        // App Ends
        barrier.await();

        saveData.saveData(clientid, transaction);

        // waits for all cleints do save data
        barrier.await();
        // waits for all servers to process data
        barrier.await();
        // System.out.println("Acabei");

        // app returns when all clients were executed
        // now it is time to save data for sanity check

        // saveData(clientid,transaction);

        System.exit(0);
    }
}

class DHTSaveData implements NewSaveData {

    public void saveData(int clientid, ExecuteTransaction trans) throws Exception {
        DHTTransaction transaction = (DHTTransaction) trans;

        List<List<String>> rows = Arrays.asList(
                Arrays.asList("commits", DHTTransaction.commits.get() + ""),
                Arrays.asList("inserts", DHTTransaction.inserts.get() + ""),
                Arrays.asList("gets", DHTTransaction.gets.get() + ""),
                Arrays.asList("commitsrts", Transaction.commits.get() + ""),
                Arrays.asList("aborts", Transaction.aborts.get() + ""));

        System.out.println("gravando arquivo");

        FileWriter csvWriter = new FileWriter("client" + clientid + ".out");

        for (List<String> rowData : rows) {
            csvWriter.append(String.join(",", rowData));
            csvWriter.append("\n");
        }

        csvWriter.flush();
        csvWriter.close();

    }

}

class DHTTransaction implements ExecuteTransaction {

    static AtomicInteger commits;

    static AtomicInteger inserts;
    static AtomicInteger gets;

    DHTTransaction() {
        commits = new AtomicInteger(0);

        inserts = new AtomicInteger(0);
        gets = new AtomicInteger(0);
    }


   /* public void execTransaction(RObject[] robjects, int op) throws Exception {


        TMObj<IHTMachine>[] TMObjects = new TMObj[robjects.length];
        for (int i = 0; i < TMObjects.length; i++) {
            TMObjects[i] = (TMObj<IHTMachine>) TMObj.lookupTMObj("rmi://localhost:" + robjects[i].getPort() + "/" + robjects[i].getAddress());
        }

        int donewithdraw = 0;

        donewithdraw = (int) Transaction.atomic(new Callable<Integer>() {
            public Integer call() throws Exception {
                int localwithdraw = 0;
                Random rng = new Random();
                if (op == 0) {
                    IHTMachine iht = TMObjects[0].openWrite();
                    iht.insert(rng.nextInt(10*100), rng.nextInt(Integer.MAX_VALUE));
                }

                if (op == 2) {
                    IHTMachine iht = TMObjects[0].openRead();
                    iht.get(rng.nextInt(10*100));
                }
                return localwithdraw;
            }
        });

        // SANITY CHECK:
        commits.getAndIncrement();

        if (op == 0) {
            inserts.getAndIncrement();
        }

        if (op == 1) {
            gets.getAndIncrement();
        }
    }*/ 

    @SuppressWarnings("unchecked")
    @Override
    public void execTransaction(int nServers, int numberOfKeys, int nObjectsPerTransaction, int hashTablesEntries, int op) throws Exception {
        Random rng = new Random();
        /*IHashTable[] machinesForOps = new IHashTable[nServers];

        // Lookup for Hash Tables within their machines/ servers in the network
        for (int i = 0; i < machinesForOps.length; i++) {
            String port = String.valueOf(1700 + i);
            String nodeName = "ht" + i;

            //System.out.println("NODE NAME: " + nodeName);
            //System.out.println("rmi://localhost:" + port + "/" + nodeName);

            machinesForOps[i] = (IHashTable) Naming.lookup("rmi://localhost:" + port + "/" + nodeName);
        }

        // Iterate and calculate the machineId needed for each operation within the Distributed Hash Table
        int[] machinesIds = new int[nObjects];*/
        int[] keys = new int[nObjectsPerTransaction];//[TMObjects.length];
        int[] values = new int[nObjectsPerTransaction];

        int serverNum = rng.nextInt(nServers);
        String port = String.valueOf(1700 + serverNum);
        String nodeName = "ht" + serverNum;
        IHashTable serverForOps = (IHashTable) Naming.lookup("rmi://localhost:" + port + "/" + nodeName);


        /*for (int i = 0; i < nObjects; i++) {
            int bound = 1000;
            keys[i] = rng.nextInt(bound);
            values[i] = rng.nextInt(Integer.MAX_VALUE);

            
            int inc = bound / nServers;
            int count = 0;
            int j = 0;
            for (; j < nServers; j++) {
                count += inc;
                if(keys[i] < count) {
                    break;
                }
            }

            int machineNum = j; // esquema de descobrir máquina deve ser repensado?
            machinesIds[i] = machineNum;
        }*/

        for (int i = 0; i < nObjectsPerTransaction; i++) {
            int bound = numberOfKeys;
            //min + rng.nextInt(max - min);
            // Limits the key generation within the bounds of the minimum and the maximum values for the current server
            keys[i] = (bound / nServers) * serverNum + rng.nextInt((bound / nServers) * (serverNum + 1) - (bound / nServers) * serverNum);
        }

        if (op == 0) {
            /*for (int i = 0; i < nObjects; i++) {
                IHashTable iHashTable = machinesForOps[machinesIds[i]];
                //System.out.println("TRANSACTION CLIENT ID " + clientId);
                //System.out.println("WRITING..." + i + ", KEY: " + keys[i] + ", " + "MACHINE: " + machinesIds[i]);
                iHashTable.insert(keys[i], values[i]);
                inserts.getAndIncrement();
                commits.getAndIncrement();
                //System.out.println("INSERTS: " + inserts.get());
            }*/
            for (int i = 0; i < nObjectsPerTransaction; i++) {
                values[i] = rng.nextInt(Integer.MAX_VALUE);
            }
            serverForOps.insertMultiple(keys, values);
            inserts.getAndIncrement();
            commits.getAndIncrement();
            //System.out.println("INSERTS: " + inserts.get());
        } else {
            /*for (int i = 0; i < nObjects; i++) {
                IHashTable iHashTable = machinesForOps[machinesIds[i]];
                //System.out.println("TRANSACTION CLIENT ID " + clientId);
                //System.out.println("READING..." + i + ", KEY: " + keys[i] + ", " + "MACHINE: " + machinesIds[i]);
                iHashTable.get(keys[i]);
                gets.getAndIncrement();
                commits.getAndIncrement();
                //System.out.println("GETS: " + gets.get());
            }*/
            serverForOps.getMultiple(keys);
            gets.getAndIncrement();
            commits.getAndIncrement();
            //System.out.println("GETS: " + gets.get());
        }

        /*int donewithdraw = 0;

        donewithdraw = (int) Transaction.atomic(0, new Callable<Integer>() {
            public Integer call() throws Exception {
                int localwithdraw = 0;
                Random rng = new Random();
                if (op == 0) {

                    for (int i = 0; i < TMObjects.length; i++) {
                        INode<Integer> iNode = TMObjects[i].openWrite();
                        //System.out.println("TRANSACTION CLIENT ID " + clientId);
                        System.out.println("WRITING..." + i + ", KEY: " + keys[i] + ", " + "MACHINE: " + machinesIds[i]);
                        iNode.insert(machinesIds[i], keys[i], rng.nextInt(Integer.MAX_VALUE));
                        inserts.getAndIncrement();
                        System.out.println("INSERTS: " + inserts.get());
                    }

                } else if (op == 1) {
                    
                    for (int i = 0; i < TMObjects.length; i++) {
                        INode<Integer> iNode = TMObjects[i].openRead();
                        System.out.println("READING...");
                        iNode.get(keys[i]);
                        gets.getAndIncrement();
                    }

                }
                return localwithdraw;
            }
        });

        // SANITY CHECK:
        commits.getAndIncrement();

        /*if (op == 0) {
            inserts.getAndIncrement();
        }

        if (op == 1) {
            gets.getAndIncrement();
        }*/
    }
}