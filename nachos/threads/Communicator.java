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
        lock.acquire();

        while (this.word != 0) {   // hy+
            condition.sleep();     // hy+
        }                          // hy+ 

        this.word = word;          // hy+ 

        condition.wakeAll();       // hy+ 
        condition.sleep();         // hy+ 


        lock.release();            // hy+ 
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
        lock.acquire();

        while(this.word == 0) {   // hy+  
            condition.sleep();    // hy+  
        }                         // hy+

        int word = this.word;     // hy+   
        this.word = 0;            // hy+

        condition.wakeAll();      // hy+
        lock.release();           // hy+

        return word;              // hy+ 

        return 0;
    }


    int word = 0;
    Lock lock = new Lock();
    Condition2 condition = new Condition2(lock); 
}
