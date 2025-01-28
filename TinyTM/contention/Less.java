package TinyTM.contention;

import java.rmi.RemoteException;

import TinyTM.ITransaction;
import TinyTM.Transaction;
import TinyTM.exceptions.AbortedException;

public class Less extends ContentionManager {
    public void resolve(Transaction me, ITransaction other) throws RemoteException {

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
