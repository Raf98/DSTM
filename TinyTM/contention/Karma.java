package TinyTM.contention;

import java.rmi.RemoteException;

import TinyTM.ITransaction;
import TinyTM.Transaction;

public class Karma extends ContentionManager {
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
        // System.out.println("OTHER: " + other.getPriority());
        // System.out.println("ME: " + me.getPriority());
        // System.out.println(attempts);
        if(me.getPriority() > other.getPriority()) {
            attempts = 0;
            other.abort();
        }
        else if (attempts <= other.getPriority() - me.getPriority()) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            ++attempts;
        } else {
            other.abort();
            attempts = 0;
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
