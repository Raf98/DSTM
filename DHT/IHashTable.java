package DHT;

import java.rmi.RemoteException;
import java.util.LinkedList;

import TinyTM.Copyable;

public interface IHashTable extends Copyable<IHashTable> {
    public LinkedList<Integer> get(int key) throws RemoteException;

    public boolean insert(int key, int value) throws RemoteException, Exception;
}
