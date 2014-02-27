package nachos.threads;

import nachos.machine.*;

import java.util.*; // +hy+
/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {

           this.isWordReady = false;
           this.lock = new Lock();       // hy+

           this.speakerCond  = new Condition2(lock);  // hy+
           this.listenerCond = new Condition2(lock);  // hy+
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
        lock.acquire();           // hy+

        speaker++;                // hy+  
        // now speader acquires the lock 

        // System.out.print("Listener: " + listener + "\n");	

        // while no available listener or word is ready(but listener hasn't fetched it)
        while (isWordReady || listener == 0) {   // hy+
            speakerCond.sleep();  // hy+
        }                         // hy+ 
        // System.out.print("Speaker waken up \n");	

        // speaker says a word
        this.word = word;         // hy+ 

        // set flag that word is ready
        isWordReady = true;       // hy+  

        // wake up all listeners
        listenerCond.wakeAll();      // hy+  

        speaker--;                // hy+

        lock.release();           // hy+ 
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
        // listener acquires the lock
        lock.acquire();           // hy+  

        // increase the number of listener by one
        listener++;               // hy+

        // System.out.print("Speaker: " + speaker + "\n");	

        // at this point, don't know if any speaker is waiting, just try to wake up all the speakers.
        // if use speakerCond.wake() instead, it will hit exception at LinkedList.removeFirst because 
        // no element to be removed.

        // while word is not ready, listener goes to sleep
        while(isWordReady == false) {   // hy+  
            speakerCond.wakeAll();       // hy+  
            listenerCond.sleep();       // hy+  
        }                         // hy+

        // listener receives the word
        int word = this.word;     // hy+   

        // reset flag that word is invalid
        isWordReady = false;      // hy+  

        // decrease listener number 
        listener--;               // hy+

        lock.release();           // hy+

        return word;              // hy+ 
    }

    // Test code added by hy
    //
    private static class Speaker implements Runnable {
	Speaker(Communicator comm, int word) {
        this.comm = comm; 
        this.word = word;
	}
	
	public void run() {
        // System.out.print(KThread.currentThread().getName() 
        //        + " will speak " + this.word + "\n");	
        comm.speak(this.word);
	}

    private int word = 0;
    private Communicator comm; 
    }

    private static class Listener implements Runnable {
	Listener(Communicator comm) {
        this.comm = comm; 
	}
	
	public void run() {
        // System.out.print(KThread.currentThread().getName() 
        //          + " will listen \n");	

        int word = comm.listen();

        // System.out.print("Listen a word: " + word + " \n"); 
	}

    private Communicator comm; 
    }

    /**
     * Test if this module is working.
     */
    public static void selfTest() {

    System.out.print("Enter Communicator.selfTest\n");	

    System.out.print("\nVAR1: Test for one speaker, one listener, speaker waits for listener\n");	

    Communicator comm = new Communicator();
    KThread threadSpeaker =  new KThread(new Speaker(comm, 100));
    threadSpeaker.setName("Thread speaker").fork();

    KThread.yield();

    KThread threadListener = new KThread(new Listener(comm));
    threadListener.setName("Thread listner").fork();

    KThread.yield();

    threadListener.join();
    threadSpeaker.join();

    System.out.print("\nVAR2: Test for one speaker, one listener, listener waits for speaker\n");	
    Communicator comm1 = new Communicator();

    KThread threadListener1 = new KThread(new Listener(comm1));
    threadListener1.setName("Thread listner").fork();

    KThread.yield();

    KThread threadSpeaker1 =  new KThread(new Speaker(comm1, 100));
    threadSpeaker1.setName("Thread speaker").fork();

    KThread.yield();

    threadListener1.join();
    threadSpeaker1.join();


    System.out.print("\nVAR3: Test for one speaker, more listener, listener waits for speaker\n");	

    Communicator comm2 = new Communicator();

    KThread t[] = new KThread[10];
	for (int i = 0; i < 10; i++) {
         t[i] = new KThread(new Listener(comm2));
         t[i].setName("Listener Thread" + i).fork();
	}

    KThread.yield();

    KThread speakerThread2 = new KThread(new Speaker(comm2, 200));
    speakerThread2.setName("Thread speaker").fork();

    KThread.yield();
    t[0].join();
    speakerThread2.join();



    System.out.print("\nVAR4:Test for one speaker, more listener, speaker waits for listener \n");	

    Communicator comm3 = new Communicator();

    KThread speakerThread3 = new KThread(new Speaker(comm3, 300));
    speakerThread3.setName("Thread speaker").fork();

    KThread.yield();

    KThread t3[] = new KThread[10];
	for (int i = 0; i < 10; i++) {
         t3[i] = new KThread(new Listener(comm3));
         t3[i].setName("Listener Thread" + i).fork();
	}

    KThread.yield();
    t3[0].join();
    speakerThread3.join();

    System.out.print("\nVAR5: Test for one speaker, more listener, listeners waits for speaker, and then create more listeners \n");	
    Communicator comm31 = new Communicator();


    KThread t31[] = new KThread[10];
	for (int i = 0; i < 5; i++) {
         t31[i] = new KThread(new Listener(comm31));
         t31[i].setName("Listener Thread" + i).fork();
	}

    KThread.yield();

    KThread speakerThread31 = new KThread(new Speaker(comm31, 300));
    speakerThread31.setName("Thread speaker").fork();

    KThread.yield();

	for (int i = 6; i < 10; i++) {
         t31[i] = new KThread(new Listener(comm31));
         t31[i].setName("Listener Thread" + i).fork();
	}

    KThread.yield();
    t3[0].join();
    speakerThread3.join();



    System.out.print("\nVAR6: Test for more speaker, one listener, listener waits for speaker\n");	

    Communicator comm4 = new Communicator();

    KThread t4[] = new KThread[10];
	for (int i = 0; i < 10; i++) {
         t4[i] = new KThread(new Speaker(comm4, (i+1)*100));
         t4[i].setName("Speaker Thread" + i).fork();
	}

    KThread.yield();

    KThread listenerCond4 = new KThread(new Listener(comm4));
    listenerCond4.setName("Thread listener").fork();

    KThread.yield();
    t4[0].join();
    listenerCond4.join();

    System.out.print("\nVAR7: Test for more speaker, one listener, speaker waits for listener\n");	

    Communicator comm5 = new Communicator();

    KThread listenerCond5 = new KThread(new Listener(comm5));
    listenerCond5.setName("Thread listener").fork();

    KThread.yield();

    KThread t5[] = new KThread[10];
	for (int i = 0; i < 10; i++) {
         t5[i] = new KThread(new Speaker(comm5, (i+1)*100));
         t5[i].setName("Speaker Thread" + i).fork();
	}

    KThread.yield();
    t5[0].join();
    listenerCond5.join();

    System.out.print("\nVAR8: Test for more speaker, one listener, speaker waits for listener,  and then create more speakers\n");	

    Communicator comm51 = new Communicator();

    KThread t51[] = new KThread[10];
	for (int i = 0; i < 5; i++) {
         t51[i] = new KThread(new Speaker(comm51, (i+1)*100));
         t51[i].setName("Speaker Thread" + i).fork();
	}

    KThread.yield();

    KThread listenerCond51 = new KThread(new Listener(comm51));
    listenerCond51.setName("Thread listener").fork();

    KThread.yield();
	for (int i = 5; i < 10; i++) {
         t51[i] = new KThread(new Speaker(comm51, (i+1)*100));
         t51[i].setName("Speaker Thread" + i).fork();
	}
    KThread.yield();

    t51[0].join();
    listenerCond51.join();

    System.out.print("\nVAR9:  Test for more speakers, more listeners, listeners waits for speaker \n");	
    Communicator comm9 = new Communicator();

    KThread ts9[] = new KThread[10];
	for (int i = 0; i < 10; i++) {
         ts9[i] = new KThread(new Speaker(comm9, (i+1)*100));
         ts9[i].setName("Speaker Thread" + i).fork();
	}

    KThread.yield();

    KThread tl9[] = new KThread[10];
	for (int i = 0; i < 10; i++) {
         tl9[i] = new KThread(new Listener(comm9));
         tl9[i].setName("Listener Thread" + i).fork();
	}

    KThread.yield();

	for (int i = 0; i < 10; i++) {
        ts9[i].join();
        tl9[i].join();
    }

    System.out.print("\nVAR10:  Test for more speakers, more listeners, speaker waits for listeners \n");	
    Communicator comm10 = new Communicator();

    KThread tl10[] = new KThread[10];
	for (int i = 0; i < 10; i++) {
         tl10[i] = new KThread(new Listener(comm10));
         tl10[i].setName("Listener Thread" + i).fork();
	}

    KThread.yield();

    KThread ts10[] = new KThread[10];
	for (int i = 0; i < 10; i++) {
         ts10[i] = new KThread(new Speaker(comm10, (i+1)*100));
         ts10[i].setName("Speaker Thread" + i).fork();
	}

    KThread.yield();

	for (int i = 0; i < 10; i++) {
        ts10[i].join();
        tl10[i].join();
    }

    System.out.print("\nVAR11:  Test for more speakers, more listeners, speaker waits for listeners \n");	
    Communicator comm11 = new Communicator();

    int num  = 80;
    ArrayList<KThread> t11 = new ArrayList<KThread>();

	for (int i = 0; i < num; i++) {
         KThread tmp = new KThread(new Speaker(comm11, (i+1)*100));
         tmp.setName("Speaker Thread" + i);

         t11.add(tmp);
	}

	for (int i = 0; i < num; i++) {
         KThread tmp = new KThread(new Listener(comm11));
         tmp.setName("Listener Thread" + i);

         t11.add(tmp);
	}

    Collections.shuffle(t11);

	for (int i = 0; i < num * 2; i++) {
         t11.get(i).fork();
	}

    KThread.yield();

	for (int i = 0; i < num * 2; i++) {
        t11.get(i).join();
    }

    System.out.print("\nTest for one speaker, more listener, speaker waits for listener \n");	
    System.out.print("\nTest for more speaker, one listener, speaker waits for listener \n");	

    System.out.print("Leave Communicator.selfTest\n");	

    }

    private int listener = 0;             // hy+
    private int speaker  = 0;             // hy+
    private int word = 0;                 // hy+ 
    private boolean isWordReady;  // hy+ 

    private Lock lock;                    // hy+
    private Condition2 speakerCond;       // hy+
    private Condition2 listenerCond;      // hy+
}


