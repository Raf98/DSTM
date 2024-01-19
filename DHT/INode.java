package DHT;

import java.io.Serializable;

public interface INode<T> extends Serializable {
    public int getKey();

    public void setKey(int key);

    public T getItem();

    public void setItem(T item);

    public String getName();

    public void setName(String name);

    public INode<T> getNext();

    public void setNext(INode<T> next);
}
