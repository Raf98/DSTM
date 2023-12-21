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
        int numberObj = Integer.parseInt(args[1]);

        Remote hashTable = new TMObjServer(new SHashTable("object" + id));

        ServerApp server = new ServerApp();
        try {
            Registry registry = LocateRegistry.createRegistry(1700 + id);
            registry.rebind("object" + id, hashTable);

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
