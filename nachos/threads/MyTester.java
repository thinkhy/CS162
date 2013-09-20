package nachos.threads;

import nachos.machine.*;

import java.util.Random;

public class MyTester {

    public static void selfTest() {
        TestPrioprityScheduler();
    }

    public static void TestPrioprityScheduler() {
        Lib.debug(dbgFlag, "Enter TestPrioprityScheduler");
        // PriopritySchedulerVAR1();
        // PriopritySchedulerVAR2();
        // PriopritySchedulerVAR3();
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

        KThread testThread2;
        testThread2 = new KThread(myrunnable1);
        testThread2.setName("child 2");

        testThread2.fork();

        testThread.join();

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

        Runnable1(Lock lock) {
            this.lock = lock;
        }
        public void run() { 
            lock.acquire();
            KThread.currentThread().yield();
            lock.release();
        }

        Lock lock;
        } 
    /**
     * VAR4: Create a scenario to hit the priority inverse problem.
     * Verify the highest thread is blocked by lower priority thread.
     */
    public static void PriopritySchedulerVAR4() {
        System.out.print("PriopritySchedulerVAR4\n");

        Lock lock = new Lock();


        Runnable myrunnable1 = new Runnable1(lock);

        Runnable myrunnable2 = new Runnable() {
        public void run() { 
            while(true) {
                KThread.currentThread().yield();
    KThread.
            }
        }
        }; 


        KThread low = new KThread(myrunnable1);
        low.fork();
        low.setName("low");
        ThreadedKernel.scheduler.setPriority(low, 1);
        KThread.currentThread().yield();

        KThread high = new KThread(myrunnable1);
        high.fork();
        high.setName("high");
        ThreadedKernel.scheduler.setPriority(high, 7);


        KThread medium = new KThread(myrunnable2);
        medium.fork();
        medium.setName("medium");
        ThreadedKernel.scheduler.setPriority(medium, 6);
        KThread.currentThread().yield();

    }

    static private char dbgFlag = 't';
}
