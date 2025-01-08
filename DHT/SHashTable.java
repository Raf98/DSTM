package DHT;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import TinyTM.Transaction;
import TinyTM.ofree.TMObjServer;

public class SHashTable extends UnicastRemoteObject implements IHashTable {

    String addressName;
    int numberHTEntries;
    public static int NUMBER_OF_MACHINES = 10;
    private TMObjServer<INode<Integer>>[] heads;

    static AtomicInteger aborts = new AtomicInteger(0);
    static AtomicInteger commits = new AtomicInteger(0);

    @SuppressWarnings("unchecked")
    public SHashTable(int machineId, int numberHTEntries, int contentionManager) throws RemoteException {
        this.addressName = "ht" + machineId;
        this.numberHTEntries = numberHTEntries;

        heads = new TMObjServer[numberHTEntries];
        for (int i = 0; i < numberHTEntries; ++i) {
            SNode<Integer> newLLHead = new SNode<Integer>(-1, i);
            // System.out.println("NEW NODE NAME CREATED: " + newLLHead.toString());
            heads[i] = new TMObjServer<INode<Integer>>(newLLHead);
        }

        Transaction.setContentionManager(contentionManager);
    }

    @SuppressWarnings("unchecked")
    public SHashTable(int machineId, int numberHTEntries, int contentionManager,
            int maxAborts_minDelay_delay, int maxDelay_intervals) throws RemoteException {
        this.addressName = "ht" + machineId;
        this.numberHTEntries = numberHTEntries;

        heads = new TMObjServer[numberHTEntries];
        for (int i = 0; i < numberHTEntries; ++i) {
            SNode<Integer> newLLHead = new SNode<Integer>(-1, i);
            // System.out.println("NEW NODE NAME CREATED: " + newLLHead.toString());
            heads[i] = new TMObjServer<INode<Integer>>(newLLHead);
        }

        Transaction.setContentionManager(contentionManager, maxAborts_minDelay_delay,
                maxDelay_intervals);
    }

