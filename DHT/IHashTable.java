package DHT;

import java.rmi.RemoteException;
import java.util.LinkedList;

import TinyTM.Copyable;
import TinyTM.ofree.TMObj;

public interface IHashTable extends Copyable<IHashTable> {
    public TMObj<INode<Integer>> get(int key) throws RemoteException;

    public boolean insert(int key, int value) throws RemoteException, Exception;
}
