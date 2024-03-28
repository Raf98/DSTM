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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import TinyTM.*;
import TinyTM.exceptions.AbortedException;

import java.rmi.*;

/**
 * Simple adaptive backoff contention manager.
 * @author Maurice Herlihy
 */
public class BackoffManager extends ContentionManager {
  private static final int MIN_DELAY = 128;//64;//32;
  private static final int MAX_DELAY = 4096;//2048;//1024;
  Random random = new Random();
  ITransaction rival = null;
  int delay = 64;
  int intervals = 8;
  boolean backedOff = false;
  List<Integer> hitList;

  public BackoffManager(){
    hitList = new ArrayList<>();
  }

  public void resolve(Transaction me, ITransaction other) throws RemoteException {
   if(rival!=null){
       if (other.hashCode() != rival.hashCode()) {
            rival = other;
            backedOff = false;
      }
    }

    if (hitList.contains(other.hashCode())) {
      //hitList.remove(other.hashCode());
      other.abort();  
    } else {
      if (backedOff) {
        me.abort();
        throw new AbortedException();
      }
      hitList.add(other.hashCode());
      try {
        Thread.sleep(intervals*delay);
        backedOff = true;
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }  
}
