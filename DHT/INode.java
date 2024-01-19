package DHT;

import java.rmi.RemoteException;

import TinyTM.Copyable;
import TinyTM.ofree.TMObj;

public interface INode<T> extends Copyable<INode<T>> {
    public int getKey() throws RemoteException;

    public void setKey(int key) throws RemoteException;

    public T getItem() throws RemoteException;

    public void setItem(T item) throws RemoteException;

    public String getName() throws RemoteException;

    public void setName(String name) throws RemoteException;

    public TMObj<INode<T>> getNext() throws RemoteException;

    public void setNext(TMObj<INode<T>> next) throws RemoteException;

    public TMObj<INode<T>> get(int key) throws Exception;

    public boolean contains(int key) throws Exception;

    public TMObj<INode<T>> insert(int machineId, int key, int value) throws Exception;
}
