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

            System.out.println("***[LotteryQueue]***");
            print();
            System.out.println("*******************");

            for (Iterator<KThread> ts = waitQueue.iterator(); ts.hasNext();) {  
                thread = ts.next(); 
                int priority = getThreadState(thread).getEffectivePriority();
                Lib.debug(dbgFlag, "[LotteryQueue.pickNextThread] Thread: " + thread 
                                    + "   Priority: " + priority);
                sum += priority;
            }

            Random rand = new Random();
            int lotteryValue = rand.nextInt(sum) + 1;

            Lib.debug(dbgFlag, "[LotteryQueue.pickNextThread] Sum: " + sum 
                                    + "   LotteryValue: " + lotteryValue);

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

            if (transferPriority == false) {
                return 0;
            }
            else {
                //if (dirty) {
                if (true) {
                    effectivePriority = 0; 

                    //Lib.debug(dbgFlag, "[ThreadState.getEffectivePriority] thread: " + this.thread); 
                    for (Iterator<KThread> it = waitQueue.iterator(); it.hasNext();) {  
                        KThread thread = it.next(); 
                        int priority = getThreadState(thread).getEffectivePriority();
                        Lib.debug(dbgFlag, "[LotteryQueue.getEffectivePriority] controlled thread: " + thread +  " priority: " +  priority ); 
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

            Lib.debug(dbgFlag, "[ThreadState.getEffectivePriority] holder thread: " + this.thread + " Size: " + this.myResource.size()); 
            if (dirty) {
                effectivePriority = this.priority;
                 
                for (Iterator<ThreadQueue> it = this.myResource.iterator(); it.hasNext();) {  
                    Lib.debug(dbgFlag, "[ThreadState.getEffectivePriority] holder thread: " + this.thread); 
                    LotteryQueue lg = (LotteryQueue)(it.next()); 
                    int waitQueuePriority = lg.getEffectivePriority();
                    Lib.debug(dbgFlag, "[ThreadState.getEffectivePriority] waitQueue priority: " + waitQueuePriority); 
                
                    effectivePriority += waitQueuePriority;
                }

                dirty = false;
            }

            return effectivePriority;
        } // ThreadState.getEffectivePriority

    } // ThreadState

    static private char dbgFlag = 't';
}



