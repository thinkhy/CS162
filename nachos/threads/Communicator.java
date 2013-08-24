package nachos.threads;

import nachos.machine.*;

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

        System.out.print("Listener: " + listener + "\n");	
        // no available listener, speaker goes to sleep
        while (isWordReady || listener == 0) {   // hy+
            speakerCond.sleep();  // hy+
        }                         // hy+ 
        System.out.print("Speaker waken up \n");	

        // speaker says a word
        this.word = word;         // hy+ 

        // set flag that word is ready
        isWordReady = true;       // hy+  

        // wake up a listener
        listenerCond.wake();      // hy+  

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

        System.out.print("Speaker: " + speaker + "\n");	

        // inform speaker I have come 
        if (speaker > 0) {
            speakerCond.wake();       // hy+  

            // and decrease speaker number to prevent other listners from passing this condition
            
            // TODO: to prevent following idle listners from passing this condition and rewaking speaker
            speaker--;
        }

        System.out.print("isWordReady: " + isWordReady + "\n");	

        // while word is not ready, listener goes to sleep
        while(isWordReady == false) {   // hy+  
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
    private static class Speaker implements Runnable {
	Speaker(Communicator comm, int word) {
        this.comm = comm; 
        this.word = word;
	}
	
	public void run() {
        System.out.print(KThread.currentThread().getName() 
                + "will speak " + this.word + "\n");	
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
        System.out.print(KThread.currentThread().getName() 
                + " will listen \n");	

        int word = comm.listen();

        System.out.print("Listen a word: " + word + " \n"); 
	}

    private Communicator comm; 
    }

    /**
     * Test if this module is working.
     */
    public static void selfTest() {

    System.out.print("Enter Communicator.selfTest\n");	

    System.out.print("\nTest for one speaker, one listener, speaker waits for listener\n");	

    Communicator comm = new Communicator();
    KThread threadSpeaker =  new KThread(new Speaker(comm, 100));
    threadSpeaker.setName("Thread speaker").fork();

    KThread.yield();

    KThread threadListener = new KThread(new Listener(comm));
    threadListener.setName("Thread listner").fork();

    KThread.yield();

    threadListener.join();
    threadSpeaker.join();

    System.out.print("\nTest for one speaker, one listener, listener waits for speaker\n");	
    Communicator comm1 = new Communicator();

    KThread threadListener1 = new KThread(new Listener(comm1));
    threadListener1.setName("Thread listner").fork();

    KThread.yield();

    KThread threadSpeaker1 =  new KThread(new Speaker(comm1, 100));
    threadSpeaker1.setName("Thread speaker").fork();

    KThread.yield();

    threadListener1.join();
    threadSpeaker1.join();


    System.out.print("\nTest for one speaker, more listener, listener waits for speaker\n");	

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



    System.out.print("\nTest for one speaker, more listener, speaker waits for listener \n");	

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

    System.out.print("\nTest for one speaker, more listener, listeners waits for speaker, and then create more listeners \n");	
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



    System.out.print("\nTest for more speaker, one listener, listener waits for speaker\n");	

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

    System.out.print("\nTest for more speaker, one listener, speaker waits for listener\n");	

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


