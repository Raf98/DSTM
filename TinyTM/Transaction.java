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

package TinyTM;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import TinyTM.contention.Aggressive;
import TinyTM.contention.CMEnum;
import TinyTM.contention.ContentionManager;
import TinyTM.contention.Karma;
import TinyTM.contention.Kindergarten;
import TinyTM.contention.Less;
import TinyTM.contention.Passive;
import TinyTM.contention.Polite;
import TinyTM.contention.Polka;
import TinyTM.contention.Timestamp;
import TinyTM.exceptions.AbortedException;
import TinyTM.exceptions.PanicException;
import TinyTM.ofree.ITMObjServer;
import TinyTM.ofree.ReadSet;

public class Transaction extends UnicastRemoteObject implements ITransaction {

  // public enum Status {ABORTED, ACTIVE, COMMITTED};
  static public final AtomicInteger commits = new AtomicInteger(0);
  static public final AtomicInteger aborts = new AtomicInteger(0);
  static public final AtomicInteger transactionId = new AtomicInteger(0);

  public AtomicInteger priority = new AtomicInteger(0);
  public AtomicLong timestamp;
  public AtomicBoolean defunct = new AtomicBoolean(false);
  public HashSet<Integer> conflictList = new HashSet<>();
  public AtomicInteger transactionAborts = new AtomicInteger(0);
  public static IGlobalClock globalClock;

  public static final Transaction COMMITTED = initCOMMITTED();
  public static final Transaction ABORTED = initABORTED();
  public ContentionManager cm;
  public static int maxAborts_minDelay_delay;
  public static int maxDelay_intervals;
  public static CMEnum cmName;
  public static int originalFirstParam;
  private final AtomicReference<Status> status;
  private ReadSet readset = new ReadSet();
  static ThreadLocal<Transaction> local = new ThreadLocal<Transaction>() {
    @Override
    protected Transaction initialValue() {
      return initCOMMITTED();
    }
  };

  public static Transaction initCOMMITTED() {
    Transaction t = null;
    try {
      t = new Transaction(Status.COMMITTED);
    } catch (RemoteException e) {
    }
    return t;
  }

  public static Transaction initABORTED() {
    Transaction t = null;
    try {
      t = new Transaction(Status.ABORTED);
    } catch (RemoteException e) {
    }
    return t;
  }

  public Transaction(/* int contentionManager */) throws RemoteException, Exception {
    super();
    status = new AtomicReference<Status>(Status.ACTIVE);

    cm = chooseCM();
    if (globalClock == null) {
      globalClock = (IGlobalClock) Naming.lookup("globalclock");
    }
    //System.out.println("TRANSACTION: " + this.hashCode() + " - GLOBAL CLOCK: " + globalClock.hashCode());
    timestamp = new AtomicLong(globalClock.getCurrentTime());
  }

  private Transaction(Transaction.Status myStatus) throws RemoteException {

    status = new AtomicReference<Status>(myStatus);
  }

  public Status getStatus() throws RemoteException {
    // System.out.println("Transaction getstatus client");
    return status.get();
  }

  public boolean commit() throws RemoteException {
    // System.out.println("Transaction commit client");
    return status.compareAndSet(Status.ACTIVE, Status.COMMITTED);
  }

  public boolean abort() throws RemoteException {
    // System.out.println("Transaction abort client!");
    return status.compareAndSet(Status.ACTIVE, Status.ABORTED);
  }

  public static Transaction getLocal() {
    return local.get();
  }

  public static void setLocal(Transaction transaction) {
    local.set(transaction);
  }

  /// readset

  public void addRS(ITMObjServer x, Object y) throws RemoteException {
    readset.add(x, y);
    // priority.incrementAndGet();
    // System.out.println("ADD RS: " + priority.get());
  }

  public int sizeRS() throws RemoteException {
    return readset.size();
  }

  public Object getRS(ITMObjServer x) throws RemoteException { // System.out.println("Transaction getRS client!");
    return readset.get(x);
  }

  public Object removeRS(ITMObjServer x) throws RemoteException { // System.out.println("removeRS cliente");
    return readset.remove(x);
  }

  public void clearRS() throws RemoteException {
    readset.clear();
  }

  public boolean validateReadSet() throws RemoteException {

    for (Map.Entry<ITMObjServer, Object> e : readset) {
      ITMObjServer server = e.getKey();
      Object version = e.getValue();
      if (!server.validateEntry(version)) {
        return false;
      }

    }
    return true;

  }

  public void CMresolve(ITransaction enemy) throws RemoteException {
    cm.resolve(this, enemy);
  }

