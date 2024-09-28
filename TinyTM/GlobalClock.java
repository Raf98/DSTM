package TinyTM;

import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicLong;

public class GlobalClock implements IGlobalClock {
    AtomicLong currentTime = new AtomicLong(0);

    @Override
    public long getCurrentTime() throws RemoteException, Exception {
        return currentTime.incrementAndGet();
    }
}
