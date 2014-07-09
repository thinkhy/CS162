package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList;    // +hy+
import java.util.Iterator;      // +hy+

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

/******************************************************************************
 *
 * 01* CHANGE-ACTIVITY:
 *
 * $B4=PROJECT2 TASK4, 140605, THINKHY: Implement lottery scheduler 
 * $B20=PROJECT2 TASK4, 140705, THINKHY: Count tickets number incorrectly 
 * $A21=PROJECT1 TASK5, 140705, THINKHY: ThreadState.myResource didn't remove holder 
 *
 *
 *****************************************************************************/

/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
    /**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
    }
    
    /**
     * Allocate a new priority thread queue.
     *
     * @param   transferPriority    <tt>true</tt> if this queue should
     *                  transfer priority from waiting threads
     *                  to the owning thread.
     * @return  a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
    //System.out.print("ThreadQueue.newThreadQueue: " + transferPriority + "\n");  // debug
    return new PriorityQueue(transferPriority);
    }

    public int getPriority(KThread thread) {
               
    return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) {

    return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
    boolean intStatus = Machine.interrupt().disabled();
               
    Lib.assertTrue(priority >= priorityMinimum &&
                       priority <= priorityMaximum);
    
    getThreadState(thread).setPriority(priority);
    Machine.interrupt().restore(intStatus);
    }

    public boolean increasePriority() {
    boolean intStatus = Machine.interrupt().disable();
               
    KThread thread = KThread.currentThread();

    int priority = getPriority(thread);
    if (priority == priorityMaximum) {
        Machine.interrupt().restore(intStatus);
        return false;
    }

    setPriority(thread, priority+1);

    Machine.interrupt().restore(intStatus);
    return true;
    }

    public boolean decreasePriority() {
    boolean intStatus = Machine.interrupt().disable();
               
    KThread thread = KThread.currentThread();

    int priority = getPriority(thread);
    if (priority == priorityMinimum) {
        Machine.interrupt().restore(intStatus);
        return false;
    }

    setPriority(thread, priority-1);

    Machine.interrupt().restore(intStatus);
    return true;
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    // public static final int priorityMaximum = 7;    
    public static final int priorityMaximum = Integer.MAX_VALUE;                /*@B4C*/

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param   thread  the thread whose scheduling state to return.
     * @return  the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
    if (thread.schedulingState == null)
        thread.schedulingState = new ThreadState(thread);

    return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    protected class PriorityQueue extends ThreadQueue {
        PriorityQueue(boolean transferPriority) {
            this.transferPriority = transferPriority;
        }

        public void waitForAccess(KThread thread) {
            boolean intStatus = Machine.interrupt().disabled();
            getThreadState(thread).waitForAccess(this);
            // Machine.interrupt().restore(intStatus);
        }

        public void acquire(KThread thread) {
            boolean intStatus = Machine.interrupt().disabled();
                
            Lib.debug('t', "[PriorityQueue.acquire] thread: " + thread
                        + " holder: " + this.holder );
            ThreadState state = getThreadState(thread); // hy+
             
            // If I have a holder and I transfer priority
            // remove myself from the holder's resource list
            // if (this.holder != null && this.transferPriority) {  /* @A21D */
            //if (this.transferPriority) {  /* @A21D */
            //    this.holder.myResource.remove(this); /* @A21D */
            // }  /* @A21D */
             
            this.holder = state;              // hy+
             
            state.acquire(this);
            // Machine.interrupt().restore(intStatus);
        }

        public KThread nextThread() {
            
            boolean intStatus = Machine.interrupt().disabled();

            if (waitQueue.isEmpty()) {
                // Machine.interrupt().restore(intStatus);
                return null;
            }
            
            
            // if I have a holder and I transfer priority, 
            // remove myself from the holder's resource list
            // if (this.holder != null && this.transferPriority)  /* @A21D */
            if (this.transferPriority)  /* @A21A */
            {
                this.holder.myResource.remove(this);
                this.holder = null;
            }

            KThread pickedThread = pickNextThread();
            if (pickedThread != null) {
                waitQueue.remove(pickedThread);
                this.acquire(pickedThread);          /* @B20C */
            }
            
            //Machine.interrupt().restore(intStatus);
            return pickedThread;
        }

        /**
         * Return the next thread that <tt>nextThread()</tt> would return,
         * without modifying the state of this queue.
         *
         * @return  the next thread that <tt>nextThread()</tt> would
         *      return.
         */
        protected KThread pickNextThread() {
            KThread nextThread = null;

            //this.print(); // debug

            // System.out.print("Inside 'pickNextThread:' transferPriority: " + transferPriority + "\n"); // debug

            for (Iterator<KThread> ts = waitQueue.iterator(); ts.hasNext();) {  
                KThread thread = ts.next(); 
                int priority = getThreadState(thread).getEffectivePriority();
                
                if (nextThread == null || priority > getThreadState(nextThread).getEffectivePriority()) { 
                    nextThread = thread;
                }
            }

            // System.out.print("Inside 'pickNextThread:' return Thread: " 
            //        + nextThread + "\n"); // debug
            
            return nextThread;
        }
        
        public int getEffectivePriority() {

            // System.out.print("[Inside getEffectivePriority] transferPriority: " + transferPriority + "\n"); // debug

            // if do not transfer priority, return minimum priority
            if (transferPriority == false) {
            // System.out.print("Inside 'getEffectivePriority:' false branch\n" ); // debug
                // return priorityMinimum;
                return priorityMinimum;
            }

            if (dirty) {
                effectivePriority = priorityMinimum; 
                for (Iterator<KThread> it = waitQueue.iterator(); it.hasNext();) {  
                    KThread thread = it.next(); 
                    int priority = getThreadState(thread).getEffectivePriority();
                    if ( priority > effectivePriority) { 
                        effectivePriority = priority;
                    }
                }
                dirty = false;
            }

            return effectivePriority;
        }

        public void setDirty() {
            if (transferPriority == false) {
                return;
            }

            dirty = true;

            if (holder != null) {
                holder.setDirty();
            }
        }

        public void print() {
            // implement me (if you want)
            for (Iterator<KThread> it = waitQueue.iterator(); it.hasNext();) {  
                KThread currentThread = it.next(); 
                int  priority = getThreadState(currentThread).getPriority();

                System.out.print("Thread: " + currentThread 
                                    + "\t  Priority: " + priority + "\n");
            }
        }

        /**
         * <tt>true</tt> if this queue should transfer priority from waiting
         * threads to the owning thread.
         */
        public boolean transferPriority;

        /** The queue  waiting on this resource */
        protected LinkedList<KThread> waitQueue = new LinkedList<KThread>();  // hy+

        /** The ThreadState corresponds to the holder of the resource */
        protected ThreadState holder = null;             // hy+

        /** Set to true when a new thread is added to the queue, 
         *  or any of the queues in the waitQueue flag themselves as dirty */
        protected boolean dirty;                  // hy+ 

        /** The cached highest of the effective priorities in the waitQueue. 
         *  This value is invalidated while dirty is true */
        protected int effectivePriority; 

    } /* PriorityQueue */


    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see nachos.threads.KThread#schedulingState
     */
    protected class ThreadState {

    /**
     * Allocate a new <tt>ThreadState</tt> object and associate it with the
     * specified thread.
     *
     * @param   thread  the thread this state belongs to.
     */
    public ThreadState(KThread thread) {
        this.thread = thread;
        
        setPriority(priorityDefault);
    }

    /**
     * Return the priority of the associated thread.
     *
     * @return  the priority of the associated thread.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Return the effective priority of the associated thread.
     *
     * @return  the effective priority of the associated thread.
     */
    public int getEffectivePriority() {

        int maxEffective = this.priority;

        // Implement on 10/15/2013
        if (dirty) {
            for (Iterator<ThreadQueue> it = myResource.iterator(); it.hasNext();) {  
                PriorityQueue pg = (PriorityQueue)(it.next()); 
                int effective = pg.getEffectivePriority();
                if (maxEffective < effective) {
                    maxEffective = effective;
                }
            }
        }
            
        return maxEffective;
    }

    /**
     * Set the priority of the associated thread to the specified value.
     *
     * @param   priority    the new priority.
     */
    public void setPriority(int priority) {
        if (this.priority == priority)
            return;
        
        this.priority = priority;
        
        setDirty();
    }

    /**
     * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
     * the associated thread) is invoked on the specified priority queue.
     * The associated thread is therefore waiting for access to the
     * resource guarded by <tt>waitQueue</tt>. This method is only called
     * if the associated thread cannot immediately obtain access.
     *
     * @param   waitQueue   the queue that the associated thread is
     *              now waiting on.
     *
     * @see nachos.threads.ThreadQueue#waitForAccess
     */
    public void waitForAccess(PriorityQueue waitQueue) {
        
        Lib.assertTrue(waitQueue.waitQueue.indexOf(thread) == -1);

        waitQueue.waitQueue.add(thread);
        waitQueue.setDirty();

        // set waitingOn
        waitingOn = waitQueue;

        // if the waitQueue was previously in myResource, remove it 
        // and set its holder to null
        // When will this IF statement be executed?
        if (myResource.indexOf(waitQueue) != -1) {
            myResource.remove(waitQueue);
            waitQueue.holder = null;                /* @B20A */
        }
    }

    /**
     * Called when the associated thread has acquired access to whatever is
     * guarded by <tt>waitQueue</tt>. This can occur either as a result of
     * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
     * <tt>thread</tt> is the associated thread), or as a result of
     * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
     *
     * @see nachos.threads.ThreadQueue#acquire
     * @see nachos.threads.ThreadQueue#nextThread
     */
    public void acquire(PriorityQueue waitQueue) {
        // implement me
        
        // [begin] hy, 9/20/2013 
        Lib.assertTrue(waitQueue.waitQueue.indexOf(this.thread) == -1);
        Lib.assertTrue(myResource.indexOf(waitQueue) == -1);
        
        // add waitQueue to myResource list
        myResource.add(waitQueue);
        
        // clean waitingOn if waitQueue is just waiting on
        if (waitQueue == waitingOn) {
            waitingOn = null;
        }

        // effective priority may be varied, set dirty flag
        setDirty();
    }   

    /**
     * ThreadState.setDirty Set the dirty flag, then call setdirty() on each thread
     * of priority queue that the thread is waiting for.
     *
     * ThreadState.setDirty and PriorityQueue.setDirty would invoke each other, they 
     * are mutually recursive.
     *
     */
    public void setDirty() {
        if (dirty) {
            return;
        }

        dirty = true;

        PriorityQueue pg = (PriorityQueue)waitingOn;
        if (pg != null) {
            pg.setDirty();
        }

    }

    /** The thread with which this object is associated. */    
    protected KThread thread;

    /** The priority of the associated thread. */
    protected int priority;

    protected int effectivePriority;            // hy+

    /** Collection of PriorityQueues that signify the Locks or other
     *  resource that this thread currently holds */
    protected LinkedList<ThreadQueue> myResource = new LinkedList<ThreadQueue>();  // hy+

    /** PriorityQueue corresponding to resources that this thread has attepmted to acquire but could not */
    protected ThreadQueue waitingOn; 

    /** Set to true when this thread's priority is changed, 
     * or when one of the queues in myResources flags itself as dirty */
    protected boolean dirty = false;                  // hy+ 

    }
}

