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

        //IHashTable hashTableImp = new SHashTable("ht" + id);
        //Remote hashTable = new TMObjServer(new SHashTable("ht" + id));
        Remote[] linkedLists = new Remote[numberHTEntries];
        //INode<Integer> nodes = new INode[numberHTEntries];
        // TENHO QUE REGISTRAR CADA UMA DAS CABEÇAS DE NODOS EM UM DETERMINADA MÁQUINA, ASSIM REPRESENTANDO A HASHTABLE?
        for(int i = 0; i < numberHTEntries; ++i) {
            INode<Integer> headNode = new SNode<Integer>(-1, id, "ht" + id + "_ll_head" + i);
            System.out.println("NEW LL HEAD NODE NAME CREATED: " + headNode.getName());
            //hashTableImp.insert(i, 0);
            //nodes[i] =  new TMObjServer<>(newNode);
            linkedLists[i] = new TMObjServer<>(new SLinkedList<>(id, "ht" + id + "_ll" + i, headNode));
        }

        ServerApp server = new ServerApp();
        try {
            Registry registry = LocateRegistry.createRegistry(1700 + id);
            //registry.rebind("ht" + id, hashTable);

            for (int i = 0; i < numberHTEntries; i++) {
                registry.rebind("ht" + id + "_ll" + i, linkedLists[i]);
                System.out.println("LINKED LIST NAME ON SERVER: " + (1700 + id) + "/ht" + id + "_ll" + i);
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
