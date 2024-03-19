package DHT;

import java.io.Serializable;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import TinyTM.Transaction;
import TinyTM.ofree.ITMObjServer;
import TinyTM.ofree.TMObj;
import TinyTM.ofree.TMObjServer;

public class SNode<T>  extends UnicastRemoteObject implements INode<T> {
    int key;
    T item;
    String name;
    TMObjServer<INode<T>> next;

    public SNode() throws RemoteException {}
    public SNode(int key, T item, String name) throws RemoteException {
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
    public TMObjServer<INode<T>> getNext() { return next; }
    @Override
    public void setNext(TMObjServer<INode<T>> next) { this.next = next; }
    @Override
    public void copyTo(INode<T> target) throws RemoteException {
        ((INode<T>)target).setNext(next);
        ((INode<T>)target).setKey(key);
        ((INode<T>)target).setItem(item);
        ((INode<T>)target).setName(name);
    }

    @Override
    public String toString() {
        return "{ KEY: " + getKey() + ", ITEM: " + getItem() + ", NAME: " + getName() + ", NEXT: " + getNext() + " }";
    }

    @Override
    public boolean contains(int key) throws Exception {
        if (this.getNext() == null) {
            return false;
        }

        for (TMObjServer<INode<T>> tmObjNode = this.next; tmObjNode != null; ) {
            INode<T> node;
                node = tmObjNode.openReadRemote(Transaction.getLocal());
                if (node.getKey() == key) {
                    return true;
                }

                tmObjNode = node.getNext();
        }

        return false;
    }

    @Override
    public TMObjServer<INode<T>> insert(int machineId, int key, int value) throws Exception {
        if (this.contains(key)) {
            return this.get(key);
        }

        /*System.out.println(getKey());
        System.out.println(getItem());
        System.out.println("NAME: " + getName());
        System.out.println(getNext());*/

        String newNodeName =  this.name + "_key" + key;
        System.out.println("NEW NODE NAME: " + newNodeName);
        SNode<T> newNode = new SNode(key, value, newNodeName);
        //System.out.println(name);

        Integer port = 1700 + machineId;
        Registry registry = LocateRegistry.getRegistry(port);
        Remote newNodeRemote = new TMObjServer<>(newNode);
        registry.rebind(newNodeName, newNodeRemote);

        //TMObj<INode<T>> newTmObjNode = TMObj.lookupTMObj("rmi://localhost:" + port + "/" + newNodeName);
        TMObjServer<INode<T>> newTmObjServerNode = (TMObjServer) Naming.lookup("rmi://localhost:" + port + "/" + newNodeName);

        System.out.println("LOOKED UP");

        if (this.next == null) {
            System.out.println("FIRST INSERT");
            this.setNext(newTmObjServerNode);
            //System.out.println("FIRST:" + newNode.toString());
        } else {
            for (TMObjServer<INode<T>> tmObjNode = this.next; ;) {
                INode<T> node;
                node = tmObjNode.openReadRemote(Transaction.getLocal());
    
                System.out.println("CURRENT NODE:" + node.toString());
                System.out.println("CURRENT NODE:" + node.getName());
                if (node.getNext() == null) {
                    System.out.println("NEXT INSERT");
                    node = tmObjNode.openWriteRemote(Transaction.getLocal());
                    node.setNext(newTmObjServerNode);
                    break;
                }
                tmObjNode = node.getNext();
            }
        }

        return newTmObjServerNode;
    }
    @Override
    public TMObjServer<INode<T>> get(int key) throws Exception {

        for (TMObjServer<INode<T>> tmObjNode = this.next; tmObjNode != null; ) {
            INode<T> node;
                node = tmObjNode.openReadRemote(Transaction.getLocal());
                if (node.getKey() == key) {
                    return tmObjNode;
                }

                tmObjNode = node.getNext();
        }

        return null;
    }
}