    @Override
    public INode<Integer> get(int key) throws RemoteException, Exception {

        TMObjServer<INode<Integer>> headTMObjServer = heads[key % numberHTEntries];

        INode<Integer> nodeFound = Transaction.atomic(new Callable<INode<Integer>>() {
            public INode<Integer> call() throws Exception {
                Transaction localTransaction = Transaction.getLocal();

                INode<Integer> headNode = headTMObjServer.openReadRemote(localTransaction);
                //System.out.println("READING: KEY: " + node.getKey() + ", " + "VALUE: " + node.getValue());

                for (TMObjServer<INode<Integer>> tmObjServerNode = headNode.getNext(); tmObjServerNode != null;) {
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

        aborts.set(Transaction.getLocal().getAborts());
        commits.set(Transaction.getLocal().getCommits());

        return nodeFound;
    }

    @Override
    public boolean insert(int key, int value) throws RemoteException, Exception {
        TMObjServer<INode<Integer>> headTMObjServer = heads[key % numberHTEntries];

        boolean inserted = Transaction.atomic(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                Transaction localTransaction = Transaction.getLocal();

                // System.out.println("Current CM: " + Transaction.getContentionManager());

                INode<Integer> headNode = headTMObjServer.openWriteRemote(localTransaction);
                // System.out.println("WRITING..." + i + ", KEY: " + keys[i] + ", " + "MACHINE:
                // " + machinesIds[i]);

                INode<Integer> newNode = new SNode<>(key, value);
                TMObjServer<INode<Integer>> newNodeTmObjServer = new TMObjServer<INode<Integer>>(newNode);

                if (headNode.getNext() == null) {
                    // System.out.println("FIRST INSERT");
                    headNode.setNext(newNodeTmObjServer);
                    // System.out.println("FIRST:" + newNode.toString());
                    return true;
                } else {
                    for (TMObjServer<INode<Integer>> tmObjServerNode = headNode.getNext();;) {
                        INode<Integer> node;
                        node = tmObjServerNode.openReadRemote(localTransaction);

                        // System.out.println("CURRENT NODE:" + node.toString());
                        if (node.getKey() == key) {
                            // System.out.printf("KEY %d: UPDATING VALUE FROM %d TO %d!\n", key,
                            // node.getItem(), value);
                            node.setValue(value);
                            return true;
                        } else if (node.getNext() == null) {
                            // System.out.println("NEXT INSERT");
                            node = tmObjServerNode.openWriteRemote(Transaction.getLocal());
                            node.setNext(newNodeTmObjServer);
                            return true;
                        }
                        tmObjServerNode = node.getNext();
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

    // check if a head has alreday been opened and, if so, return its index
    // it should be refactor to use a hash map maybe (????)
    private int tmObjAlreadyOpen(TMObjServer<INode<Integer>>[] headsTMObjServer, int currentIndex) {
        for (int i = 0; i < currentIndex; i++) {
            if (headsTMObjServer[i].hashCode() == headsTMObjServer[currentIndex].hashCode()) {
                return i;
            }
        }

        return -1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public INode<Integer>[] getMultiple(int[] keys) throws RemoteException, Exception {

        TMObjServer<INode<Integer>>[] headsTMObjServer = new TMObjServer[keys.length];

        for (int i = 0; i < keys.length; i++) {
            headsTMObjServer[i] = heads[keys[i] % numberHTEntries];
        }

        INode<Integer>[] nodesFound = Transaction.atomic(new Callable<INode<Integer>[]>() {
            public INode<Integer>[] call() throws Exception {
                Transaction localTransaction = Transaction.getLocal();

                INode<Integer>[] nodesFound = new INode[keys.length];
                for (int i = 0; i < keys.length; i++) {
                    INode<Integer> headNode = headsTMObjServer[i].openReadRemote(localTransaction);

                    for (TMObjServer<INode<Integer>> tmObjServerNode = headNode.getNext(); tmObjServerNode != null;) {
                        INode<Integer> node;
                        node = tmObjServerNode.openReadRemote(localTransaction);
                        System.out.println("READING: KEY: " + node.getKey() + ", " + "VALUE: " + node.getValue());
                        if (node.getKey() == keys[i]) {
                            nodesFound[i] = node;
                            break;
                        }

                        tmObjServerNode = node.getNext();
                    }
                }

                return nodesFound;
            }
        });

        aborts.set(Transaction.getLocal().getAborts());
        commits.set(Transaction.getLocal().getCommits());

        return nodesFound;
    }

    @SuppressWarnings("unchecked")
	@Override
    public boolean[] insertMultiple(int[] keys, int[] values) throws RemoteException, Exception {
        TMObjServer<INode<Integer>>[] headsTMObjServer = new TMObjServer[keys.length];

        // get the head of the LL for which the key is hashed
        for (int i = 0; i < keys.length; i++) {
            headsTMObjServer[i] = heads[keys[i] % numberHTEntries];
            //.println("KEY: " + keys[i] + "; " + "TMOBJ: " + headsTMObjServer[i].hashCode());
        }

        boolean[] inserteds = Transaction.atomic(new Callable<boolean[]>() {
            public boolean[] call() throws Exception {
                Transaction localTransaction = Transaction.getLocal();

                ///System.out.println("Current CM: " + Transaction.getContentionManager());

                boolean[] inserteds = new boolean[keys.length];
                INode<Integer>[] headNodes = new SNode[keys.length];

                for (int i = 0; i < keys.length; i++) {
                    // if another key has already open a same head for write,
                    // retrieve instead of trying to reopening it for write within  the same transaction,
                    // which results in an error
                    /*int openedIndex = tmObjAlreadyOpen(headsTMObjServer, i);

                    if (openedIndex == -1) {
                        headNodes[i] = headsTMObjServer[i].openWriteRemote(localTransaction); 
                    } else {
                        //System.out.println("ALREADY OPENED: " + headNodes[openedIndex]);
                        headNodes[i] = headNodes[openedIndex];
                    }*/

                    //System.out.println("WRITING..." + i + ", KEY: " + keys[i]);

                    // open the head of the current LL for reading, because it will be needed to check
                    // whether or not it should be written (its next field, more specifically)
                    headNodes[i] = headsTMObjServer[i].openReadRemote(localTransaction);

                    INode<Integer> newNode = new SNode<>(keys[i], values[i]);
                    TMObjServer<INode<Integer>> newNodeTmObjServer = new TMObjServer<INode<Integer>>(newNode);

                    if (headNodes[i].getNext() == null) {
                        //System.out.println("FIRST INSERT");
                        headNodes[i] = headsTMObjServer[i].openWriteRemote(localTransaction);
                        headNodes[i].setNext(newNodeTmObjServer);
                        System.out.println("FIRST NODE: " + newNode.toString() + 
                                           "; INDEX: " + keys[i] % numberHTEntries + 
                                           "; SERVER: " + addressName);
                        inserteds[i] = true;
                    } else {
                        for (TMObjServer<INode<Integer>> tmObjServerNode = headNodes[i].getNext(); tmObjServerNode != null;) {
                            INode<Integer> node;
                            node = tmObjServerNode.openReadRemote(localTransaction);

                            System.out.println("CURRENT NODE:" + node.toString());
                            if (node.getKey() == keys[i]) {
                                System.out.printf("KEY %d; INDEX %d; SERVER %s: UPDATING VALUE FROM %d TO %d!\n", 
                                                    keys[i], keys[i] % numberHTEntries, addressName, node.getValue(), values[i]);
                                node.setValue(values[i]);
                                inserteds[i] = true;
                                break;
                            } else if (node.getNext() == null) {
                                System.out.printf("KEY %d; INDEX %d; SERVER %s: INSERTING %d!\n", 
                                                    keys[i], keys[i] % numberHTEntries, addressName, values[i]);
                                node = tmObjServerNode.openWriteRemote(Transaction.getLocal());
                                node.setNext(newNodeTmObjServer);
                                inserteds[i] = true;
                                break;
                            }
                            tmObjServerNode = node.getNext();
                        }
                    }
                }

                return inserteds;
            }
        });

        aborts.set(Transaction.getLocal().getAborts());
        commits.set(Transaction.getLocal().getCommits());

        return inserteds;
    }

}