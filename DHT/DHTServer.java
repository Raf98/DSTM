package DHT;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import DSTMBenchmark.AppCoordinator;
import DSTMBenchmark.IDBarrier;
import DSTMBenchmark.ServerApp;
import TinyTM.ofree.TMObjServer;

public class DHTServer {
    // args[0] == Server ID: starts from zero
    // args[1] == Number of objects in the server

    public static void main(String[] args) throws Exception {

        int id = Integer.parseInt(args[0]);
        int numberHTEntries = Integer.parseInt(args[1]);

        IHashTable hashTableImp = new SHashTable("ht" + id);
        Remote hashTable = new TMObjServer(new SHashTable("ht" + id));
        Remote[] nodes = new Remote[numberHTEntries];
        // TENHO QUE REGISTRAR CADA UMA DAS CABEÇAS DE NODOS EM UM DETERMINADA MÁQUINA, ASSIM REPRESENTANDO A HASHTABLE?
        for(int i = 0; i < numberHTEntries; ++i) {
            SNode newNode = new SNode(-1, 0, "ht" + id + "_node" +i);
            //hashTableImp.insert(i, 0);
            nodes[i] = new TMObjServer(newNode);
        }

        ServerApp server = new ServerApp();
        try {
            Registry registry = LocateRegistry.createRegistry(1700 + id);
            registry.rebind("ht" + id, hashTable);

            for (int i = 0; i < numberHTEntries; i++) {
                registry.rebind("ht" + id + "_node" +i, nodes[i]);
            }

            IDBarrier barrier = AppCoordinator.connectToBarrier("serverbarrier");// (IDBarrier)
                                                                                 // Naming.lookup("serverbarrier");
            barrier.await(); // waits for all servers to be up and running
            barrier.await(); // waits for clients to finish work
            UnicastRemoteObject.unexportObject(registry, true);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
