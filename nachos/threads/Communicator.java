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
        lock.acquire();            // hy+

        while (listener == 0) {    // hy+
            speakerCond.sleep();   // hy+
        }                          // hy+ 

        this.word = word;          // hy+ 

        wordReady = true; 
        listenerCond.wake();       // hy+ 

        lock.release();            // hy+ 
    }


    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {

        // Refer to: http://neuron.csie.ntust.edu.tw/homework/94/os/homework/homework2/OS%E4%BD%9C%E6%A5%AD2%20%20A9415013%20A9415014%20A9415028%20A9415058(new)/OS%E4%BD%9C%E6%A5%AD2%20%20A9415013%20A9415014%20A9415028%20A9415058(new).files/Page647.htm
        lock.acquire();

        listener++;

        speakerCond.wake();

        while(!wordReady) {     // hy+  
            listenerCond.sleep();    // hy+  
        }                         // hy+

        int word = this.word;     // hy+   
        wordRead = false;

        listener--;
        lock.release();           // hy+

        return word;              // hy+ 

        return 0;
    }

    int listener = 0;
    bool wordReady = false;
    int word = 0;
    Lock lock = new Lock();

    Condition2 speakerCond  = new Condition2(lock); 
    Condition2 listenerCond = new Condition2(lock); 
}


