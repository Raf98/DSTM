package DHT.DHTLocks;

import java.rmi.RemoteException;

import TinyTM.Copyable;

public interface INode<T> extends Copyable<INode<T>> {
    public int getKey() throws RemoteException;

    public void setKey(int key) throws RemoteException;

    public T getItem() throws RemoteException;

    public void setItem(T item) throws RemoteException;

    public INode<T> getNext() throws RemoteException;

    public void setNext(INode<T> next) throws RemoteException;
}
