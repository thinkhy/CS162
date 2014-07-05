package nachos.threads;

import nachos.machine.*;

import java.util.Random;

/******************************************************************************
 *
 * 01* CHANGE-ACTIVITY:
 *
 * $B4=PROJECT2 TASK4, 140611, THINKHY: Implement lottery scheduler 
 *
 *
 *
 *****************************************************************************/

public class MyTester {

    public static void selfTest() {
        // TestPrioprityScheduler();                              /*@B4D*/
         
        // Test Boating solution                                  /*@B4D*/
        // TestBoatingSolution();                                 /*@B4D*/
        TestLotteryScheduler();                                   /*@B4A*/
    }

    public static void TestBoatingSolution() {
        System.out.println("\n***  Enter TestBoatingSolution");
        Boat.selfTest();
        System.out.println("\n***  Leave TestBoatingSolution");
    }

    public static void TestPrioprityScheduler() {
        Lib.debug(dbgFlag, "Enter TestPrioprityScheduler");
        PriopritySchedulerVAR1();
        PriopritySchedulerVAR2();
        PriopritySchedulerVAR3();
        PriopritySchedulerVAR4();
        Lib.debug(dbgFlag, "Leave TestPrioprityScheduler");
    }

                                                            

    /**
     *  VAR1: Create several(>2) threads, verify these threads can be run successfully.
     */
    public static void PriopritySchedulerVAR1() {

        System.out.print("PriopritySchedulerVAR1\n");

        Runnable myrunnable1 = new Runnable() {
        public void run() { 
            int i = 0;
            while(i < 10) { 
                System.out.println("*** in while1 loop " + i + " ***");
                i++;
            } /*yield();*/ 
        }
        }; 

        KThread testThread;
        testThread = new KThread(myrunnable1);
        testThread.setName("child 1");

        testThread.fork();
        testThread.join();

        KThread testThread2;
        testThread2 = new KThread(myrunnable1);
        testThread2.setName("child 2");

        testThread2.fork();
        KThread.yield();
        testThread2.join();

        KThread t[] = new KThread[10];
        for (int i=0; i<10; i++) {
             t[i] = new KThread(myrunnable1);
             t[i].setName("Thread" + i).fork();
        }

        KThread.yield();
    }

    /**
     * VAR2: Create lots of threads with more locks and more complicated resource allocation
     */
    public static void PriopritySchedulerVAR2() {
        System.out.print("PriopritySchedulerVAR2\n");

        KThread.selfTest();
        Communicator.selfTest();
        Condition2.selfTest();
        // Alarm.selfTest();
        Semaphore.selfTest();
    }

    /**
     * VAR3: Create several(>2) threads, decrease or increase the priorities of these threads. 
     * Verify these threads can be run successfully.
     */
    public static void PriopritySchedulerVAR3() {
        System.out.print("PriopritySchedulerVAR3\n");

        Runnable myrunnable1 = new Runnable() {
            public void run() { 
                int i = 0;
                while(i < 10) { 
                    System.out.println("*** in while1 loop " + i + " ***");
                    i++;
                } /*yield();*/ 
            }
        }; 

        KThread testThread;
        testThread = new KThread(myrunnable1);
        testThread.setName("child 1");
        testThread.fork();
        ThreadedKernel.scheduler.setPriority(testThread, 2);

        KThread testThread2;
        testThread2 = new KThread(myrunnable1);
        testThread2.setName("child 2");
        ThreadedKernel.scheduler.setPriority(testThread2, 3);
        testThread2.fork();

        testThread.join();

        KThread t[] = new KThread[10];
        for (int i=0; i<10; i++) {
             t[i] = new KThread(myrunnable1);
             t[i].setName("Thread" + i).fork();

             ThreadedKernel.scheduler.setPriority(t[i], (i+1)%8);
        }

        Random rand = new Random();

        KThread t1[] = new KThread[10];
        for (int i=0; i<10; i++) {
             t1[i] = new KThread(myrunnable1);
             t1[i].setName("Thread" + i).fork();

             ThreadedKernel.scheduler.setPriority(t1[i], rand.nextInt(8));
        }

        KThread.yield();
    }

    // Runnalbe class for low priority thread
    private static class Runnable1 implements Runnable  {

        Runnable1(Lock lock, boolean isOpen) {
            this.lock = lock;
            this.isOpen = isOpen;
        }

        public void run() { 
            lock.acquire();
            this.isRun = true;
            System.out.print("Low thread has got the lock.\n");
            while (this.isOpen == false) {
                System.out.print("Low thread is blocked, please open the door.\n");
                KThread.currentThread().yield();
            }
            this.isOpen = false;
            System.out.print("Low thread released, close the door.\n");
            lock.release();
        }

        Lock lock;
        static public boolean isOpen = false;
        static public boolean isRun  = false;
    } 

    // Runnalbe class for high priority thread
    private static class Runnable2 implements Runnable  {

        Runnable2(Lock lock) {
            this.lock = lock;
        }

        public void run() { 
            while (Runnable1.isRun != true) {
                KThread.currentThread().yield();
            }

            Runnable1.isOpen = true;

            lock.acquire();
            while (Runnable1.isOpen == true) {
                System.out.print("High thread is blocked, please close the door.\n");
                KThread.currentThread().yield();
            }

            Runnable1.isOpen = true;
            System.out.print("High thread released, close the door.\n");
            lock.release();
        }

        Lock lock;
        static public boolean isOpen = false;
    } 

    // Runnalbe class for medium priority thread
    private static class Runnable3 implements Runnable  {
        Runnable3() {
        }

