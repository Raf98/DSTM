package DHT;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IHashTable extends Remote {
    public INode<Integer> get(int key) throws RemoteException, Exception;

    public INode<Integer>[] getMultiple(int[] keys) throws RemoteException, Exception;

    public boolean insert(int key, int value) throws RemoteException, Exception;

    public boolean[] insertMultiple(int[] keys, int[] values) throws RemoteException, Exception;

    public int getAborts() throws RemoteException;

    public int getCommits() throws RemoteException;
}
