// Universidade Federal de Pelotas 2022
// This work is licensed under a Creative Commons
package TinyTM.contention;

import TinyTM.Transaction;
import java.util.Random;
import TinyTM.*;
import java.rmi.*;
import TinyTM.exceptions.*;

public class Passive extends ContentionManager {
  private static /* final */ int MAX_ABORTS;
  private int aborts;

  public Passive() {
    this.aborts = 0;
    MAX_ABORTS = 10;
  }

  public Passive(final int maxAborts) {
    this.aborts = 0;
    MAX_ABORTS = maxAborts;
  }

  public void resolve(Transaction me, ITransaction other) throws RemoteException {
    if (aborts < MAX_ABORTS) {
      aborts++;
      me.abort();
      throw new AbortedException();
    } else {
      other.abort();
    }
  }
}
