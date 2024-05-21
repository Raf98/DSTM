package DHT;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.concurrent.Callable;

import TinyTM.Transaction;
import TinyTM.ofree.TMObjServer;

public class SHashTable extends UnicastRemoteObject implements IHashTable {

    String addressName;
    int numberHTEntries;
    public static int NUMBER_OF_MACHINES = 10;
    private TMObjServer<INode<Integer>>[] heads;

    @SuppressWarnings("unchecked")
    public SHashTable(int machineId, int numberHTEntries, int contentionManager) throws RemoteException{
        this.addressName = "ht" + machineId;
        this.numberHTEntries = numberHTEntries;

        heads = new TMObjServer[numberHTEntries];
        for(int i = 0; i < numberHTEntries; ++i) {
            SNode<Integer> newLLHead = new SNode<Integer>(-1, i);
            System.out.println("NEW NODE NAME CREATED: " + newLLHead.toString());
            heads[i] = new TMObjServer<INode<Integer>>(newLLHead);
        }

        Transaction.setContentionManager(contentionManager);
    }

    @Override
    public void copyTo(IHashTable target) throws RemoteException {
        // ((IHTMachine)target).setField0(this.field0);
    }

    @Override
    public INode<Integer> get(int key) throws RemoteException, Exception {

        TMObjServer<INode<Integer>> headTMObjServer = heads[key % numberHTEntries];

        return Transaction.atomic(new Callable<INode<Integer>>() {
            public INode<Integer> call() throws Exception {
                Transaction localTransaction = Transaction.getLocal(); 

                INode<Integer> headNode = headTMObjServer.openWriteRemote(localTransaction);
                //System.out.println("WRITING..." + i + ", KEY: " + keys[i] + ", " + "MACHINE: " + machinesIds[i]);
                
                for (TMObjServer<INode<Integer>> tmObjServerNode = headNode.getNext(); tmObjServerNode != null; ) {
                    INode<Integer> node;
                    node = tmObjServerNode.openReadRemote(localTransaction);
                    if (node.getKey() == key) {
                        return node;
                    }
        
                    tmObjServerNode = node.getNext();
                }
                
                return null;
           }
        });
    }

    @Override
    public boolean insert(int key, int value) throws RemoteException, Exception {
        String port = 1700 + String.valueOf(key % NUMBER_OF_MACHINES);
        String machineName = "object" + port;

        if (addressName.equals(machineName)) {

        }

        TMObjServer<INode<Integer>> headTMObjServer = heads[key % numberHTEntries];

        Transaction.atomic(new Callable<Integer>() {
            public Integer call() throws Exception {
                Random rng = new Random();
                Transaction localTransaction = Transaction.getLocal(); 

                System.out.println("Current CM: " + Transaction.getContentionManager());

                    INode<Integer> headNode = headTMObjServer.openWriteRemote(localTransaction);
                    //System.out.println("WRITING..." + i + ", KEY: " + keys[i] + ", " + "MACHINE: " + machinesIds[i]);

                    INode<Integer> newNode = new SNode<>(key, value);
                    TMObjServer<INode<Integer>> newNodeTmObjServer = new TMObjServer<INode<Integer>>(newNode); 

                    if (headNode.getNext() == null) {
                        System.out.println("FIRST INSERT");
                        headNode.setNext(newNodeTmObjServer);
                        System.out.println("FIRST:" + newNode.toString());
                    } else {
                        for (TMObjServer<INode<Integer>> tmObjServerNode = headNode.getNext(); ;) {
                            INode<Integer> node;
                            node = tmObjServerNode.openReadRemote(localTransaction);
                
                            System.out.println("CURRENT NODE:" + node.toString());
                            if (node.getNext() == null) {
                                System.out.println("NEXT INSERT");
                                node = tmObjServerNode.openWriteRemote(Transaction.getLocal());
                                node.setNext(newNodeTmObjServer);
                                break;
                            }
                            tmObjServerNode = node.getNext();
                        }
                    }
            
                    return 0;
            }
        });

        return true;
    }

}
