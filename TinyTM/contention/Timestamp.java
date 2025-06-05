package TinyTM.contention;

import java.rmi.RemoteException;

import TinyTM.ITransaction;
import TinyTM.Transaction;

public class Timestamp extends ContentionManager {
    int delay;// = 64;
    int attempts;// = 0;
    int intervals;// = 32;
    int currentEnemyHashCode = Integer.MIN_VALUE;
    boolean wasSetDefunctBefore = false;

    public Timestamp() {
        delay = 64;
        attempts = 0;
        intervals = 32;
    }

    public Timestamp(int delay, int intervals) {
        this.delay = delay;
        attempts = 0;
        this.intervals = intervals;
    }

    public void resolve(Transaction me, ITransaction other) throws RemoteException {
        // System.out.println("ME TIMESTAMP: " + me.getTimestamp() + ", HASH:" +
        // me.hashCode());
        // System.out.println("OTHER TIMESTAMP: " + other.getTimestamp() + ", HASH:" +
        // other.hashCode());
        // System.out.println(me.getTimestamp() == other.getTimestamp());

        if (other.hashCode() != currentEnemyHashCode) {
            currentEnemyHashCode = other.hashCode();
            attempts = 0;
        }

        if (wasSetDefunctBefore && !other.getDefunct()) {
            attempts = 0;
            wasSetDefunctBefore = false;
        }

        if (me.getTimestamp() < other.getTimestamp() || (attempts >= intervals && other.getDefunct())) {
            attempts = 0;
            other.abort();
            return;
        } 
        
        
        if (attempts >= intervals / 2 && !other.getDefunct()) {
            other.setDefunct(true);
            wasSetDefunctBefore = true;
        }

        backOff(delay);
        ++attempts;
    }

    @Override
    public int getFirstParam() {
        return delay;
    }

    @Override
    public void setFirstParam(int firstParam) {
        delay = firstParam;
    }
}
