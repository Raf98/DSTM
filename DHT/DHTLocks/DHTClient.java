package DHT.DHTLocks;

import java.io.FileWriter;
import java.rmi.Naming;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import DHT.ExecuteTransaction;
import DHT.NewSaveData;
import DSTMBenchmark.AppCoordinator;
import DSTMBenchmark.ChooseOP;
import DSTMBenchmark.ClientApp;
import DSTMBenchmark.IDBarrier;
import DSTMBenchmark.RObject;
import TinyTM.Transaction;
import TinyTM.ofree.TMObj;

public class DHTClient {
    public static void main(String[] args) throws Exception {
        System.out.println("começa");

        int clientid = Integer.parseInt(args[0]);
        int servers = Integer.parseInt(args[1]);
        int objects = Integer.parseInt(args[2]);
        int writes = Integer.parseInt(args[3]);
        int transactions = Integer.parseInt(args[4]);
        int objectsPerTransaction = Integer.parseInt(args[5]);
        int contentionManager = Integer.parseInt(args[6]);
        int hashTablesEntries = Integer.parseInt(args[7]);

        ClientApp app = new ClientApp();
        DHTTransaction transaction = new DHTTransaction();

        var cop = new ChooseOPDHT();
        var saveData = new DHTSaveData();
        // var cs = new MyChoiceOfObjects();

        IDBarrier barrier = AppCoordinator.connectToBarrier("barrier"); // (IDBarrier) Naming.lookup("barrier");
        barrier.await();

        Random random = new Random();
        RObject[] robjects;
        int op;
        for (int i = 0; i < transactions; i++) {
            // robjects = cs.chooseObjects(servers, objects, objectsPerTransaction, random);
            op = cop.chooseOP(writes, random);
            op = 0;
            // transaction.execTransaction(robjects, op);
            transaction.execTransaction(servers, objects, objectsPerTransaction, hashTablesEntries, op,
                    contentionManager);
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
                Arrays.asList("aborts", DHTTransaction.aborts.get() + ""));

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

class ChooseOPDHT implements ChooseOP {

    public int chooseOP(int writes, Random random) {
        int op = 0;
        int choice = random.nextInt(100) + 1;
        int choice2 = random.nextInt(2);
        if (choice <= writes) {
            op = 0;// random.nextInt(2);
        } else {
            op = 1;// 2;
        }
        return op;

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
    public void execTransaction(int nServers, int nObjectsServers, int nObjects, int hashTablesEntries, int op,
            int contentionManager) throws Exception {
        Random rng = new Random();
        IHashTable[] machinesForOps = new IHashTable[nServers];

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
        int[] machinesIds = new int[nObjects];
        int[] keys = new int[nObjects];// [TMObjects.length];

        for (int i = 0; i < nObjects; i++) {
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
        }

        for (int i = 0; i < nObjects; i++) {
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

            /*while (!acquiredLocks[i]) {
                if (iHashTable.tryLock()) acquiredLocks[i] = true;
                else aborts.getAndIncrement();
            }*/

            iHashTable.get(keys[i]);

            //iHashTable.unlock();
            //acquiredLocks[i] = false;

            gets.getAndIncrement();
            commits.getAndIncrement();
            System.out.println("GETS: " + gets.get());
        }

        /*
         * int donewithdraw = 0;
         * 
         * donewithdraw = (int) Transaction.atomic(0, new Callable<Integer>() {
         * public Integer call() throws Exception {
         * int localwithdraw = 0;
         * Random rng = new Random();
         * if (op == 0) {
         * 
         * for (int i = 0; i < TMObjects.length; i++) {
         * INode<Integer> iNode = TMObjects[i].openWrite();
         * //System.out.println("TRANSACTION CLIENT ID " + clientId);
         * System.out.println("WRITING..." + i + ", KEY: " + keys[i] + ", " +
         * "MACHINE: " + machinesIds[i]);
         * iNode.insert(machinesIds[i], keys[i], rng.nextInt(Integer.MAX_VALUE));
         * inserts.getAndIncrement();
         * System.out.println("INSERTS: " + inserts.get());
         * }
         * 
         * } else if (op == 1) {
         * 
         * for (int i = 0; i < TMObjects.length; i++) {
         * INode<Integer> iNode = TMObjects[i].openRead();
         * System.out.println("READING...");
         * iNode.get(keys[i]);
         * gets.getAndIncrement();
         * }
         * 
         * }
         * return localwithdraw;
         * }
         * });
         * 
         * // SANITY CHECK:
         * commits.getAndIncrement();
         * 
         * /*if (op == 0) {
         * inserts.getAndIncrement();
         * }
         * 
         * if (op == 1) {
         * gets.getAndIncrement();
         * }
         */
    }
}

/*
 * class MyChoiceOfObjects implements ChooseObjects {
 * 
 * public RObject[] chooseObjects(int nServers, int nObjectsServers, int
 * nObjects, Random random) {
 * RObject[] objects = new RObject[nObjects];
 * int server, obj;
 * HashSet<Pair> set = new HashSet<>();
 * // System.out.println("Size set: " + set.size());
 * while (set.size() < nObjects) {
 * server = random.nextInt(nServers);
 * // System.out.println("server: "+server);
 * obj = random.nextInt(nObjectsServers);
 * // System.out.println("object"+obj);
 * set.add(new Pair(server, obj));
 * }
 * int i = 0;
 * for (Pair p : set) {
 * objects[i++] = new RObject("object" + p.object, 1700 + p.server);
 * }
 * return objects;
 * }
 * 
 * }
 * 
 * class Pair {
 * public int server;
 * public int object;
 * 
 * Pair(int f, int s) {
 * server = f;
 * object = s;
 * }
 * 
 * public boolean equals(Object o) {
 * return ((Pair) o).server == this.server && ((Pair) o).object == this.object;
 * }
 * 
 * public int hashCode() {
 * final int prime = 31;
 * int result = 1;
 * result = prime * result + server;
 * result = prime * result + object;
 * return result;
 * }
 * }
 * 
 */