// Universidade Federal de Pelotas 2022
// This work is licensed under a Creative Commons
package TinyTM.contention;

import TinyTM.*;
import java.rmi.*;
import TinyTM.exceptions.*;

public class Passive extends ContentionManager {
  private static /* final */ int MAX_ABORTS;
  private int aborts;
  // private boolean abortEnemy = false;

  public Passive() {
    this.aborts = 0;
    MAX_ABORTS = 32;// 10;
  }

  public Passive(final int maxAborts) {
    this.aborts = 0;
    MAX_ABORTS = maxAborts;
  }

  public void resolve(Transaction me, ITransaction other) throws RemoteException {
    /*
     * if (aborts < MAX_ABORTS) {
     * aborts++;
     * } else {
     * aborts = 1;
     * abortEnemy = !abortEnemy;
     * }
     * 
     * if (!abortEnemy) {
     * other.abort();
     * } else {
     * me.abort();
     * throw new AbortedException();
     * }
     */

    if (aborts < MAX_ABORTS) {
      aborts++;
      me.abort();
      throw new AbortedException();
    } else {
      aborts = 0;
      other.abort();
    }

  }

  @Override
  public int getFirstParam() {
    return MAX_ABORTS;
  }

  @Override
  public void setFirstParam(int firstParam) {
    MAX_ABORTS = firstParam;
  }
}
