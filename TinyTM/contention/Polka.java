package TinyTM.contention;

import java.rmi.RemoteException;
import java.util.Random;

import TinyTM.ITransaction;
import TinyTM.Transaction;

public class Polka extends ContentionManager {
    private int minDelay; // = 128;// 64;//32;
    private int maxDelay; // 2048;//1024;

    int delay;
    int attempts = 0;
    int currentEnemyHashCode = Integer.MIN_VALUE;

    public Polka() {
        minDelay = 128;
        maxDelay = 2048;
        delay = minDelay;
    }

    public Polka(int minDelay, int maxDelay) {
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        delay = minDelay;
    }

    public void resolve(Transaction me, ITransaction other) throws RemoteException {
        if (other.hashCode() != currentEnemyHashCode) {
            currentEnemyHashCode = other.hashCode();
            attempts = 0;
            delay = minDelay;
        }

        // System.out.println("OTHER: " + other.getPriority());
        // System.out.println("ME: " + me.getPriority());
        // System.out.println(attempts);
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

        if (delay < maxDelay) {
            delay *= 2;
        }
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
