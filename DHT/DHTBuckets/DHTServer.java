package DHT.DHTBuckets;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import DSTMBenchmark.AppCoordinator;
import DSTMBenchmark.IDBarrier;
import TinyTM.ofree.TMObjServer;

public class DHTServer {
    // args[0] == Server ID: starts from zero
    // args[1] == Number of objects in the server

    public static void main(String[] args) throws Exception {

        int id = Integer.parseInt(args[0]);
        int numberHTEntries = Integer.parseInt(args[1]);

        Remote[] htBuckets = new Remote[numberHTEntries];
        // TENHO QUE REGISTRAR CADA UMA DAS CABEÇAS DE NODOS EM UM DETERMINADA MÁQUINA, ASSIM REPRESENTANDO A HASHTABLE?
        for(int i = 0; i < numberHTEntries; ++i) {
            SNode<Integer> newNode = new SNode<Integer>(-1, i);
            htBuckets[i] = new TMObjServer<>(newNode);
        }

        try {
            Registry registry = LocateRegistry.createRegistry(1700 + id);

            for (int i = 0; i < numberHTEntries; i++) {
                registry.rebind("bucket" + i, htBuckets[i]);
                //System.out.println("NODE NAME ON SERVER: " + (1700 + id) + "/bucket" + i);
            }

            IDBarrier barrier = AppCoordinator.connectToBarrier("serverbarrier");// (IDBarrier)
                                                                                 // Naming.lookup("serverbarrier");
            barrier.await(); // waits for all servers to be up and running
            barrier.await(); // waits for clients to finish work
            UnicastRemoteObject.unexportObject(registry, true);
            System.exit(0);
        } catch (Exception e) {
            //e.printStackTrace();
            /*System.out.println("SERVER APP EXCEPTION:");
			System.out.println("Server ID: " + id + "; Server Port: " + (1666 + id));
			System.out.println(e.getMessage());*/
        }
        System.exit(0);
    }
}