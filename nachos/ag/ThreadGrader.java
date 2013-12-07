// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.ag;

import nachos.machine.*;
import nachos.security.*;
import nachos.threads.*;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Random;

/**
 * The default autograder. Loads the kernel, and then tests it using
 * <tt>Kernel.selfTest()</tt>.
 */
public class AutoGrader {
    /**
     * Allocate a new autograder.
     */
    public AutoGrader() {
    }

    /**
     * Start this autograder. Extract the <tt>-#</tt> arguments, call
     * <tt>init()</tt>, load and initialize the kernel, and call
     * <tt>run()</tt>.
     *
     * @param	privilege      	encapsulates privileged access to the Nachos
     * 				machine.
     */
    public void start(Privilege privilege) {
	Lib.assertTrue(this.privilege == null,
		   "start() called multiple times");
	this.privilege = privilege;

	String[] args = Machine.getCommandLineArguments();

	extractArguments(args);

	System.out.print(" grader");

	init();

	System.out.print("\n");	

	kernel =
	    (Kernel) Lib.constructObject(Config.getString("Kernel.kernel"));
	kernel.initialize(args);

	run();
    }

    private void extractArguments(String[] args) {
	String testArgsString = Config.getString("AutoGrader.testArgs");
	if (testArgsString == null) {
		testArgsString = "";
	}
	
	for (int i=0; i<args.length; ) {
	    String arg = args[i++];
	    if (arg.length() > 0 && arg.charAt(0) == '-') {
		if (arg.equals("-#")) {
		    Lib.assertTrue(i < args.length,
			       "-# switch missing argument");
		    testArgsString = args[i++];
		}
	    }
	}

	StringTokenizer st = new StringTokenizer(testArgsString, ",\n\t\f\r");

	while (st.hasMoreTokens()) {
	    StringTokenizer pair = new StringTokenizer(st.nextToken(), "=");

	    Lib.assertTrue(pair.hasMoreTokens(),
		       "test argument missing key");
	    String key = pair.nextToken();

	    Lib.assertTrue(pair.hasMoreTokens(),
		       "test argument missing value");
	    String value = pair.nextToken();

	    testArgs.put(key, value);
	}	
    }

    String getStringArgument(String key) {
	String value = (String) testArgs.get(key);
	Lib.assertTrue(value != null,
		   "getStringArgument(" + key + ") failed to find key");
	return value;
    }

    int getIntegerArgument(String key) {
	try {
	    return Integer.parseInt(getStringArgument(key));
	}
	catch (NumberFormatException e) {
	    Lib.assertNotReached("getIntegerArgument(" + key + ") failed: " +
				 "value is not an integer");
	    return 0;
	}
    }

    boolean getBooleanArgument(String key) {
	String value = getStringArgument(key);

	if (value.equals("1") || value.toLowerCase().equals("true")) {
	    return true;
	}
	else if (value.equals("0") || value.toLowerCase().equals("false")) {
	    return false;
	}
	else {
	    Lib.assertNotReached("getBooleanArgument(" + key + ") failed: " +
				 "value is not a boolean");
	    return false;
	}	
    }

    long getTime() {
	return privilege.stats.totalTicks;
    }

    void targetLevel(int targetLevel) {
	this.targetLevel = targetLevel;
    }

    void level(int level) {
	this.level++;	
	Lib.assertTrue(level == this.level,
		   "level() advanced more than one step: test jumped ahead");
	
	if (level == targetLevel)
	    done();
    }

    private int level = 0, targetLevel = 0;

    void done() {
	System.out.print("\nsuccess\n");
	privilege.exit(162);
    }

    private Hashtable<String, String> testArgs = 
        new Hashtable<String, String>();

    void init() {
    }
    
    void run() {
	kernel.selfTest();
	kernel.run();
	kernel.terminate();
    }

    Privilege privilege = null;
    Kernel kernel;

