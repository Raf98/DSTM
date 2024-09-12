package DHT.DHTLocks;

import java.io.FileWriter;
import java.rmi.Naming;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import DHT.ExecuteTransaction;
import DHT.NewSaveData;
import DHT.OperationsShuffler;
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

        IDBarrier barrier = AppCoordinator.connectToBarrier("barrier"); // (IDBarrier) Naming.lookup("barrier");
        barrier.await();

        RObject[] robjects;
        int op;

        OperationsShuffler opsShuffler = new OperationsShuffler();
        Integer[] shuffledOps = opsShuffler.shuffledArray(transactions, writes);

        for (int i = 0; i < transactions; i++) {
            op = shuffledOps[i];
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
        List<List<String>> rows = Arrays.asList(
                Arrays.asList("commits", DHTTransaction.commits.get() + ""),
                Arrays.asList("inserts", DHTTransaction.inserts.get() + ""),
                Arrays.asList("gets", DHTTransaction.gets.get() + ""),
                Arrays.asList("commitsrts", Transaction.commits.get() + ""),
                Arrays.asList("aborts", /*DHT*/Transaction.aborts.get() + ""));

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
    static AtomicInteger aborts;

    static AtomicInteger inserts;
    static AtomicInteger gets;

    DHTTransaction() {
        commits = new AtomicInteger(0);
        aborts = new AtomicInteger(0);

        inserts = new AtomicInteger(0);
        gets = new AtomicInteger(0);
    }

    public boolean allAcquired(boolean acquired[]) {
        for (int i = 0; i < acquired.length; i++)
            if (!acquired[i])
                return false;
        return true;
    }

    @Override
    public void execTransaction(int nServers, int numberOfKeys, int nObjectsPerTransaction, int hashTablesEntries, int op) throws Exception {
        Random rng = new Random();
        /*IHashTable[] machinesForOps = new IHashTable[nServers];

        // Lookup for Hash Tables within their machines/ servers in the network
        for (int i = 0; i < machinesForOps.length; i++) {
            String port = String.valueOf(1700 + i);
            String nodeName = "ht" + i;

            System.out.println("NODE NAME: " + nodeName);
            System.out.println("rmi://localhost:" + port + "/" + nodeName);

            machinesForOps[i] = (IHashTable) Naming.lookup("rmi://localhost:" + port + "/" + nodeName);
        }

        boolean [] acquiredLocks = new boolean[machinesForOps.length];

        // Iterate and calculate the machineId needed for each operation within the
        // Distributed Hash Table
        int[] machinesIds = new int[nObjects];*/
        int[] keys = new int[nObjectsPerTransaction];// [TMObjects.length];
        int[] values = new int[nObjectsPerTransaction];

        int serverNum = rng.nextInt(nServers);
        String port = String.valueOf(1700 + serverNum);
        String nodeName = "ht" + serverNum;
        IHashTable serverForOps = (IHashTable) Naming.lookup("rmi://localhost:" + port + "/" + nodeName);

        /*for (int i = 0; i < nObjectsPerTransaction; i++) {
            int bound = 1000;
            keys[i] = rng.nextInt(bound);

            int inc = bound / nServers;
            int count = 0;
            int j = 0;
            for (; j < nServers; j++) {
                count += inc;
                if (keys[i] < count) {
                    break;
                }
            }

            machinesIds[i] = j;
        }*/

        for (int i = 0; i < nObjectsPerTransaction; i++) {
            int bound = numberOfKeys;
            //min + rng.nextInt(max - min);
            // Limits the key generation within the bounds of the minimum and the maximum values for the current server
            keys[i] = (bound / nServers) * serverNum + rng.nextInt((bound / nServers) * (serverNum + 1) - (bound / nServers) * serverNum);
        }

        if (op == 0) {
            /*for (int i = 0; i < nObjectsPerTransaction; i++) {
                IHashTable iHashTable = machinesForOps[machinesIds[i]];
                //System.out.println("TRANSACTION CLIENT ID " + clientId);
                System.out.println("WRITING..." + i + ", KEY: " + keys[i] + ", " + "MACHINE: " + machinesIds[i]);
                iHashTable.insert(keys[i], rng.nextInt(Integer.MAX_VALUE));
                inserts.getAndIncrement();
                commits.getAndIncrement();
                System.out.println("INSERTS: " + inserts.get());
            }*/
            for (int i = 0; i < nObjectsPerTransaction; i++) {
                values[i] = rng.nextInt(Integer.MAX_VALUE);
            }
            serverForOps.insertMultiple(keys, values);
            inserts.getAndIncrement();
            commits.getAndIncrement();
            //System.out.println("INSERTS: " + inserts.get());
        } else {
            /*for (int i = 0; i < nObjectsPerTransaction; i++) {
                IHashTable iHashTable = machinesForOps[machinesIds[i]];
                //System.out.println("TRANSACTION CLIENT ID " + clientId);
                System.out.println("READING..." + i + ", KEY: " + keys[i] + ", " + "MACHINE: " + machinesIds[i]);
                iHashTable.get(keys[i]);
                gets.getAndIncrement();
                commits.getAndIncrement();
                System.out.println("GETS: " + gets.get());
            }*/
            serverForOps.getMultiple(keys);
            gets.getAndIncrement();
            commits.getAndIncrement();
            //System.out.println("GETS: " + gets.get());
        }

        /*for (int i = 0; i < nObjects; i++) {
            IHashTable iHashTable = machinesForOps[machinesIds[i]];
            // System.out.println("TRANSACTION CLIENT ID " + clientId);
            System.out.println("WRITING..." + i + ", KEY: " + keys[i] + ", " + "MACHINE: " + machinesIds[i]);

            while (!acquiredLocks[i]) {
                if (iHashTable.tryLock()) acquiredLocks[i] = true;
                else aborts.getAndIncrement();
                //System.out.println("LOCK STATE: " + acquiredLocks[i]);
            }

            iHashTable.insert(keys[i], rng.nextInt(Integer.MAX_VALUE));

            iHashTable.unlock();
            acquiredLocks[i] = false;

            inserts.getAndIncrement();
            commits.getAndIncrement();
            System.out.println("INSERTS: " + inserts.get());
        }

        for (int i = 0; i < nObjects; i++) {
            IHashTable iHashTable = machinesForOps[machinesIds[i]];
            // System.out.println("TRANSACTION CLIENT ID " + clientId);
            System.out.println("READING..." + i + ", KEY: " + keys[i] + ", " + "MACHINE: " + machinesIds[i]);

            while (!acquiredLocks[i]) {
                if (iHashTable.tryLock()) acquiredLocks[i] = true;
                else aborts.getAndIncrement();
            }

            iHashTable.get(keys[i]);

            iHashTable.unlock();
            acquiredLocks[i] = false;

            gets.getAndIncrement();
            commits.getAndIncrement();
            System.out.println("GETS: " + gets.get());
        }*/
    }
}