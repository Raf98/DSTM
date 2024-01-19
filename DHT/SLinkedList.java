package DHT;

import java.rmi.RemoteException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public class SLinkedList<T> extends UnicastRemoteObject implements ILinkedList<T> {

    INode<T> head;
    int machineId;
    String name;

    public SLinkedList() throws RemoteException {
        head = null;
    }

    public SLinkedList(int machineId, String name, INode<T> headNode) throws RemoteException {
        this.machineId = machineId;
        this.name = name;
        head = headNode;
    }

    @Override
    public INode<T> getHead() throws RemoteException {
       return head;
    }

    @Override
    public void setHead(INode<T> head) throws RemoteException {
        this.head = head;
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    public void setName(String name) throws RemoteException {
        this.name = name;
    }

    @Override
    public int getMachineId() throws RemoteException {
        return machineId;
    }

    @Override
    public void setMachineId(int machineId) throws RemoteException {
        this.machineId = machineId;
    }

    @Override
    public void copyTo(ILinkedList<T> target) throws RemoteException {
        ((ILinkedList<T>)target).setHead(head);
        ((ILinkedList<T>)target).setName(name);
    }

    @Override
    public boolean contains(int key) throws RemoteException {
        if (this.head.getNext() == null) {
            return false;
        }

        for (INode<T> node = this.head.getNext(); node != null; node = node.getNext()) {
            if (node.getKey() == key) {
                System.out.println("CONTAINS: " + key);
                System.out.println(node.toString());
                return true;
            }
            System.out.println(node.toString());
        }

        return false;
    }

    @Override
    public INode<T> insert(int key, T value) throws RemoteException {
        if (this.contains(key)) {
            return null;
        }

        System.out.println(this.head.getKey());
        System.out.println(this.head.getItem());
        System.out.println("NAME: " + this.head.getName());
        System.out.println(this.head.getNext());

        String newNodeName =  this.name + "_key" + key;
        System.out.println(newNodeName);
        INode<T> newNode = new SNode(key, value, newNodeName);

        if (this.head.getNext() == null) {
            System.out.println("FIRST INSERT");
            this.head.setNext(newNode);
            System.out.println("FIRST:" + newNode.toString());
        } else {
            for (INode<T> node = this.head.getNext(); node != null; node = node.getNext()) {

    
                if (node.getNext() == null) {
                    System.out.println("NEXT INSERT");
                    node.setNext(newNode);
                    break;
                }

                System.out.println("NEXT:" + node.getNext().toString());

            }
        }

        return newNode;
    }
    @Override
    public INode<T> get(int key) throws RemoteException {

        for (INode<T> node = this.head.getNext(); node != null; node = node.getNext()) {
            if (node.getKey() == key) {
                return node;
            }
        }

        return null;
    }

}
