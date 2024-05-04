package DHT;

import java.rmi.Remote;
import java.rmi.RemoteException;

import TinyTM.Copyable;
import TinyTM.ofree.TMObjServer;

public interface IHashTable extends Remote, Copyable<IHashTable> {
    public TMObjServer<INode<Integer>> get(int key) throws RemoteException, Exception;

    public boolean insert(int key, int value) throws RemoteException, Exception;
}
