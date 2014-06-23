package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

/******************************************************************************
 *
 * 01* CHANGE-ACTIVITY:
 *
 * $B4=PROJECT2 TASK4, 140605, THINKHY: Implement lottery scheduler 
 *
 *
 *
 *****************************************************************************/


/**
 * A scheduler that chooses threads using a lottery.
 *
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 *
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 *
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */
public class LotteryScheduler extends PriorityScheduler {
    /**
     * Allocate a new lottery scheduler.
     */
    public LotteryScheduler() {
    }
    
    /**
     * Allocate a new lottery thread queue.
     *
     * @param   transferPriority    <tt>true</tt> if this queue should
     *                  transfer tickets from waiting threads
     *                  to the owning thread.
     * @return  a new lottery thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
    // implement me
    return new LotteryQueue(transferPriority);
    }

    protected ThreadState getThreadState(KThread thread) {
    if (thread.schedulingState == null)
        thread.schedulingState = new ThreadState(thread);

    return (ThreadState) thread.schedulingState;
    }


    protected class LotteryQueue extends PriorityQueue {
       LotteryQueue(boolean transferPriority) {
        super(transferPriority);
       }

       protected KThread pickNextThread() {
            KThread nextThread = null;
            int sum = 0;
            KThread thread;

            Lib.debug('t', "Inside pickNextThread"); 
            print();

            for (Iterator<KThread> ts = waitQueue.iterator(); ts.hasNext();) {  
                thread = ts.next(); 
                int priority = getThreadState(thread).getEffectivePriority();
                System.out.print("Loop: Thread: " + thread 
                                    + "\t  Priority: " + priority + "\n");
                sum += priority;
            }

            Random rand = new Random();
            int lotteryValue = rand.nextInt(sum) + 1;

            Lib.debug(dbgFlag, "Lottery value: " + lotteryValue 
                                    + "   Sum: " + sum);

            sum = 0; 

            for (Iterator<KThread> ts = waitQueue.iterator(); ts.hasNext();) {  
                thread = ts.next(); 
                int priority = getThreadState(thread).getEffectivePriority();
                sum += priority;

                if (sum >= lotteryValue) {
                    nextThread = thread;    
                    break;
                }
            }

            Lib.assertTrue(nextThread != null);
            return nextThread;
       }

       public int getEffectivePriority() {
            System.out.println("Inside  LotteryQueue.getEffectivePriority");
            print();
            System.out.println("===========End of Queue=================");

            if (transferPriority == false) {
                return 0;
            }
            else {
                if (dirty) {
                    effectivePriority = 0; 

                    for (Iterator<KThread> it = waitQueue.iterator(); it.hasNext();) {  
                        KThread thread = it.next(); 
                        int priority = getThreadState(thread).getEffectivePriority();
                        effectivePriority += priority;
                    }
                }

                dirty = false;
                return effectivePriority;
            } /* else */
        } /* getEffectivePriority */
    }

    protected class ThreadState extends PriorityScheduler.ThreadState {

        public ThreadState(KThread thread) {
          super(thread);
        }

        public int getEffectivePriority() {
            System.out.println("Inside  ThreadState.getEffectivePriority");

            if (dirty) {
                effectivePriority = this.priority;

                for (Iterator<ThreadQueue> it = myResource.iterator(); it.hasNext();) {  
                    System.out.println("Loop myResource inside ThreadState.getEffectivePriority");
                    LotteryQueue lg = (LotteryQueue)(it.next()); 
                    System.out.println("===========Start of Resource Queue=================");
                    lg.print();
                    System.out.println("===========End of Resource Queue=================");
                    int waitQueuePriority = lg.getEffectivePriority();

                    effectivePriority += waitQueuePriority;
                }

                dirty = false;
            }

            return effectivePriority;
        } // ThreadState.getEffectivePriority

    } // ThreadState

    static private char dbgFlag = 't';
}



