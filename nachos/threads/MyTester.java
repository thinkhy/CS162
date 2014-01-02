package nachos.threads;

import nachos.machine.*;

import java.util.Random;

public class MyTester {

    public static void selfTest() {
        // TestPrioprityScheduler()
         
        // Test Boating solution
        TestBoatingSolution();
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
        Alarm.selfTest();
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

    private static class Runnable1 implements Runnable  {

        Runnable1(Lock lock, boolean isOpen) {
            this.lock = lock;
            this.isOpen = isOpen;
        }

        public void run() { 
            lock.acquire();
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
    } 

    private static class Runnable2 implements Runnable  {

        Runnable2(Lock lock) {
            this.lock = lock;
        }

        public void run() { 
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
     



    static private char dbgFlag = 't';
}


