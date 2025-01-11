package DHT;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import DSTMBenchmark.AppCoordinator;
import DSTMBenchmark.IDBarrier;

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

            System.out.println("Contention Manager Dynamic Parameters:");
            System.out.println("maxAborts/minDelay/delay: " + maxAborts_minDelay_delay);
            
            if (args.length > 4 && contentionManager > 0) {
                maxDelay_intervals = Integer.parseInt(args[4]);

                System.out.println("maxDelay_intervals: " + maxDelay_intervals);
            }
        }

        /*
        //IHashTable hashTableImp = new SHashTable("ht" + id);
        //Remote hashTable = new TMObjServer(new SHashTable("ht" + id));
        Remote[] nodes = new Remote[numberHTEntries];
        // TENHO QUE REGISTRAR CADA UMA DAS CABEÇAS DE NODOS EM UM DETERMINADA MÁQUINA, ASSIM REPRESENTANDO A HASHTABLE?
        for(int i = 0; i < numberHTEntries; ++i) {
            SNode<Integer> newNode = new SNode<Integer>(-1, i, "ht" + id + "_node" + i);
            System.out.println("NEW NODE NAME CREATED: " + newNode.name);
            System.out.println("NEW NODE NAME CREATED: " + newNode.getName());
            //hashTableImp.insert(i, 0);
            nodes[i] = new TMObjServer<>(newNode);
        }
         */

        Remote localHashTable;
        
        localHashTable = new SHashTable(id, numberHTEntries, contentionManager,
                                                        maxAborts_minDelay_delay, maxDelay_intervals);

        //ServerApp server = new ServerApp();
        try {
            Registry registry = LocateRegistry.createRegistry(1700 + id);
            registry.rebind("ht" + id, localHashTable);

            /*for (int i = 0; i < numberHTEntries; i++) {
                registry.rebind("ht" + id + "_node" + i, nodes[i]);
                System.out.println("NODE NAME ON SERVER: " + (1700 + id) + "/ht" + id + "_node" + i);
            }*/

            IDBarrier barrier = AppCoordinator.connectToBarrier("serverbarrier");// (IDBarrier)
                                                                                 // Naming.lookup("serverbarrier");
            barrier.await(); // waits for all servers to be up and running
            barrier.await(); // waits for clients to finish work
            UnicastRemoteObject.unexportObject(registry, true);
            System.exit(0);
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("SERVER APP EXCEPTION:");
			System.out.println("Server ID: " + id + "; Server Port: " + (1666 + id));
			System.out.println(e.getMessage());
        }
        System.exit(0);
    }
}
