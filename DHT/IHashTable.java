package DHT;

import java.rmi.Remote;
import java.rmi.RemoteException;

import TinyTM.Copyable;

public interface IHashTable extends Remote, Copyable<IHashTable> {
    public INode<Integer> get(int key) throws RemoteException, Exception;

    public boolean insert(int key, int value) throws RemoteException, Exception;
}
