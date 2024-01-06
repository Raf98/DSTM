package DHT;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Callable;

import TinyTM.Transaction;
import TinyTM.ofree.TMObj;

public class SHashTable extends UnicastRemoteObject implements IHashTable {

    int key, value;
    String addressName;
    public static int NUMBER_OF_MACHINES = 10;
    private TMObj<INode<Integer>> ht[];
    private HashTable hashTable;
    private Map<Integer, String> routingTable;              //armazena para cada valor de e

    protected SHashTable(String addressName) throws RemoteException {
        super();
        this.addressName = addressName;
        ht = new TMObj[NUMBER_OF_MACHINES];
        hashTable = new HashTable();
        routingTable = Collections.synchronizedMap(new HashMap<Integer, String>());
    }

    protected SHashTable(int key, int value, String addressName) throws RemoteException {
        super();
        this.key = key;
        this.value = value;
        this.addressName = addressName;
        hashTable = new HashTable();
    }

    @Override
    public void copyTo(IHashTable target) throws RemoteException {
        // ((IHTMachine)target).setField0(this.field0);
    }

    @Override
    public TMObj<INode<Integer>> get(int key) throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }

    @Override
    public boolean insert(int key, int value) throws RemoteException, Exception {

        hashTable.set(key, value);

        /*
        String port = 1700 + String.valueOf(key % NUMBER_OF_MACHINES);
        String machineName = "object" + port;

        if (addressName.equals(machineName)) {

        }

        TMObj<IHTMachine> htMachine = (TMObj<IHTMachine>) TMObj.lookupTMObj("rmi://localhost:" + port + "/" + machineName);

        Transaction.atomic(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'call'");
                IHTMachine iht = htMachine.openWrite();
                iht.insert(key, value);
            }
            
        })*/

        return false;
    }

}
