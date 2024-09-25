package TinyTM;

import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicLong;

public class GlobalClock implements IGlobalClock {
    static AtomicLong currentTime = new AtomicLong(0);

    public GlobalClock() {
        System.out.println("GLOBAL CLOCK OBJ: " + this.hashCode());
    }

    @Override
    public synchronized long getCurrentTime() throws RemoteException, Exception {
        //System.out.println("GLOBAL CLOCK OBJ: " + this.hashCode());
        //System.out.println("CURRENT TIME: " + currentTime.get());
        return currentTime.incrementAndGet();//System.currentTimeMillis(); //System.nanoTime();
    }
}
