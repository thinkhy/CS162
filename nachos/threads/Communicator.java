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

        // now speader acquires the lock 

        // no available listener, speaker goes to sleep
        while (listener == 0) {   // hy+
            speakerCond.sleep();  // hy+
        }                         // hy+ 

        // speaker says a word
        this.word = word;         // hy+ 

        // set flag that word is ready
        isWordReady = true;       // hy+  


        // wake up a listener
        listenerCond.wake();      // hy+  

        lock.release();            // hy+ 
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

        // inform speacker I have come 
        speakerCond.wake();      // hy+  

        // while word is not ready, listener goes to sleep
        while(isWordReady == false) {   // hy+  
            listenerCond.sleep();       // hy+  
        }                         // hy+

        // listener receives the word
        int word = this.word;     // hy+   

        // reset flag that word is invalid
        isWordReady = false;      // hy+  

        // decrease the number of listener 
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

    Communicator comm = new Communicator();
    KThread threadSpeaker =  new KThread(new Speaker(comm, 100));
    threadSpeaker.setName("Thread speaker").fork();

    KThread threadListener = new KThread(new Listener(comm));
    threadListener.setName("Thread listner").fork();

    KThread.yield();

    threadListener.join();
    threadSpeaker.join();

    System.out.print("Leave Communicator.selfTest\n");	

    }


    private int listener = 0;             // hy+
    private int word = 0;                 // hy+ 
    private boolean isWordReady = false;     // hy+ 

    private Lock lock;                    // hy+
    private Condition2 speakerCond;       // hy+
    private Condition2 listenerCond;      // hy+
}


