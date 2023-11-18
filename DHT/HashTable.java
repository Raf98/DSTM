package DHT;

import java.rmi.Remote;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class HashTable {
    static final int SIZE = 7;
    private LinkedList<Integer>[] dataMap;

    public HashTable(){
        dataMap = new LinkedList[SIZE];
        for(int i = 0; i < SIZE; ++i) {
            dataMap[i] = new LinkedList<>();
        }
    }

    int hashFunction(int key){
        return key % SIZE;
    }

    void printTable() {
        for(int i = 0; i < SIZE; i++) {
            System.out.print("Index " + i + ": ");
            if(dataMap.length != 0) {
                System.out.print("Contains => ");
                LinkedList<Integer> values = dataMap[i];
                Iterator<Integer> valuesIter = values.iterator();
		        while(valuesIter.hasNext()){
                    System.out.print("{" + valuesIter.next() + "}");
                    if(valuesIter.hasNext()) System.out.print(", ");
		        }
                System.out.println();
            } else {
                System.out.println("Empty");
            }
        }
    }

    void set(int key, int value) {
            int index = hashFunction(key);
            dataMap[index].add(value);
    }
    
    /*int get(int key) {
        int index = hashFunction(key);
        
        return dataMap.get(index).peekFirst();
    }*/

    public static void main(String[] args) {
        HashTable ht = new HashTable();
        ht.set(1000, 78);
        ht.set(1005, 98);
        ht.set(1076, 99);
        ht.set(1008, 78);
        ht.set(1007, 8768);
        ht.printTable();
    }
}

class Node<T> {
    private T value;
    private Node<T> next;

    public Node (T value) {
        this.value = value;
        this.next = null;
    }

    public Node<T> getNext() {
        return next;
    }

    public void setNext(Node<T> next) {
        this.next = next;
    }
}

class MyLinkedList<T> {
    private Node<T> head;
    private Node<T> tail;
    private Integer size;

    public MyLinkedList () {
        head = null;
        tail = null;
        size = 0;
    }

    void append(T value) {
        Node<T> newNode = new Node<>(value);
        if (size == 0) {
            head = newNode;
            tail = newNode;
        } else {
            tail.setNext(newNode);
            tail = newNode;
        }
        ++size;
    }

    void deleteLast() {
        if (size == 0) return;
        Node<T> temp = head;
        if (size == 1) {
            head = null;
            tail = null;
        } else {
            Node<T> pre = head;
            while (temp.getNext() != null) {
                pre = temp;
                temp.setNext(temp.getNext());
            }
            tail = pre;
            tail.setNext(null);
        }
        --size;
    }
}