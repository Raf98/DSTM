package DHT.DHTLocks;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import TinyTM.Transaction;
import TinyTM.ofree.TMObjServer;

public class SHashTable extends UnicastRemoteObject implements IHashTable {

    String addressName;
    int numberHTEntries;
    public static int NUMBER_OF_MACHINES = 10;
    private INode<Integer>[] heads;
    private Semaphore lock;

    static AtomicInteger aborts = new AtomicInteger(0);
    static AtomicInteger commits = new AtomicInteger(0);

    @SuppressWarnings({ "unchecked" })
    public SHashTable(int machineId, int numberHTEntries, int contentionManager) throws RemoteException{
        this.lock = new Semaphore(1);

        this.addressName = "ht" + machineId;
        this.numberHTEntries = numberHTEntries;

        heads = new INode[numberHTEntries];
        for(int i = 0; i < numberHTEntries; ++i) {
            SNode<Integer> newLLHead = new SNode<Integer>(-1, i);
            System.out.println("NEW NODE NAME CREATED: " + newLLHead.toString());
            heads[i] = newLLHead;
        }

        Transaction.setContentionManager(contentionManager);
    }

    @SuppressWarnings({ "unchecked" })
    public SHashTable(int machineId, int numberHTEntries, int contentionManager, 
                        int maxAborts_minDelay_delay, int maxDelay_intervals) throws RemoteException{
        this.lock = new Semaphore(1);

        this.addressName = "ht" + machineId;
        this.numberHTEntries = numberHTEntries;

        heads = new INode[numberHTEntries];
        for(int i = 0; i < numberHTEntries; ++i) {
            SNode<Integer> newLLHead = new SNode<Integer>(-1, i);
            System.out.println("NEW NODE NAME CREATED: " + newLLHead.toString());
            heads[i] = newLLHead;
        }

        Transaction.setContentionManager(contentionManager, maxAborts_minDelay_delay, 
                                        maxDelay_intervals);
    }
    

    @Override
    public INode<Integer> get(int key) throws RemoteException, Exception {

        INode<Integer> headNode = heads[key % numberHTEntries];

        INode<Integer> nodeFound = null;

        //System.out.println("WRITING..." + i + ", KEY: " + keys[i] + ", " + "MACHINE: " + machinesIds[i]);
        
        for (INode<Integer> node = headNode.getNext(); node != null; node = node.getNext()) {
            if (node.getKey() == key) {
                nodeFound = node;
                break;
            }
        }
                
        aborts.set(Transaction.getLocal().getAborts());
        commits.set(Transaction.getLocal().getCommits());

        return nodeFound;
    }

    @Override
    public boolean insert(int key, int value) throws RemoteException, Exception {
        String port = 1700 + String.valueOf(key % NUMBER_OF_MACHINES);
        String machineName = "object" + port;

        if (addressName.equals(machineName)) {

        }

        INode<Integer> headNode = heads[key % numberHTEntries];

        Transaction.atomic(new Callable<Integer>() {
            public Integer call() throws Exception {
                Random rng = new Random();

                System.out.println("Current CM: " + Transaction.getContentionManager());
                    //System.out.println("WRITING..." + i + ", KEY: " + keys[i] + ", " + "MACHINE: " + machinesIds[i]);

                    INode<Integer> newNode = new SNode<>(key, value);

                    if (headNode.getNext() == null) {
                        System.out.println("FIRST INSERT");
                        headNode.setNext(newNode);
                        System.out.println("FIRST:" + newNode.toString());
                    } else {
                        for (INode<Integer> node = headNode.getNext(); ;) {
                
                            System.out.println("CURRENT NODE:" + node.toString());
                            if (node.getNext() == null) {
                                System.out.println("NEXT INSERT");
                                node.setNext(newNode);
                                break;
                            }
                            node = node.getNext();
                        }
                    }
            
                    return 0;
            }
        });

        aborts.set(Transaction.getLocal().getAborts());
        commits.set(Transaction.getLocal().getCommits());

        return true;
    }

    @Override
    public int getAborts() {
        return aborts.get();
    }

    @Override
    public int getCommits() {
        return commits.get();
    }

    @Override
    public boolean tryLock() throws RemoteException{
        return lock.tryAcquire();
    }
 
    @Override
    public void unlock() throws RemoteException{
        lock.release();
    }

}