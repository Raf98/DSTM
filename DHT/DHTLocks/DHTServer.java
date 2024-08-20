package DHT.DHTLocks;

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
        int contentionManager = Integer.parseInt(args[2]);

        int maxAborts_minDelay_delay = 64;
        int maxDelay_intervals = 256;

        // If CM is not Less or Agressive, that have no settable parameters
        if (args.length > 3 && contentionManager < 6) {
            maxAborts_minDelay_delay = Integer.parseInt(args[3]);
            
            if (args.length > 4 && contentionManager > 0) {
                maxDelay_intervals = Integer.parseInt(args[4]);
            }
        }

        Remote localHashTable;
        
        if (args.length < 3) {
            localHashTable = new SHashTable(id, numberHTEntries, contentionManager);
        } else {
            localHashTable = new SHashTable(id, numberHTEntries, contentionManager,
                                                        maxAborts_minDelay_delay, maxDelay_intervals);
        }

        //ServerApp server = new ServerApp();
        try {
            Registry registry = LocateRegistry.createRegistry(1700 + id);
            registry.rebind("ht" + id, localHashTable);

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
