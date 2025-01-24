package TinyTM.contention;

import java.rmi.RemoteException;

import TinyTM.ITransaction;
import TinyTM.Transaction;

public class Aggressive extends ContentionManager {

    @Override
    public void resolve(Transaction me, ITransaction other) throws RemoteException {
        // if the current attacking transaction is conflicting with an enemy that aborted it previosly, then
        // it should be aborted again, to enforce the agressive policy from the previous transaction abort and, thus,
        // prevent a livelock loop with it
        // it should also check if the enemy is defunct and, if so, abort it
        // this could be done by a max attempts parameter, that controls the number of times 
        // an enemy attempted to complete itself after conflicting and, if it passes this limit,
        // the enemy is set to defunct, so that it can be aborted, since both its attempts and
        // defunct flag would be reset whenever it opens a new transactional object
       // if (!other.getConflictList().contains(me.hashCode()) || other.getDefunct()) {
            other.abort();
        //    me.getConflictList().add(other.hashCode());
        //} else {
            /*other.setAttempts(other.getAttempts() + 1);
            if (other.getAttempts() > attempts) {
                other.setDefunct(true);
            }*/
            //me.abort();
        //}
    }

    @Override
    public int getFirstParam() {
        return 0;
    }

    @Override
    public void setFirstParam(int firstParam) {};

}
