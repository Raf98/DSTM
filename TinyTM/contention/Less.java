package TinyTM.contention;

import java.rmi.RemoteException;

import TinyTM.ITransaction;
import TinyTM.Transaction;
import TinyTM.exceptions.AbortedException;

public class Less extends ContentionManager {
    int maxSelfAborts = 128;

    public Less(int maxSelfAborts) {
        this.maxSelfAborts = maxSelfAborts;
    }

    public void resolve(Transaction me, ITransaction other) throws RemoteException {
        if (me.getTransactionAborts() > maxSelfAborts) {
            me.setTransactionAborts(0);    
        }
    
        if (me.getTransactionAborts() <= other.getTransactionAborts()) {
            other.abort();
        } else {
            me.abort();
            throw new AbortedException();
        }
    }

    @Override
    public int getFirstParam() {
        return 0;
    }

    @Override
    public void setFirstParam(int firstParam) {};
}
