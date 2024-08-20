package DHT.DHTLocks;

import java.io.BufferedReader;
import java.io.FileReader;
import java.rmi.Naming;

import DSTMBenchmark.AppCoordinator;
import DSTMBenchmark.ProcessData;
import TinyTM.Transaction;

public class DHTCoordinator {
    public static void main(String[] args) {
        AppCoordinator coordinator = new AppCoordinator();
        DHTProcessData processData = new DHTProcessData();
        coordinator.runCoordinator(processData, args);
    }
}

class DHTProcessData implements ProcessData {

    public void processData(int numberOfServers, int numberOfClients, int numberOfObjects) throws Exception {

        int gets = 0;
        int inserts = 0;
        int commits = 0;
        int commitsrts = 0;
        int aborts = 0;
        String[] data;
        BufferedReader csvReader;

        for (int i = 0; i < numberOfClients; i++) {
            csvReader = new BufferedReader(new FileReader("client" + i + ".out"));

            data = csvReader.readLine().split(",");
            commits = commits + Integer.parseInt(data[1]);
            data = csvReader.readLine().split(",");
            inserts = inserts + Integer.parseInt(data[1]);
            data = csvReader.readLine().split(",");
            gets = gets + Integer.parseInt(data[1]);
            data = csvReader.readLine().split(",");
            commitsrts = commitsrts + Integer.parseInt(data[1]);
            data = csvReader.readLine().split(",");
            aborts = aborts + Integer.parseInt(data[1]);

            csvReader.close();
        }

        Transaction.setLocal(Transaction.COMMITTED);

        int total = 0;

        IHashTable htServer;

        /*
         * System.out.println("before list!");
         * 
         * String names[] = Naming.list("rmi://localhost:1666");
         * for (int i = 0; i < names.length; i++) {
         * System.out.println(names[i]);
         * }
         */

        // System.out.println("after list!");

        for (int i = 0; i < numberOfServers; i++) {
            String port = String.valueOf(1700 + i);
            String nodeName = "ht" + i;
            htServer = (IHashTable) Naming.lookup("rmi://localhost:" + port + "/" + nodeName);
            commitsrts += htServer.getCommits();
            System.out.printf("Current commits: %d\n", commitsrts);
        }

        System.out.printf("Total of commits: %d (Expected: %d)\n", commits, commitsrts);
        System.out.printf("Total of inserts: %d \n", inserts);
        System.out.printf("Total of gets: %d \n", gets);
        System.out.printf("Total of aborts: %d \n", aborts);

        /*int totalOperations = setminus + setplus + gets + transfer;
        // int totalExpected = ((commits-(transfer/2)) * 4) + transfer;
        System.out.printf("Total of operations: %d\n", totalOperations);
        int totalMoney = ((4 * numberOfServers * numberOfObjects * 1000) + (setplus * 100)) - (setminus * 100);
        System.out.printf("Total value of fields: %d (Expected: %d)\n", total, totalMoney);*/

    }
}