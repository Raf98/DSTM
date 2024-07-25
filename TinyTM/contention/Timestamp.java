package TinyTM.contention;

import java.rmi.RemoteException;
import java.util.Random;

import TinyTM.ITransaction;
import TinyTM.Transaction;

public class Timestamp extends ContentionManager {
    Random random = new Random();
    ITransaction rival = null;
    int delay;// = 64;
    int attempts;// = 0;
    int intervals;// = 32;

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
        if (rival != null) {
            if (other.hashCode() != rival.hashCode()) {
                rival = other;
                attempts = 0;
            }
        }

        if (me.getTimestamp() < other.getTimestamp() || (attempts >= intervals && other.getDefunct())) {
            other.abort();
        } else {
            if (attempts >= intervals / 2) {
                other.setDefunct(true);
            }
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            ++attempts;
        }
    }
}