        public void run() { 
            while(Runnable1.isOpen == false) {
                System.out.print("Medium thread is blocked, please open the door.\n");
                KThread.currentThread().yield();
            }

            System.out.print("Medium thread released, looks good.\n");
        }
    }

    /**
     * VAR4: Create a scenario to hit the priority inverse problem.
     * Verify the highest thread is blocked by lower priority thread.
     */
    public static void PriopritySchedulerVAR4() {
        System.out.print("PriopritySchedulerVAR4\n");

        Lock lock = new Lock();

        // low priority thread closes the door
        KThread low = new KThread(new Runnable1(lock, false));
        low.fork();
        low.setName("low");
        ThreadedKernel.scheduler.setPriority(low, 1);
        KThread.currentThread().yield();

        // High priority thread "high" waits for low priority thread "low" because they use the same lock.
        
        // high priority thread opens the door
        KThread high = new KThread(new Runnable2(lock));
        high.fork();
        high.setName("high");
        ThreadedKernel.scheduler.setPriority(high, 7);

        // medium priority thread waits for closing the door
        KThread medium = new KThread(new Runnable3());
        medium.fork();
        medium.setName("medium");
        ThreadedKernel.scheduler.setPriority(medium, 6);

        KThread.currentThread().yield();
    }

    public static void TestLotteryScheduler() {                                /*@B4A*/
        Lib.debug(dbgFlag, "++MyTester Enter TestLotteryScheduler");           /*@B4A*/
        LotterySchedulerVAR1();                                                /*@B4A*/
        LotterySchedulerVAR2();                                                /*@B4A*/
        Lib.debug(dbgFlag, "++MyTester Leave TestLotteryScheduler");           /*@B4A*/
    }                                                                          /*@B4A*/
     
    public static void LotterySchedulerVAR1() {                                /*@B4A*/
        System.out.print("++MyTester LotterySchedulerVAR1\n");                 /*@B4A*/

        Runnable myrunnable1 = new Runnable() {                                /*@B4A*/
            public void run() {                                                /*@B4A*/
                int i = 0;                                                     /*@B4A*/
                while(i < 10) {                                                /*@B4A*/
                    System.out.println("*** in while1 loop " + i + " ***");    /*@B4A*/
                    i++;                                                       /*@B4A*/
                } /*yield();*/                                                 /*@B4A*/
            }                                                                  /*@B4A*/
        };                                                                     /*@B4A*/

        KThread testThread;                                                    /*@B4A*/
        testThread = new KThread(myrunnable1);                                 /*@B4A*/
        testThread.setName("child 1");                                         /*@B4A*/

        testThread.fork();                                                     /*@B4A*/
        testThread.join();                                                     /*@B4A*/

        KThread testThread2;                                                   /*@B4A*/
        testThread2 = new KThread(myrunnable1);                                /*@B4A*/
        testThread2.setName("child 2");                                        /*@B4A*/

        testThread2.fork();                                                    /*@B4A*/
        KThread.yield();                                                       /*@B4A*/
        testThread2.join();                                                    /*@B4A*/

        Random rand = new Random();                                            /*@B4A*/

        KThread t[] = new KThread[10];                                         /*@B4A*/
        for (int i=0; i<10; i++) {                                             /*@B4A*/
             t[i] = new KThread(myrunnable1);                                  /*@B4A*/
             // ThreadedKernel.scheduler.setPriority(testThread, Lib.random(10));
             //ThreadedKernel.scheduler.setPriority(testThread, (1+rand.nextInt(10)));
             ThreadedKernel.scheduler.setPriority(testThread, 3);              /*@B4A*/
             t[i].setName("Thread" + i).fork();                                /*@B4A*/
        }                                                                      /*@B4A*/

        KThread.yield();                                                       /*@B4A*/

        for (int i=0; i<10; i++) {                                             /*@B4A*/
            t[i].join();                                                       /*@B4A*/
        }                                                                      /*@B4A*/
    }                                                                          /*@B4A*/

    public static void LotterySchedulerVAR2() {                                /*@B4A*/
        System.out.print("LotterySchedulerVAR2\n");                          /*@B4A*/

        Lock lock = new Lock();                                                /*@B4A*/

        // low priority thread closes the door
        KThread low = new KThread(new Runnable1(lock, false));                 /*@B4A*/
        low.fork();                                                            /*@B4A*/
        low.setName("low");                                                    /*@B4A*/
        ThreadedKernel.scheduler.setPriority(low, 5);                          /*@B4A*/ 
        // KThread.currentThread().yield();                                       /*@B4A*/

        // High priority thread "high" waits for low priority thread "low" because they use the same lock.
        
        // high priority thread opens the door
        KThread high = new KThread(new Runnable2(lock));                       /*@B4A*/ 
        high.fork();                                                           /*@B4A*/
        high.setName("high");                                                  /*@B4A*/
        ThreadedKernel.scheduler.setPriority(high, 7);                         /*@B4A*/

        // medium priority thread waits for closing the door
        KThread medium = new KThread(new Runnable3());                         /*@B4A*/
        medium.fork();                                                         /*@B4A*/
        medium.setName("medium");                                              /*@B4A*/
        ThreadedKernel.scheduler.setPriority(medium, 6);                       /*@B4A*/

        // KThread.yield();                                                       /*@B4A*/
        low.join();                                                            /*@B4A*/
        medium.join();                                                         /*@B4A*/
        high.join();                                                           /*@B4A*/       

        // KThread.currentThread().yield();
    }

    static private char dbgFlag = 't';                                         /*@B4A*/
}

