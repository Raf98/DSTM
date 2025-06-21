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
  private int minDelay; // = 128;// 64;//32;
  private int maxDelay; // = 4096;// 2048;//1024;
  int delay;
  int currentEnemyHashCode = Integer.MIN_VALUE;

  public Polite() {
    minDelay = 64;
    maxDelay = 4096;
    delay = minDelay;
  }

  public Polite(final int minDelay, final int maxDelay) {
    this.minDelay = minDelay;
    this.maxDelay = maxDelay;
    delay = minDelay;
  }

  public void resolve(Transaction me, ITransaction other) throws RemoteException {
    if (other.hashCode() != currentEnemyHashCode) {
      currentEnemyHashCode = other.hashCode();
      delay = minDelay;
    }

    if (delay >= maxDelay) {
      other.abort();
      delay = minDelay;
      return;
    }

    backOff(delay);
    delay *= 2;
  }

  @Override
  public int getFirstParam() {
    return minDelay;
  }

  @Override
  public void setFirstParam(int firstParam) {
    minDelay = firstParam;
  }
}
