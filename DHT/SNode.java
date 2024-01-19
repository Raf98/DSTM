package DHT;

import java.rmi.AccessException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import TinyTM.ofree.TMObj;
import TinyTM.ofree.TMObjServer;

public class SNode<T> implements INode<T> {
    int key;
    T item;
    String name;
    TMObj<INode<T>> next;

    public SNode() {}
    public SNode(int key, T item, String name) {
        this.key = key;
        this.item = item;
        this.name = name;
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
    public String getName() {
        return name;
    }
    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public TMObj<INode<T>> getNext() { return next; }
    @Override
    public void setNext(TMObj<INode<T>> next) { this.next = next; }
    @Override
    public void copyTo(INode<T> target) {
        ((INode<T>)target).setNext(next);
        ((INode<T>)target).setKey(key);
        ((INode<T>)target).setItem(item);
    }
    @Override
    public boolean contains(int key) throws Exception {
        if (this.getNext() == null) {
            return false;
        }

        for (TMObj<INode<T>> tmObjNode = this.next; tmObjNode != null; ) {
            INode<T> node;
                node = tmObjNode.openRead();
                if (node.getKey() == key) {
                    return true;
                }

                tmObjNode = node.getNext();
        }

        return false;
    }

    @Override
    public TMObj<INode<T>> insert(int key, int value) throws Exception {
        if (this.contains(key)) {
            return this.get(key);
        }

        System.out.println(getKey());
        System.out.println(getItem());
        System.out.println(getName());
        String newNodeName = this.name + "_key" + key;
        SNode<T> newNode = new SNode(-1, 0, newNodeName);
        System.out.println(newNodeName);
        System.out.println(name);
        Integer id = Integer.parseInt(name.split("ht", 3)[1]);
        Integer port = 1700 + id;
        Registry registry = LocateRegistry.createRegistry(port);
        Remote newNodeRemote = new TMObjServer(newNode);
        registry.rebind("object" + id, newNodeRemote);

        TMObj<INode<T>> newTmObjNode = TMObj.lookupTMObj("rmi://localhost:" + port + "/" + newNodeName);
         
        if (this.key == -1) {
            this.setNext(newTmObjNode);
        }

        for (TMObj<INode<T>> tmObjNode = this.next; ;) {
            INode<T> node;
            node = tmObjNode.openRead();

            if (node.getNext() == null) {
                node = tmObjNode.openWrite();
                node.setNext(newTmObjNode);
                break;
            }

            tmObjNode = node.getNext();
        }

        return newTmObjNode;
    }
    @Override
    public TMObj<INode<T>> get(int key) throws Exception {

        for (TMObj<INode<T>> tmObjNode = this.next; tmObjNode != null; ) {
            INode<T> node;
                node = tmObjNode.openRead();
                if (node.getKey() == key) {
                    return tmObjNode;
                }

                tmObjNode = node.getNext();
        }

        return null;
    }
}