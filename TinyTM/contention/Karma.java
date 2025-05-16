package TinyTM.contention;

import java.rmi.RemoteException;

import TinyTM.ITransaction;
import TinyTM.Transaction;

public class Karma extends ContentionManager {
    int delay;
    int attempts = 0;
    int currentEnemyHashCode = Integer.MIN_VALUE;

    public Karma() {
        delay = 64;
    }

    public Karma(int delay) {
        this.delay = delay;
    }

    public void resolve(Transaction me, ITransaction other) throws RemoteException {
        // System.out.println("OTHER: " + other.getPriority());
        // System.out.println("ME: " + me.getPriority());
        // System.out.println(attempts);
        if (other.hashCode() != currentEnemyHashCode) {
            currentEnemyHashCode = other.hashCode();
            attempts = 0;
        }

        if ((me.getPriority() > other.getPriority()) || 
            ((attempts + me.getPriority()) > other.getPriority())) {
            attempts = 0;
            other.abort();
            return;
        }

        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
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
