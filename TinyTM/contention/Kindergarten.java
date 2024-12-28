package TinyTM.contention;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
//import java.util.HashSet;
//import java.util.Set;

import TinyTM.ITransaction;
import TinyTM.Transaction;
import TinyTM.exceptions.AbortedException;

public class Kindergarten extends ContentionManager {
    int delayInterval;// = 64;
    boolean backedOff = false;
    List<Integer> hitList = new ArrayList<>();
    //Set<Integer> hitSet = new HashSet<>();

    public Kindergarten() {
        delayInterval = 256;
    }

    // ELIMINATE INTERVALS, INTEGRATE IT TO DELAY (FIXED INTERVAL)
    public Kindergarten(int delay) {
        this.delayInterval = delay;
    }

    public void resolve(Transaction me, ITransaction other) throws RemoteException {
        if (backedOff) {
            //System.out.println("ATTACKING BACKED OFF! ABORT IT...");
            backedOff = false;
            me.abort();
            throw new AbortedException();
        }
        else if (hitList.contains(other.hashCode())) { //hitSet.contains(other.hashCode()
            /*System.out.println("HIT AGAIN! ABORT ENEMY...");
            System.out.println("ATTACKING TRANSACTION: " + me.hashCode());
            System.out.println("ENEMY TRANSACTION: " + other.hashCode());*/

            hitList.remove(hitList.indexOf(other.hashCode()));
            //hitSet.remove(other.hashCode());
            other.abort();
        } else {

            /*System.out.println("FIRST HIT! BACK OFF ATTACKING...");
            System.out.println("ATTACKING TRANSACTION: " + me.hashCode());
            System.out.println("ENEMY TRANSACTION: " + other.hashCode());*/
            
            hitList.add(other.hashCode());
            //hitSet.add(other.hashCode());
            try {
                Thread.sleep(delayInterval);
                backedOff = true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
