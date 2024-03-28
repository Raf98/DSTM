package TinyTM.contention;

import java.rmi.RemoteException;
import java.util.Random;

import TinyTM.ITransaction;
import TinyTM.Transaction;
import TinyTM.exceptions.AbortedException;

public class Less extends ContentionManager {
    Random random = new Random();
    ITransaction rival = null;
    int delay = 64;
    int attempts = 0;
    int intervals = 16;

    public void resolve(Transaction me, ITransaction other) throws RemoteException {
        if (rival != null) {
            if (other.hashCode() != rival.hashCode()) {
                rival = other;
                attempts = 0;
            }
        }

        if (me.getAborts() <= other.getAborts()) {
            other.abort();
        } else {
            me.abort();
            throw new AbortedException();
        }
    }
}