    /**
     * Notify the autograder that the specified thread is the idle thread.
     * <tt>KThread.createIdleThread()</tt> <i>must</i> call this method before
     * forking the idle thread.
     *
     * @param	idleThread	the idle thread.
     */
    public void setIdleThread(KThread idleThread) {
    }

    /**
     * Notify the autograder that the specified thread has moved to the ready
     * state. <tt>KThread.ready()</tt> <i>must</i> call this method before
     * returning.
     *
     * @param	thread	the thread that has been added to the ready set.
     */
    public void readyThread(KThread thread) {
    }

    /**
     * Notify the autograder that the specified thread is now running.
     * <tt>KThread.restoreState()</tt> <i>must</i> call this method before
     * returning.
     *
     * @param	thread	the thread that is now running.
     */
    public void runningThread(KThread thread) {
	privilege.tcb.associateThread(thread);
	currentThread = thread;
    }

    /**
     * Notify the autograder that the current thread has finished.
     * <tt>KThread.finish()</tt> <i>must</i> call this method before putting
     * the thread to sleep and scheduling its TCB to be destroyed.
     */
    public void finishingCurrentThread() {
	privilege.tcb.authorizeDestroy(currentThread);
    }

    /**
     * Notify the autograder that a timer interrupt occurred and was handled by
     * software if a timer interrupt handler was installed. Called by the
     * hardware timer.
     *
     * @param	privilege	proves the authenticity of this call.
     * @param	time	the actual time at which the timer interrupt was
     *			issued.
     */
    public void timerInterrupt(Privilege privilege, long time) {
	Lib.assertTrue(privilege == this.privilege,
		   "security violation");
    }

    /**
     * Notify the autograder that a user program executed a syscall
     * instruction.
     *
     * @param	privilege	proves the authenticity of this call.
     * @return	<tt>true</tt> if the kernel exception handler should be called.
     */
    public boolean exceptionHandler(Privilege privilege) {
	Lib.assertTrue(privilege == this.privilege,
		   "security violation");
	return true;
    }

    /**
     * Notify the autograder that <tt>Processor.run()</tt> was invoked. This
     * can be used to simulate user programs.
     *
     * @param	privilege	proves the authenticity of this call.
     */
    public void runProcessor(Privilege privilege) {
	Lib.assertTrue(privilege == this.privilege,
		   "security violation");
    }

    /**
     * Notify the autograder that a COFF loader is being constructed for the
     * specified file. The autograder can use this to provide its own COFF
     * loader, or return <tt>null</tt> to use the default loader.
     *
     * @param	file	the executable file being loaded.
     * @return	a loader to use in loading the file, or <tt>null</tt> to use
     *		the default.
     */
    public Coff createLoader(OpenFile file) {
	return null;
    }

    /**
     * Request permission to send a packet. The autograder can use this to drop
     * packets very selectively.
     *
     * @param	privilege	proves the authenticity of this call.
     * @return	<tt>true</tt> if the packet should be sent.
     */
    public boolean canSendPacket(Privilege privilege) {
	Lib.assertTrue(privilege == this.privilege,
		   "security violation");
	return true;
    }
    
    /**
     * Request permission to receive a packet. The autograder can use this to
     * drop packets very selectively.
     *
     * @param	privilege	proves the authenticity of this call.
     * @return	<tt>true</tt> if the packet should be delivered to the kernel.
     */
    public boolean canReceivePacket(Privilege privilege) {
	Lib.assertTrue(privilege == this.privilege,
		   "security violation");
	return true;
    }
    
    private KThread currentThread;
}


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
            while (true) {
                KThread.currentThread().yield();
                if (isOpen ) {
                    break;
                }
            }
            lock.release();
        }

        Lock lock;
        boolean isOpen = false;
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
                }
            }
        }; 

        KThread low = new KThread(myrunnable1);
        low.fork();
        low.setName("low");
        ThreadedKernel.scheduler.setPriority(low, 1);
        KThread.currentThread().yield();

        // High priority thread "high" waits for low priority thread "low" because they use the same lock.
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
