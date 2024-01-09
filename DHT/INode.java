package DHT;

import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicReference;

import TinyTM.Copyable;
import TinyTM.ofree.TMObj;

interface INode<T> extends Copyable<INode<T>> {
    public int getKey();

    public void setKey(int key);

    public T getItem();

    public void setItem(T item);

    public TMObj<INode<T>> getNext();

    public void setNext(TMObj<INode<T>> next);

    public TMObj<INode<T>> get(int key) throws Exception;

    public boolean contains(int key) throws Exception;

    public TMObj<INode<T>> insert(int key, int value) throws Exception;
}
