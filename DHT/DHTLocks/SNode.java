package DHT.DHTLocks;

import java.io.Serializable;
import java.rmi.AccessException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Semaphore;

import TinyTM.ofree.TMObjServer;

public class SNode<T>  extends UnicastRemoteObject implements INode<T> {
    int key;
    T item;
    String name;
    INode<T> next;

    public SNode() throws RemoteException {}
    public SNode(int key, T item) throws RemoteException {
        this.key = key;
        this.item = item;
        this.next = null;
    }
    
    @Override
    public int getKey() {
        return key;
    }
    @Override
    public void setKey(int key) {
        this.key = key;
    }
    @Override
    public T getItem() {
        return item;
    }
    @Override
    public void setItem(T item) {
        this.item = item;
    }

    @Override
    public INode<T> getNext() { return next; }
    @Override
    public void setNext(INode<T> next) { this.next = next; }
    @Override
    public void copyTo(INode<T> target) throws RemoteException {
        ((INode<T>)target).setNext(next);
        ((INode<T>)target).setKey(key);
        ((INode<T>)target).setItem(item);
    }

    @Override
    public String toString() {
        return "{ KEY: " + getKey() + ", ITEM: " + getItem() /*+ ", NEXT: " + getNext()*/ + " }";
    }
}