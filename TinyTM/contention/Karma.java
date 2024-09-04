package TinyTM.contention;

import java.rmi.RemoteException;
import java.util.Random;

import TinyTM.ITransaction;
import TinyTM.Transaction;

public class Karma extends ContentionManager {
    Random random = new Random();
    ITransaction rival = null;
    int delay;//= 64;
    int attempts;// = 0;

    public Karma() {
        delay = 64;
        attempts = 0;
    }

    public Karma(int delay) {
        this.delay = delay;
        attempts = 0;
    }

    public void resolve(Transaction me, ITransaction other) throws RemoteException {
        if (rival != null) {
            if (other.hashCode() != rival.hashCode()) {
                rival = other;
            }
        }

        // System.out.println("OTHER: " + other.getPriority());
        // System.out.println("ME: " + me.getPriority());
        // System.out.println(attempts);
        if (attempts <= other.getPriority() - me.getPriority()) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            ++attempts;
        } else {
            other.abort();
        }
    }
}
