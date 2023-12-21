package DHT;

import java.util.concurrent.atomic.AtomicReference;

import TinyTM.Copyable;

public interface ILinkedList<T, U extends INode<T>> extends Copyable<ILinkedList<T, U>> {
    public void add(T value);

    public U get(T value);

    public boolean contains(T value);
}
