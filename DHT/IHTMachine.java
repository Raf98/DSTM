package DHT;

import java.rmi.RemoteException;
import java.util.LinkedList;

import TinyTM.Copyable;

public interface IHTMachine extends Copyable<IHTMachine> {
    public LinkedList<Integer> get(int key) throws RemoteException;

    public boolean insert(int key, int value) throws RemoteException, Exception;
}
