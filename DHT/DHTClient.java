package DHT;

import java.io.FileWriter;
import java.rmi.Naming;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import DSTMBenchmark.AppCoordinator;
import DSTMBenchmark.IDBarrier;
import DSTMBenchmark.RObject;
import TinyTM.Transaction;
import TinyTM.contention.CMEnum;
import TinyTM.ofree.TMObj;

public class DHTClient {
    public static void main(String[] args) throws Exception {
        // System.out.println("começa");

        int clientid = Integer.parseInt(args[0]); // i in NCLIENT
        int servers = Integer.parseInt(args[1]); // NSERVER
        int numberOfKeys = Integer.parseInt(args[2]); // NKEYS
        int writes = Integer.parseInt(args[3]); // WRITES
        int transactions = Integer.parseInt(args[4]); // NTRANS
        int objectsPerTransaction = Integer.parseInt(args[5]); // NOBJTRANS
        int hashTablesEntries = Integer.parseInt(args[6]); // NHTENTRIES
        int contentionManager = args.length > 7 ? Integer.parseInt(args[7]) : 0; // CM

        int maxAborts_minDelay_delay = 64;
        int maxDelay_intervals = 256;

        // If CM is not Less or Agressive, that have no settable parameters
        if (args.length > 8 && contentionManager < 6) {
            maxAborts_minDelay_delay = Integer.parseInt(args[7]);

            if (args.length > 8) {
                maxDelay_intervals = Integer.parseInt(args[8]);
            }
        }

        System.out.println("Contention Manager Dynamic Parameters:");
        System.out.println("maxAborts/minDelay/delay: " + maxAborts_minDelay_delay);
        System.out.println("maxDelay_intervals: " + maxDelay_intervals);
        System.out.println("CONTENTION MANAGER: " + CMEnum.fromId(contentionManager));
        Transaction.setContentionManager(contentionManager, maxAborts_minDelay_delay, maxDelay_intervals);

        DHTTransaction transaction = new DHTTransaction();

        var saveData = new DHTSaveData();
        // var cs = new MyChoiceOfObjects();

        IDBarrier barrier = AppCoordinator.connectToBarrier("barrier"); // (IDBarrier) Naming.lookup("barrier");
        barrier.await();

        RObject[] robjects;
        int op;

        // the shuffling should be done for each table, before operating over them
        // since a transaction is done over one hash table/server only
        // however, since each transaction writes either 5 or 20 key-value pairs under
        // one machine
        // it will probably fill a handful of those tables before reading from them
        // so, it isn't much of an issue

        OperationsShuffler opsShuffler = new OperationsShuffler();
        Integer[] shuffledOps = opsShuffler.shuffledArray(transactions, writes);

        for (int i = 0; i < transactions; i++) {
            // robjects = cs.chooseObjects(servers, objects, objectsPerTransaction, random);
            op = shuffledOps[i]; // cop.chooseOP(writes, random);
            // transaction.execTransaction(robjects, op);
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

        // System.out.println("gravando arquivo");

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

    /*
     * public void execTransaction(RObject[] robjects, int op) throws Exception {
     * 
     * 
     * TMObj<IHTMachine>[] TMObjects = new TMObj[robjects.length];
     * for (int i = 0; i < TMObjects.length; i++) {
     * TMObjects[i] = (TMObj<IHTMachine>) TMObj.lookupTMObj("rmi://localhost:" +
     * robjects[i].getPort() + "/" + robjects[i].getAddress());
     * }
     * 
     * int donewithdraw = 0;
     * 
     * donewithdraw = (int) Transaction.atomic(new Callable<Integer>() {
     * public Integer call() throws Exception {
     * int localwithdraw = 0;
     * Random rng = new Random();
     * if (op == 0) {
     * IHTMachine iht = TMObjects[0].openWrite();
     * iht.insert(rng.nextInt(10*100), rng.nextInt(Integer.MAX_VALUE));
     * }
     * 
     * if (op == 2) {
     * IHTMachine iht = TMObjects[0].openRead();
     * iht.get(rng.nextInt(10*100));
     * }
     * return localwithdraw;
     * }
     * });
     * 
     * // SANITY CHECK:
     * commits.getAndIncrement();
     * 
     * if (op == 0) {
     * inserts.getAndIncrement();
     * }
     * 
     * if (op == 1) {
     * gets.getAndIncrement();
     * }
     * }
     */

    @SuppressWarnings("unchecked")
    @Override
    public void execTransaction(int nServers, int numberOfKeys, int nObjectsPerTransaction, int hashTablesEntries,
            int op) throws Exception {
        Random rng = new Random();

        // Iterate and calculate the machineId needed for each operation within the
        // Distributed Hash Table
        // int[] machinesIds = new int[nObjects];
        int[] keys = new int[nObjectsPerTransaction];// [TMObjects.length];
        int[] values = new int[nObjectsPerTransaction];

        int serverNum = 0;
        int port = 1700;
        String bucketName = "bucket0";

        serverNum = rng.nextInt(nServers);
        port = 1700 + serverNum;

        int bound = numberOfKeys;
        HashSet<Integer> keysGenerated = new HashSet<>();
        TMObj<INode<Integer>>[] TMOBuckets = new TMObj[nObjectsPerTransaction];
        INode<Integer>[] bucketsHeads = new INode[nObjectsPerTransaction];

        for (int i = 0; i < nObjectsPerTransaction; i++) {
            //serverNum = rng.nextInt(nServers);
            //port = 1700 + serverNum;

            // min + rng.nextInt(max - min);
            // Limits the key generation within the bounds of the minimum and the maximum
            // values for the current server
            do {
                keys[i] = (bound / nServers) * serverNum
                    + rng.nextInt((bound / nServers) * (serverNum + 1) - (bound / nServers) * serverNum);
            } while (keysGenerated.contains(keys[i]));
            
            bucketName = "bucket" + keys[i] % hashTablesEntries;
            values[i] = rng.nextInt(Integer.MAX_VALUE);
            TMOBuckets[i] = (TMObj<INode<Integer>>) TMObj.lookupTMObj("rmi://localhost:" + port + "/" + bucketName);
        }

        INode<Integer>[] results = Transaction.atomic(new Callable<INode<Integer>[]>() {
            public INode<Integer>[] call() throws Exception {
                INode<Integer>[] nodesFound = new INode[keys.length];

                if (op == 0) {

                    for (int i = 0; i < nObjectsPerTransaction; i++) {
                        bucketsHeads[i] = TMOBuckets[i].openWrite();
                    }

                    for (int i = 0; i < nObjectsPerTransaction; i++) {
                        //System.out.println("INSERT " + i);
                        bucketsHeads[i].insert(keys[i], values[i]);
                    }
                }

                if (op == 1) {
                    for (int i = 0; i < nObjectsPerTransaction; i++) {
                        bucketsHeads[i] = TMOBuckets[i].openRead();
                    }

                    for (int i = 0; i < nObjectsPerTransaction; i++) {
                        //System.out.println("GET " + i);
                        bucketsHeads[i].get(keys[i]);
                    }
                }
                return nodesFound;
            }
        });

        commits.getAndIncrement();

        if (op == 0) {
            inserts.getAndIncrement();
        }

        if (op == 1) {
            gets.getAndIncrement();
        }
    }
}