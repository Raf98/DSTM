package DHT;

import java.rmi.RemoteException;

import TinyTM.Copyable;

public interface ILinkedList<T> extends Copyable<ILinkedList<T>> {
    public INode<T> getHead() throws RemoteException;

    public void setHead(INode<T> head) throws RemoteException;

    public String getName() throws RemoteException;

    public void setName(String name) throws RemoteException;

    public int getMachineId() throws RemoteException;

    public void setMachineId(int machineId) throws RemoteException;

    public INode<T> insert(int key, T value) throws RemoteException;

    public INode<T> get(int key) throws RemoteException;

    public boolean contains(int key) throws RemoteException;
}
