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
package TinyTM.ofree;

import java.rmi.Naming;

/**
 * Encapsulates transactional synchronization for obstruction-free objects.
 * @author Maurice Herlihy
 */
import TinyTM.Copyable;
import TinyTM.Transaction;
import TinyTM.exceptions.AbortedException;
import TinyTM.exceptions.PanicException;

public class TMObj<T extends Copyable<T>> { // extends TinyTM.AtomicObject<T> {

  // T localRef = null;
  ITMObjServer server;

  TMObj(ITMObjServer server) {
    // super(null);
    // this.localRef = null;
    this.server = server;
  }

  public static TMObj lookupTMObj(String remoteName) throws Exception { // System.out.println("lookup cliente");
    ITMObjServer serverObj = (ITMObjServer) Naming.lookup(remoteName);
    return new TMObj(serverObj);
  }

  /*
   * private T openSequential() {
   * Locator locator = start.get();
   * switch (locator.owner.getStatus()) {
   * case COMMITTED:
   * return locator.newVersion;
   * case ABORTED: //System.out.println("Abort");
   * return locator.oldVersion;
   * default:
   * throw new PanicException("Active/Inactitive transaction conflict");
   * }
   * }
   */

  // public void close() throws RemoteException
  // {
  // server.close();
  // }

  @SuppressWarnings("unchecked")
  public T openRead() throws Exception {
    // System.out.println("openread cliente");
    Transaction me = Transaction.getLocal();
    switch (me.getStatus()) {
      case COMMITTED:
        me.priority.set(0);
        me.defunct.set(false);          // if it performs any transaction-related operation, defunct should be reset
        return (T) server.openSequential();
      case ABORTED:
        me.defunct.set(false);          // if it performs any transaction-related operation, defunct should be reset
        throw new AbortedException();
      case ACTIVE:
        // if (localRef != null)
        // return localRef;
        // System.out.println("openread cliente pedindo para abrir");
        // ITransaction stub = (ITransaction) UnicastRemoteObject.exportObject(me, 0);

        // the 2 lines below have been moved to TMObjServer's openReadRemote, since it has effect also
        // on locally handled TMObjs this way (DHT)
        //me.priority.incrementAndGet();  // increments priority when opening an object to read
        //me.defunct.set(false);          // if it performs any transaction-related operation, defunct should be reset
        T result = (T) server.openReadRemote(me);
        // localRef = result;
        return result;
      default:
        throw new PanicException("Unexpected transaction state: " + me.getStatus());
    }
  }

  @SuppressWarnings("unchecked")
  public T openWrite() throws Exception {
    // System.out.println("openwrite cliente");
    Transaction me = Transaction.getLocal();
    switch (me.getStatus()) {
      case COMMITTED:
        me.priority.set(0);
        me.defunct.set(false);          // if it performs any transaction-related operation, defunct should be reset
        return (T) server.openSequential();
      case ABORTED:
        me.defunct.set(false);
        throw new AbortedException();
      case ACTIVE:
        // if (localRef != null){
        // System.out.println("local ref diferetne de null");
        // return localRef;
        // }
        // System.out.println("openwrite cliente pedindo para abrir");
        // ITransaction stub = (ITransaction) UnicastRemoteObject.exportObject(me, 0);

        // the 2 lines below have been moved to TMObjServer's openWriteRemote, since it has effect also
        // on locally handled TMObjs this way (DHT)
        //me.priority.incrementAndGet(); // increments priority when opening an object to write
        //me.defunct.set(false);
        T result = (T) server.openWriteRemote(me);
        // localRef = result;
        return result;
      default:
        throw new PanicException("Unexpected transaction state: " + me.getStatus());
    }
  }

}
