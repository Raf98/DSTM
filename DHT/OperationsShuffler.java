package DHT;

import java.util.Random;

public class OperationsShuffler {
    public Integer[] shuffledArray(int transactions, int writesPercentage) throws Exception {
        if (transactions < 50) {
            throw new Exception("Insufficient number of transactions!");
        }

        Integer[] shuffledArray = new Integer[transactions];
        Random random = new Random();

        int writes = (int) Math.ceil(transactions * writesPercentage / 100);
        int reads = transactions - writes;
        System.out.println("WRITES: " + writes);
        System.out.println("READS: " + reads);

        int i = 0;
        for (; i < transactions / 10; i++) {
            shuffledArray[i] = 0;
        }

        writes -= i;

        for (; i < transactions && reads > 0 && writes > 0; i++) {
            shuffledArray[i] = random.nextInt(2);

            if (shuffledArray[i] == 1) {
                --reads;
            } else {
                --writes;
            }
        }

        //System.out.println("WRITES: " + writes);
        //System.out.println("READS: " + reads);

        while (writes > 0) {
            shuffledArray[i] = 0;
            --writes;
            ++i;
        }

        while (reads > 0) {
            shuffledArray[i] = 1;
            --reads;
            ++i;
        }

        System.out.print("[" + shuffledArray[0]);
        for (i = 1; i < transactions; i++) {
            System.out.print("," + shuffledArray[i]);    
        }
        System.out.println("]");
        
        return shuffledArray;
    }
}
