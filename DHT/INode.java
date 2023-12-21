package DHT;

import java.util.concurrent.atomic.AtomicReference;

import TinyTM.Copyable;

interface INode<T> extends Copyable<INode<T>> {
    public int getKey();

    public void setKey(int key);

    public T getItem();

    public void setItem(T item);

    public AtomicReference<Node<T>> getNext();

    public void setNext(AtomicReference<Node<T>> next);
}
