package TinyTM.contention;

import java.rmi.RemoteException;

import TinyTM.ITransaction;
import TinyTM.Transaction;

// Greedy CM implementation based on Toward a Theory of Transactional Contention Managers (2005)
// by Guerraoui, Herlihy and Pochon 
public class Greedy extends ContentionManager {
    int delay;//= 64;

    public Greedy(int delay) {
        this.delay = delay;
    }

    public void resolve(Transaction attacking, ITransaction enemy) throws RemoteException {
        if(attacking.getTimestamp() < enemy.getTimestamp() || enemy.isWaiting()) {
            enemy.setWaiting(false);
            enemy.abort();
            attacking.setWaiting(false);
            return;
        }

        if (!enemy.isWaiting()) {
            attacking.setWaiting(true);
        }
        
        backOff(delay);
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