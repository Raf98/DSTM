// Universidade Federal de Pelotas 2022
// This work is licensed under a Creative Commons
package DSTMBenchmark;

import java.rmi.registry.*;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public class ServerApp {
	public void publiciseObjectsAndWait(int id, Remote[] objects) {
		try {
			// System.setProperty("java.rmi.server.hostname", "127.0.0.1");
			Registry reg = LocateRegistry.createRegistry(1666 + id);
			// System.out.println("objects length:" + objects.length);
			for (int i = 0; i < objects.length; i++) {
				reg.rebind("object" + i, objects[i]);
			}

			IDBarrier barrier = AppCoordinator.connectToBarrier("serverbarrier");// (IDBarrier)
																					// Naming.lookup("serverbarrier");
			barrier.await(); // waits for all servers to be up and running
			barrier.await(); // waits for clients to finish work
			UnicastRemoteObject.unexportObject(reg, true);
			System.exit(0);
		} catch (Exception e) {
			System.out.println("SERVER APP EXCEPTION:");
			//long jvmMemoryMB = Runtime.getRuntime().totalMemory() / (1024 * 1024);
			//System.out.println("JVM AVAILABLE MEMORY: " + jvmMemoryMB + " MB");
			System.out.println("Server ID: " + id + "; Server Port: " + (1666 + id));
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

}
