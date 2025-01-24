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

/**
 * Encapsulates transactional synchronization for obstruction-free objects.
 * @author Maurice Herlihy
 */
import TinyTM.*;
//import TinyTM.contention.ContentionManager;
import TinyTM.exceptions.AbortedException;
import TinyTM.exceptions.PanicException;

import java.util.concurrent.atomic.AtomicReference;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public class TMObjServer<T extends Copyable<T>> extends UnicastRemoteObject implements ITMObjServer<T> {

  protected Class<T> myClass;
  AtomicReference<Locator> start;

  @SuppressWarnings("unchecked")
  public TMObjServer(T init) throws RemoteException {
    // super(init);
    super();
    myClass = (Class<T>) init.getClass();
    start = new AtomicReference<Locator>(new Locator(init));
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static void rebind(String name, Copyable obj) throws Exception {

    // ITMObjServer stub = (ITMObjServer) UnicastRemoteObject.exportObject(new
    // TMObjServer(obj), 0);

    // Bind the remote object's stub in the registry
    Naming.rebind(name, new TMObjServer(obj));

  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public T openWriteRemote(ITransaction tx) throws RemoteException {

    Locator locator = start.get();
    if (locator.owner.hashCode() == tx.hashCode()) {
      tx.setEnemyAttempts(0);     // reset transaction's enemies attempts whenever when opening an object to write
      tx.setDefunct(false);           // if it performs any transaction-related operation, defunct should be reset
      return (T) locator.newVersion;
    }
    // System.out.println ("open write servidor");
    Locator newLocator = new Locator();
    newLocator.owner = new AtomicReference(tx);

    while (true) {
      Locator oldLocator = start.get();
      ITransaction writer = oldLocator.owner.get();   // this is the transaction that is writing or wrote last on the object

      tx.setEnemyAttempts(0);     // reset transaction's enemies attempts whenever when opening an object to write
      tx.setDefunct(false);           // if it performs any transaction-related operation, defunct should be reset

      switch (writer.getStatus()) {
        case COMMITTED:
          tx.setPriority(0);     
          // System.out.println("committed");
          newLocator.oldVersion = oldLocator.newVersion;
          break;
        case ABORTED:
          //tx.setEnemyAttempts(0);
          //tx.setDefunct(false);
          // System.out.println("Abort");
          newLocator.oldVersion = oldLocator.oldVersion;
          break;
        case ACTIVE: // tx.abort(); throw new AbortedException();
          // writer.abort();
          tx.setPriority(tx.getPriority() + 1);     // increments priority when opening an object to write
          tx.CMresolve(writer);                     // if writer is already working on the same object, call the CM
          continue;
        default:
          throw new PanicException("Unexpected transaction state: " + writer.getStatus());
      }
      // System.out.println("Antes remove:" + tx.sizeRS());
      T rse = (T) tx.removeRS((ITMObjServer) this);

      // System.out.println("Depois remove:" + tx.sizeRS());

      if (rse != null && rse.hashCode() != newLocator.oldVersion.hashCode()) {
        tx.abort();
        throw new AbortedException();
      }
      // if(rse!=null){System.out.println("sempre");}

      try {
        newLocator.newVersion = (T) myClass.getDeclaredConstructor().newInstance();
      } catch (Exception ex) {
        System.out.println("Aqui");
        throw new PanicException(ex);
      }
      newLocator.oldVersion.copyTo(newLocator.newVersion);

      if (start.compareAndSet(oldLocator, newLocator)) // {
      {
        if (rse == null) {
          if (!tx.validateReadSet()) {
            tx.abort();
            throw new AbortedException();
          }
        }
        return (T) newLocator.newVersion;
      }
    }

  }

  // public void close() throws RemoteException
  // {
  // start.get().owner = Transaction.COMMITTED;
  // }
  @SuppressWarnings("unchecked")
  public T openSequential() throws RemoteException {
    Locator locator = start.get();
    switch (locator.owner.get().getStatus()) {
      case COMMITTED:
        return (T) locator.newVersion;// (T) UnicastRemoteObject.exportObject(locator.newVersion, 0);
      case ABORTED: // System.out.println("Abort");
        return (T) locator.oldVersion;// (T) UnicastRemoteObject.exportObject(locator.oldVersion, 0);
      default:
        throw new PanicException("Active/Inactitive transaction conflict");
    }
  }

  @SuppressWarnings("unchecked")
  public T openReadRemote(ITransaction tx) throws RemoteException {
    Locator locator = start.get();

    tx.setEnemyAttempts(0);    // reset transaction's enemies attempts whenever when opening an object to read
    tx.setDefunct(false);

    if (locator.owner.get().hashCode() == tx.hashCode()) {
      return (T) locator.newVersion;
    }
    // locator = start.get();
    ITransaction writer = locator.owner.get();
    T version = null;
    T cv = null;

    switch (writer.getStatus()) {
      case COMMITTED:
        version = (T) locator.newVersion;
        if (writer != Transaction.COMMITTED) {
          locator.owner.compareAndSet(writer, Transaction.COMMITTED);
        }
        break;
      case ABORTED:
        version = (T) locator.oldVersion;
        if (writer != Transaction.ABORTED) {
          locator.owner.compareAndSet(writer, Transaction.ABORTED);
        }
        break;
      case ACTIVE:
        tx.setPriority(tx.getPriority() + 1);    // increments priority when opening an object to read
        version = (T) locator.oldVersion;
        break;
      default:
        throw new PanicException("Unexpected transaction state: " + writer.getStatus());
    }
    // System.out.println("open read chegou servidor:"+ tx.sizeRS());

    // System.out.println("open read chegou servidor tx:"+ Transaction.sizeRS());
    cv = (T) tx.getRS(this);
    // if (cv==null) {System.out.println("null");}
    if (!(cv == null)) {
      if (!(cv.hashCode() == version.hashCode())) {
        tx.abort();
        throw new AbortedException();
      } else {
        return version;
      }
    } else {
      tx.addRS(this, version);
    }

    if (!tx.validateReadSet()) {
      tx.abort();
      throw new AbortedException();
    }
    return version;

  }

  public boolean validateEntry(Object version) throws RemoteException {

    Locator loc = (Locator) this.start.get();
    // Transaction me = Transaction.getLocal();
    // if (loc.owner.equals(me)) {System.out.println("impossibre!");}
    if ((loc.owner.get().getStatus() == Transaction.Status.COMMITTED) &&
        (!(version.hashCode() == loc.newVersion.hashCode()))) {
      return false;
    }

    if ((loc.owner.get().getStatus() == Transaction.Status.ABORTED) &&
        (!(version.hashCode() == loc.oldVersion.hashCode()))) {
      return false;
    }

    if ((loc.owner.get().getStatus() == Transaction.Status.ACTIVE) &&
        (!(version.hashCode() == loc.oldVersion.hashCode()))) {
      return false;
    }

    return true;

  }

}
