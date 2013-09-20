package nachos.threads;

import nachos.machine.*;

public class MyTester {

    public static void selfTest() {
        TestPrioprityScheduler();
    }

    public static void TestPrioprityScheduler() {
        PriopritySchedulerVAR1();
        PriopritySchedulerVAR2();
    }

    /**
     *  VAR1: Create several(>2) threads, verify these threads can be run successfully.
     */
    public static void PriopritySchedulerVAR1() {

        Lib.debug(dbgFlag, "Enter MyTester.selfTest");
        System.out.print("MYTEST\n");

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
        KThread.selfTest();
        Communicator.selfTest();
        Condition2.selfTest();
        Alarm.selfTest();
        Semaphore.selfTest();
    }

    static private char dbgFlag = 't';
}
