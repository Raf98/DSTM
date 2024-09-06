// Universidade Federal de Pelotas 2022
// This work is licensed under a Creative Commons
package DSTMBenchmark;

import java.rmi.*;
import java.util.Random;

import DSTMBenchmark.GenericDSTM.OperationsShuffler;

import java.util.HashSet;

public class ClientApp {

        ChooseObjects cs = new MyChoiceOfObjects();

        public void executeClient(ChooseOP cop, SaveData saveData, ExecTransaction et, String args[]) throws Exception {
                IDBarrier barrier = AppCoordinator.connectToBarrier("barrier"); // (IDBarrier) Naming.lookup("barrier");
                barrier.await();
                //// App Begins
                int clientid = Integer.parseInt(args[0]);
                int servers = Integer.parseInt(args[1]);
                int objects = Integer.parseInt(args[2]);
                int writes = Integer.parseInt(args[3]);
                int transactions = Integer.parseInt(args[4]);
                int objectspertransaction = Integer.parseInt(args[5]);
                //System.out.println("ARGS LENGTH: " + args.length);
                int contentionManager = args.length > 6 ? Integer.parseInt(args[6]) : 0;

                // System.out.println("CONTENTION MANAGER: " + contentionManager);

                Random random = new Random();
                RObject[] robjects;
                int op;

                OperationsShuffler opsShuffler = new OperationsShuffler();
                Integer[] shuffledOps = opsShuffler.shuffledArray(transactions, writes);
                
                try {
                        for (int i = 0; i < transactions; i++) {
                                robjects = cs.chooseObjects(servers, objects, objectspertransaction, random);
                                op = shuffledOps[i]; //cop.chooseOP(writes, random);
                                et.execTransaction(robjects, op, contentionManager);
                        }

                        // App Ends
                        barrier.await();

                        saveData.saveData(clientid, et);

                        // waits for all cleints do save data
                        barrier.await();
                        // waits for all servers to process data
                        barrier.await();
                        // System.out.println("Acabei");
                } catch (Exception e) {
                        /*System.out.println("CLIENT APP EXCEPTION:");
                        // long jvmMemoryMB = Runtime.getRuntime().totalMemory()/( 1024 * 1024 );
                        // System.out.println("JVM AVAILABLE MEMORY: " + jvmMemoryMB + " MB");
                        System.out.println("Client ID: " + clientid + "; Client Port: " + (1666 + clientid));
                        System.out.println(e.getMessage());*/
                        //e.printStackTrace();
                }

        }

        public static double doSomeComputation() {
                double x = 0;
                for (int i = 0; i < 10000000 / 10000; i++) {
                        x = x + 1;
                        x = 3 * x + 4;
                        x = x + 2;
                }
                x = x + 1;
                return x;
        }
}

class MyChoiceOfObjects implements ChooseObjects {

        // Defines a random number of objects per server up until nObjectsServers,
        // limiting these objects within the maximum objects per transaction definition,
        // and then formats the object's remote name using the object's number and its
        // relative port number
        public RObject[] chooseObjects(int nServers, int nObjectsServers, int nObjects, Random random) {
                RObject[] objects = new RObject[nObjects];
                int server, obj;
                HashSet<Pair> set = new HashSet<>();
                set.add(new Pair(0, 0));
                set.add(new Pair(0, 0));
                // System.out.println("Size set: " + set.size());
                while (set.size() < nObjects) {
                        server = random.nextInt(nServers);
                        // System.out.println("server: "+server);
                        obj = random.nextInt(nObjectsServers);
                        // System.out.println("object"+obj);
                        set.add(new Pair(server, obj));
                }
                int i = 0;
                for (Pair p : set) {
                        objects[i] = new RObject("object" + p.object, 1666 + p.server);
                        i++;
                }
                return objects;
        }

}

class Pair {
        public int server;
        public int object;

        Pair(int f, int s) {
                server = f;
                object = s;
        }

        public boolean equals(Object o) {
                return ((Pair) o).server == this.server && ((Pair) o).object == this.object;
        }

        public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + server;
                result = prime * result + object;
                return result;
        }
}
