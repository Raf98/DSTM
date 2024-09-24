package TinyTM;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IGlobalClock extends Remote, Serializable {
    public long getCurrentTime() throws RemoteException, Exception;
}
