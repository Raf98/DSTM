package DHT;

import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicReference;

public class SLinkedList<T, U extends INode<T>> implements ILinkedList<T, U> {

    final U head;
    final U tail;

    public SLinkedList() {
        head = null;
        tail = null;
    }

    public void add(T newValue) {
        /*Node<T>[] preds = (Node<T>[]) new Node[MAX_HEIGHT];
        Node<T>[] succs = (Node<T>[]) new Node[MAX_HEIGHT];
        if (find(v, preds, succs) != -1) {
            return false;
        }
        Node<T> newNode = new Node<T>(topLevel + 1, v);
        for (int level = 0; level <= topLevel; level++) {
            newNode.getNext().set(level, succs[level]);
            preds[level].getNext().set(level, newNode);
        }
        return true;*/
    }

    @Override
    public void copyTo(ILinkedList<T, U> target) throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'copyTo'");
    }

    @Override
    public U get(T value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }

    @Override
    public boolean contains(T value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'contains'");
    }

}
