package TinyTM.contention;

import java.rmi.RemoteException;

import TinyTM.ITransaction;
import TinyTM.Transaction;

public class Aggressive extends ContentionManager {

    @Override
    public void resolve(Transaction me, ITransaction other) throws RemoteException {
        other.abort();
    }

    @Override
    public int getFirstParam() {
        return 0;
    }

    @Override
    public void setFirstParam(int firstParam) {};

}
