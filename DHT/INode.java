package DHT;

import java.rmi.RemoteException;

import TinyTM.Copyable;
import TinyTM.ofree.TMObjServer;

public interface INode<T> extends Copyable<INode<T>> {
    public int getKey() throws RemoteException;

    public void setKey(int key) throws RemoteException;

    public T getValue() throws RemoteException;

    public void setValue(T value) throws RemoteException;

    public TMObjServer<INode<T>> getNext() throws RemoteException;

    public void setNext(TMObjServer<INode<T>> next) throws RemoteException;

    /*public TMObjServer<INode<T>> get(int key) throws Exception;

    public boolean contains(int key) throws Exception;

    public TMObjServer<INode<T>> insert(int machineId, int key, int value) throws Exception;*/
}
