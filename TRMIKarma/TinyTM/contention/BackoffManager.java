/*
 *
 * Initial code taken from:
 * From "The Art of Multiprocessor Programming",
 * by Maurice Herlihy and Nir Shavit.
 *
 * + Fixed Bugs
 * + Merged abstractions
 * + Added validation through a read set
 * + Added Distributed STM
 *
 * Universidade Federal de Pelotas 2022
 * 
 * This work is licensed under a Creative Commons Attribution-Share Alike 3.0 United States License.
 * http://i.creativecommons.org/l/by-sa/3.0/us/88x31.png
 */

package TinyTM.contention;

import TinyTM.Transaction;
import java.util.Random;
import TinyTM.*;
import java.rmi.*;

/**
 * Simple adaptive backoff contention manager.
 * @author Maurice Herlihy
 */
public class BackoffManager extends ContentionManager {
  private static final int MIN_DELAY = 16;//32;
  private static final int MAX_DELAY = 512;//1024;
  Random random = new Random();
  ITransaction rival = null;
  int delay = 1024;
  int attempts = 0;

  public void resolve(Transaction me, ITransaction other) throws RemoteException {
   if(rival!=null){
       if (other.hashCode() != rival.hashCode()) {
            rival = other;
      }
    }
    
    //System.out.println("OTHER: " + other.getPriority());
    //System.out.println("ME: " + me.getPriority());
    //System.out.println(attempts);
    if (attempts < other.getPriority() - me.getPriority()) {            // be patient
      try {
        System.out.println("ME CODE: " + me.hashCode() + " PRIORITY: " + me.getPriority());
        System.out.println("OTHER CODE: " + other.hashCode() + " PRIORITY: " + other.getPriority());
        Thread.sleep(delay);
      } catch (InterruptedException ex) {
        System.out.println("KARMA THREAD INTERRUPT: " + me.hashCode() + "; " + other.hashCode());
        Thread.currentThread().interrupt();
      }
      ++attempts;
    } else {                          // patience exhausted
      System.out.println("ABORTING OTHER: " + other.hashCode());
      other.abort();
    }
  }  
}
