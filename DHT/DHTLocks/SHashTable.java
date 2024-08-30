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
    public synchronized INode<Integer> get(int key) throws RemoteException, Exception {

        INode<Integer> headNode = heads[key % numberHTEntries];

        INode<Integer> nodeFound = null;
        
        for (INode<Integer> node = headNode.getNext(); node != null; node = node.getNext()) {
            System.out.println("READING: KEY: " + node.getKey() + ", " + "VALUE: " + node.getValue());
            if (node.getKey() == key) {
                nodeFound = node;
                System.out.println("NODE FOUND!");
                break;
            }
        }

        if (nodeFound == null) {
            System.out.println("NODE NOT FOUND!");
        }
                
        aborts.set(Transaction.getLocal().getAborts());
        commits.set(Transaction.getLocal().getCommits());

        return nodeFound;
    }

    @Override
    public synchronized boolean insert(int key, int value) throws RemoteException, Exception {
        INode<Integer> headNode = heads[key % numberHTEntries];

        boolean inserted = Transaction.atomic(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                System.out.println("Current CM: " + Transaction.getContentionManager());
                System.out.println("WRITING: KEY: " + key + ", " + "VALUE: " + value);

                    INode<Integer> newNode = new SNode<>(key, value);

                    if (headNode.getNext() == null) {
                        System.out.println("FIRST INSERT");
                        headNode.setNext(newNode);
                        System.out.println("FIRST:" + newNode.toString());
                        return true;
                    } else {
                        for (INode<Integer> node = headNode.getNext(); ;) {
                
                            System.out.println("CURRENT NODE:" + node.toString());
                            if (node.getKey() == key) {
                                System.out.printf("KEY %d: UPDATING VALUE FROM %d TO %d!\n", key, node.getValue(), value);
                                node.setValue(value);
                                return true;
                            } else if (node.getNext() == null) {
                                System.out.println("NEXT INSERT");
                                node.setNext(newNode);
                                return true;
                            }
                            node = node.getNext();
                        }
                    }            
            }
        });

        aborts.set(Transaction.getLocal().getAborts());
        commits.set(Transaction.getLocal().getCommits());

        return inserted;
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