  public static <T> T atomic(Callable<T> xaction) throws Exception {
    T result;
    Transaction me;
    Thread myThread = Thread.currentThread();
    long transactionTimestamp = -1;
    int transactionPriority = -1;
    HashSet<Integer> transactionConflictList = null;

    int transactionNum = transactionId.incrementAndGet();
    int transactionAborts = 0;
    int currentDelay = Transaction.maxAborts_minDelay_delay;

    while (!myThread.isInterrupted()) {
      me = new Transaction();
      Transaction.setLocal(me);

      if (transactionPriority != -1) {
        me.priority.set(transactionPriority);
      }
      if (transactionTimestamp != -1) {
        me.timestamp.set(transactionTimestamp);
      }
      me.transactionAborts.set(transactionAborts);
      /*if (transactionConflictList != null) {
        me.conflictList = transactionConflictList;
      }*/

      /*if (transactionAborts > 0 && transactionAborts % 8 == 0 && cmName.equals(CMEnum.Kindergarten)) {
        currentDelay *= 2;
        me.cm.setFirstParam(currentDelay);    
        System.out.printf("THREAD: %d; CURRENT DELAY INTERVAL: %d\n", myThread.hashCode(), me.cm.getFirstParam());    
      }*/

      //System.out.println("CURRENT TIMESTAMP: " + me.getTimestamp());
      //System.out.println("CURRENT PRIORITY: " + me.getPriority());

      try {
        result = xaction.call();
        if (me.validateReadSet() && me.commit()) {
          commits.getAndIncrement();
          //System.out.printf("THREAD: %d TRANSACTION %d; COMMITTED: %d\n", myThread.hashCode(), transactionNum, commits.get());

          return result;
        }
      } catch (AbortedException e) {
        transactionTimestamp = me.timestamp.get();
        transactionPriority = me.priority.get();
        //transactionConflictList = me.conflictList;
        transactionAborts = me.transactionAborts.incrementAndGet();
      } catch (InterruptedException e) {
        myThread.interrupt();
      } catch (Exception e) {
        e.printStackTrace();
        throw new PanicException(e);
      }
      aborts.getAndIncrement();
      //System.out.printf("THREAD: %d; TRANSACTION: %d; ABORTED: %d\n", myThread.hashCode(), transactionNum, transactionAborts);
      //System.out.println("ABORTED: " + aborts.get());
    }
    throw new InterruptedException();
  }

  @Override
  public int getPriority() throws RemoteException {
    // System.out.println("GET: " + priority.get());
    return priority.get();
  }

  @Override
  public void setPriority(int priority) throws RemoteException {
    this.priority.set(priority);
  }

  @Override
  public long getTimestamp() throws RemoteException {
    return timestamp.get();
  }

  @Override
  public boolean getDefunct() throws RemoteException {
    return defunct.get();
  }

  @Override
  public void setDefunct(boolean isDefunct) throws RemoteException {
    this.defunct.set(isDefunct);
  }

  @Override
  public int getAborts() throws RemoteException {
    return aborts.get();
  }

  @Override
  public int getCommits() throws RemoteException {
    return commits.get();
  }

  public static String getContentionManager() {
    return cmName.getId() + ": " + cmName.toString();
  }

  /*public static void setContentionManager(int contentionManager) throws RemoteException {
    cm = chooseCM(contentionManager);
  }*/

  public static void setContentionManager(int contentionManager, int maxAborts_minDelay_delay,
      int maxDelay_intervals) throws RemoteException {
      cmName = CMEnum.fromId(contentionManager);  
      Transaction.maxAborts_minDelay_delay = maxAborts_minDelay_delay;
      Transaction.maxDelay_intervals = maxDelay_intervals;
      //cm = chooseCM(contentionManager, maxAborts_minDelay_delay, maxDelay_intervals);
  }

  /*private ContentionManager chooseCM() {
    switch (cmName) {
      case Passive:
        cm = new Passive();
        break;
      case Polite:
        cm = new Polite();
        break;
      case Karma:
        cm = new Karma();
        break;
      case Polka:
        cm = new Polka();
        break;
      case Timestamp:
        cm = new Timestamp();
        break;
      case Kindergarten:
        cm = new Kindergarten();
        break;
      case Less:
        cm = new Less();
        break;
      case Aggressive:
        cm = new Aggressive();
        break;
      default:
        cm = new Passive();
        break;
    }
    return cm;
  }*/

  private ContentionManager chooseCM() {
    Transaction.originalFirstParam = maxAborts_minDelay_delay;
    switch (cmName) {
      case Passive:
        cm = new Passive(maxAborts_minDelay_delay);
        break;
      case Polite:
        cm = new Polite(maxAborts_minDelay_delay, maxDelay_intervals);
        break;
      case Karma:
        cm = new Karma(maxAborts_minDelay_delay);
        break;
      case Polka:
        cm = new Polka(maxAborts_minDelay_delay);
        break;
      case Timestamp:
        cm = new Timestamp(maxAborts_minDelay_delay, maxDelay_intervals);
        break;
      case Kindergarten:
        cm = new Kindergarten(maxAborts_minDelay_delay);
        break;
      case Less:
        cm = new Less();
        break;
      case Aggressive:
        cm = new Aggressive();
        break;
      default:
        cm = new Passive(maxAborts_minDelay_delay);
        break;
    }
    return cm;
  }

  @Override
  public HashSet<Integer> getConflictList() throws RemoteException {
    return conflictList;
  }

  @Override
  public int getTransactionAborts() throws RemoteException {
    return this.transactionAborts.get();
  }
}