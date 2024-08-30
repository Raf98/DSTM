package DHT.DHTLocks;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class SNode<T>  extends UnicastRemoteObject implements INode<T> {
    int key;
    T value;
    String name;
    INode<T> next;

    public SNode() throws RemoteException {}
    public SNode(int key, T item) throws RemoteException {
        this.key = key;
        this.value = item;
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
    public T getValue() {
        return value;
    }
    @Override
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public INode<T> getNext() { return next; }
    @Override
    public void setNext(INode<T> next) { this.next = next; }
    @Override
    public void copyTo(INode<T> target) throws RemoteException {
        ((INode<T>)target).setNext(next);
        ((INode<T>)target).setKey(key);
        ((INode<T>)target).setValue(value);
    }

    @Override
    public String toString() {
        return "{ KEY: " + getKey() + ", ITEM: " + getValue() /*+ ", NEXT: " + getNext()*/ + " }";
    }
}