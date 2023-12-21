package DHT;

import java.util.concurrent.atomic.AtomicReference;

class SNode<T> implements INode<T> {
    T item;
    int key;
    AtomicReference<Node<T>> next;

    public SNode() {}
    public SNode(int key, T item) {
        this.key = key;
        this.item = item;
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
    public AtomicReference<Node<T>> getNext() { return next; }
    @Override
    public void setNext(AtomicReference<Node<T>> next) { this.next = next; }
    @Override
    public void copyTo(INode<T> target) {
        ((INode<T>)target).setNext(next);
        ((INode<T>)target).setKey(key);
        ((INode<T>)target).setItem(item);
    }
}