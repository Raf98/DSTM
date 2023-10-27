package DHT;

import java.rmi.Remote;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class HashTable {
    static final int SIZE = 7;
    private ArrayList<LinkedList<Integer>> dataMap;
    private HashMap<Integer, String> routingTable;

    public HashTable(){
        dataMap = new ArrayList<>(SIZE);
        for(int i = 0; i < SIZE; ++i) {
            dataMap.add(new LinkedList<>());
        }
    }

    int hashFunction(int key){
        return key % SIZE;
    }

    void printTable() {
        for(int i = 0; i < SIZE; i++) {
            System.out.print("Index " + i + ": ");
            if(dataMap.get(i).size() != 0) {
                System.out.print("Contains => ");
                LinkedList<Integer> values = dataMap.get(i);
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
            dataMap.get(index).add(value);
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
