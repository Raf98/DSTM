package DHT.DHTBuckets;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class SNode<T>  extends UnicastRemoteObject implements INode<T> {
    int key;
    T value;
    String name;
    INode<T> next;

    public SNode() throws RemoteException {}
    public SNode(int key, T value) throws RemoteException {
        this.key = key;
        this.value = value;
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
        return "{ KEY: " + getKey() + ", VALUE: " + getValue() /*+ ", NEXT: " + getNext()*/ + " }";
    }

    @Override
    public boolean contains(int key) throws Exception {
        if (this.getNext() == null) {
            return false;
        }

        for (INode<T> tempNode = this.next; tempNode != null; tempNode = tempNode.getNext()) {
            if (tempNode.getKey() == key) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public INode<T> insert(int key, T value) throws Exception {
        /*if (this.contains(key)) {
            return this.get(key);
        }*/

        /*System.out.println(getKey());
        System.out.println(getItem());
        System.out.println("NAME: " + getName());
        System.out.println(getNext());*/

        SNode<T> newNode = new SNode(key, value);
        //System.out.println(name);

        /*Integer port = 1700 + machineId;
        Registry registry = LocateRegistry.getRegistry(port);
        Remote newNodeRemote = new TMObjServer<>(newNode);
        registry.rebind(newNodeName, newNodeRemote);

        TMObjServer<INode<T>> newTmObjServerNode =  TMObjServer.lookupTMObjServer("rmi://localhost:" + port + "/" + newNodeName);*/
         

        if (this.next == null) {
            //System.out.println("FIRST INSERT: " + newNode.toString());
            this.setNext(newNode);
            //System.out.println("FIRST:" + newNode.toString());
        } else {
            for (INode<T> tempNode = this.next; ; tempNode = tempNode.getNext()) {    
                //System.out.println("CURRENT NODE:" + tempNode.toString());

                if (tempNode.getKey() == key) {
                    //System.out.printf("KEY %d; UPDATING VALUE FROM %d TO %d!\n", key, (int) tempNode.getValue(), (int) value);
                    tempNode.setValue(value);
                    break;
                } else if (tempNode.getNext() == null) {
                    /*System.out.printf("KEY %d; INDEX %d; SERVER %s: INSERTING %d!\n", 
                                        keys[i], keys[i] % numberHTEntries, addressName, values[i]);*/
                    tempNode.setNext(newNode);
                    break;
                }
            }
        }

        return newNode;
    }
    @Override
    public INode<T> get(int key) throws Exception {

        for (INode<T> tempNode = this.next; tempNode != null; tempNode = tempNode.getNext()) {
            if (tempNode.getKey() == key) {
                return tempNode;
            }
        }

        return null;
    }
}