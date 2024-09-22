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

import TinyTM.*;
import java.rmi.*;

/**
 * Simple adaptive backoff contention manager.
 * 
 * @author Maurice Herlihy
 */
public class Polite extends ContentionManager {
  private static /*final*/ int MIN_DELAY; //= 128;// 64;//32;
  private static /*final*/ int MAX_DELAY; //= 4096;// 2048;//1024;
  int delay;// = MIN_DELAY;

  public Polite(){
    MIN_DELAY = 128;
    MAX_DELAY = 4096;
    delay = MIN_DELAY;
  }

  public Polite(final int minDelay, final int maxDelay) {
    MIN_DELAY = minDelay;
    MAX_DELAY = maxDelay;
    delay = MIN_DELAY;
  }

  public void resolve(Transaction me, ITransaction other) throws RemoteException {
    if (delay < MAX_DELAY) { // be patient
      try {
        Thread.sleep(delay);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
      delay = 2 * delay;
    } else { // patience exhausted
      other.abort();
      delay = MIN_DELAY;
    }
  }
}
