package TinyTM.contention;

import java.rmi.RemoteException;

import TinyTM.ITransaction;
import TinyTM.Transaction;

public class Timestamp extends ContentionManager {
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
        //System.out.println("ME TIMESTAMP: " + me.getTimestamp() + ", HASH:" + me.hashCode());
        //System.out.println("OTHER TIMESTAMP: " + other.getTimestamp() + ", HASH:" + other.hashCode());
        //System.out.println(me.getTimestamp() == other.getTimestamp());

        if (me.getTimestamp() < other.getTimestamp() || (attempts >= intervals && other.getDefunct())) {
            attempts = 0;
            other.abort();
        } else {
            if (attempts >= intervals / 2 && !other.getDefunct()) {
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
