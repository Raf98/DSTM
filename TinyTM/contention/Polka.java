package TinyTM.contention;

import java.rmi.RemoteException;
import java.util.Random;

import TinyTM.ITransaction;
import TinyTM.Transaction;

public class Polka extends ContentionManager {
    private static /*final*/ int MIN_DELAY; //= 128;// 64;//32;
    //private static final int MAX_DELAY = 2048;// 2048;//1024;
    Random random = new Random();
    int delay;// = 64;
    int attempts;// = 0;

    public Polka() {
        MIN_DELAY = 128;
        delay = MIN_DELAY;
        attempts = 0;
    }

    public Polka(final int minDelay) {
        MIN_DELAY = minDelay;
        delay = MIN_DELAY;
        attempts = 0;
    }

    public void resolve(Transaction me, ITransaction other) throws RemoteException {
        // System.out.println("OTHER: " + other.getPriority());
        // System.out.println("ME: " + me.getPriority());
        // System.out.println(attempts);
        if(me.getPriority() > other.getPriority()) {
            attempts = 0;
            delay = MIN_DELAY;
            other.abort();
        }
        else if (attempts <= other.getPriority() - me.getPriority()) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            ++attempts;
            // if(delay != MAX_DELAY){
            delay *= 2;
            // }
        } else {
            other.abort();
            delay = MIN_DELAY;
            attempts = 0;
        }
    }
}
