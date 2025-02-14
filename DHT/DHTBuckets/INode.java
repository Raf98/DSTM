package DHT.DHTBuckets;

import java.rmi.RemoteException;

import TinyTM.Copyable;

public interface INode<T> extends Copyable<INode<T>> {
    public int getKey() throws RemoteException;

    public void setKey(int key) throws RemoteException;

    public T getValue() throws RemoteException;

    public void setValue(T value) throws RemoteException;

    public INode<T> getNext() throws RemoteException;

    public void setNext(INode<T> next) throws RemoteException;

    public INode<T> get(int key) throws Exception;

    public boolean contains(int key) throws Exception;

    public INode<T> insert(int key, T value) throws Exception;
}