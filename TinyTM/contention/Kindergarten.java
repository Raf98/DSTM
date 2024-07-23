package TinyTM.contention;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import TinyTM.ITransaction;
import TinyTM.Transaction;
import TinyTM.exceptions.AbortedException;

public class Kindergarten extends ContentionManager {
    Random random = new Random();
    ITransaction rival = null;
    int delay;// = 64;
    int intervals;// = 8;
    boolean backedOff = false;
    List<Integer> hitList = new ArrayList<>();;

    public Kindergarten() {
        delay = 64;
        intervals = 8;
    }

    public Kindergarten(int delay, int intervals) {
        this.delay = delay;
        this.intervals = intervals;
    }

    public void resolve(Transaction me, ITransaction other) throws RemoteException {
        if (rival != null) {
            if (other.hashCode() != rival.hashCode()) {
                rival = other;
                backedOff = false;
            }
        }

        if (hitList.contains(other.hashCode())) {
            // hitList.remove(other.hashCode());
            other.abort();
        } else {
            if (backedOff) {
                me.abort();
                throw new AbortedException();
            }
            hitList.add(other.hashCode());
            try {
                Thread.sleep(intervals * delay);
                backedOff = true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
