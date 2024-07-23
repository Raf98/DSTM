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

import TinyTM.exceptions.AbortedException;
import TinyTM.exceptions.PanicException;
import java.util.concurrent.Callable;
import java.util.Map;
import TinyTM.ofree.TMObjServer;
import TinyTM.ofree.ITMObjServer;
import java.util.concurrent.atomic.AtomicReference;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.*;
import TinyTM.ofree.ReadSet;
import TinyTM.ofree.TMObj;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import TinyTM.contention.*;

public class Transaction extends UnicastRemoteObject implements ITransaction {

  // public enum Status {ABORTED, ACTIVE, COMMITTED};
  static public final AtomicInteger commits = new AtomicInteger(0);
  static public final AtomicInteger aborts = new AtomicInteger(0);

  public AtomicInteger priority = new AtomicInteger(0);
  public AtomicLong timestamp;
  public AtomicBoolean defunct = new AtomicBoolean(false);

  public static final Transaction COMMITTED = initCOMMITTED();
  public static final Transaction ABORTED = initABORTED();
  public static ContentionManager cm;
  public static CMEnum cmName;
  private final AtomicReference<Status> status;
  private ReadSet readset = new ReadSet();
  static ThreadLocal<Transaction> local=new ThreadLocal<Transaction>(){@Override protected Transaction initialValue(){return initCOMMITTED();}};

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

  public Transaction(/*int contentionManager*/) throws RemoteException {
    super();
    status = new AtomicReference<Status>(Status.ACTIVE);

    //cm = chooseCM(contentionManager);
    timestamp = new AtomicLong(GlobalClock.getCurrentTime());
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

    while (!myThread.isInterrupted()) {
      me = new Transaction();
      Transaction.setLocal(me);
      try {
        result = xaction.call();
        if (me.validateReadSet() && me.commit()) {
          commits.getAndIncrement();
          System.out.println("TRANSACTION " + me.toString() +"; COMMITED: " + commits.get());

          return result;
        }
      } catch (AbortedException e) {
      } catch (InterruptedException e) {
        myThread.interrupt();
      } catch (Exception e) {
        e.printStackTrace();
        throw new PanicException(e);
      }
      aborts.getAndIncrement();
      System.out.println("TRANSACTION " + me.toString() +"; ABORTED: " + aborts.get());
    }
    throw new InterruptedException();
  }

  @Override
  public int getPriority() throws RemoteException {
    // System.out.println("GET: " + priority.get());
    return priority.get();
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
    return cmName.getId() + ": " +  cmName.toString();
  }

  public static void setContentionManager(int contentionManager) throws RemoteException{
    cm = chooseCM(contentionManager);
  }

  public static void setContentionManager(int contentionManager, int maxAborts_minDelay_delay, 
                                          int maxDelay_attempts_intervals, int intervals) throws RemoteException{
    cm = chooseCM(contentionManager, maxAborts_minDelay_delay, maxDelay_attempts_intervals, intervals);
  }

  private static ContentionManager chooseCM(int contentionManager) {
    cmName = CMEnum.fromId(contentionManager);
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
  }

  private static ContentionManager chooseCM(int contentionManager, int maxAborts_minDelay_delay, 
                                            int maxDelay_attempts_intervals, int intervals) {
    cmName = CMEnum.fromId(contentionManager);
    switch (cmName) {
      case Passive:
        cm = new Passive(maxAborts_minDelay_delay);
        break;
      case Polite:
        cm = new Polite(maxAborts_minDelay_delay, maxDelay_attempts_intervals);
        break;
      case Karma:
        cm = new Karma(maxAborts_minDelay_delay, maxDelay_attempts_intervals);
        break;
      case Polka:
        cm = new Polka(maxAborts_minDelay_delay, maxDelay_attempts_intervals);
        break;
      case Timestamp:
        cm = new Timestamp(maxAborts_minDelay_delay, maxDelay_attempts_intervals, intervals);
        break;
      case Kindergarten:
        cm = new Kindergarten(maxAborts_minDelay_delay, maxDelay_attempts_intervals);
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
}