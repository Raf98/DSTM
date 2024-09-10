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
        long aborts = 0;
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

        IHashTable htServer;

        for (int i = 0; i < numberOfServers; i++) {
            String port = String.valueOf(1700 + i);
            String nodeName = "ht" + i;
            htServer = (IHashTable) Naming.lookup("rmi://localhost:" + port + "/" + nodeName);
            commitsrts += htServer.getCommits();
            aborts += htServer.getAborts();
            System.out.printf("Current commits: %d\n", commitsrts);
            System.out.printf("Current aborts: %d\n", aborts);
        }

        System.out.printf("Total of commits: %d (Expected: %d)\n", commits, commitsrts);
        System.out.printf("Total of inserts: %d \n", inserts);
        System.out.printf("Total of gets: %d \n", gets);
        System.out.printf("Total of aborts: %d \n", aborts);
    }
}