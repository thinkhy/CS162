package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.util.LinkedList; 
import java.util.Iterator;   
import java.util.HashMap;   

/**************************************************************************
 *
 * 01* CHANGE-ACTIVITY:
 *                                                                        
 *  $BA=PROJECT2 TASK1, 140125, THINKHY: Implement the file system calls  
 *  $BB=PROJECT2 TASK2, 140222, THINKHY: Implement support for multiprogramming  
 *  $BC=PROJECT2 TASK3, 140302, THINKHY: Implement system calls for process management
 *                                                                        
 **************************************************************************/

/**
 * A kernel that can support multiple user processes
 */
public class UserKernel extends ThreadedKernel {
    /**
     * Allocate a new user kernel.
     */
    public UserKernel() {
	super();
    }

    /**
     * Initialize this kernel. Creates a synchronized console and sets the
     * processor's exception handler.
     */
    public void initialize(String[] args) {
	super.initialize(args);

	console = new SynchConsole(Machine.console());
	
	Machine.processor().setExceptionHandler(new Runnable() {
		public void run() { exceptionHandler(); }
	    });

    int numPhysPages = Machine.processor().getNumPhysPages();     // @BAA
    for(int i = 0; i < numPhysPages; i++)                         // @BAA          
        pageTable.add(i);                                         // @BAA


    }

    /**
     * Test the console device.
     */	
    public void selfTest() {
	super.selfTest(); // @BAD

	System.out.println("Testing the console device. Typed characters");
	System.out.println("will be echoed until q is typed.");

    // commented by HY [1/12/2014]
    /*
	char c;

	do {
	    c = (char) console.readByte(true);
	    console.writeByte(c);
	}
	while (c != 'q');

	System.out.println("");
    */
    }

    /**
     * Returns the current process.
     *
     * @return	the current process, or <tt>null</tt> if no process is current.
     */
    public static UserProcess currentProcess() {
	if (!(KThread.currentThread() instanceof UThread))
	    return null;
	
	return ((UThread) KThread.currentThread()).process;
    }

    /**
     * The exception handler. This handler is called by the processor whenever
     * a user instruction causes a processor exception.
     *
     * <p>
     * When the exception handler is invoked, interrupts are enabled, and the
     * processor's cause register contains an integer identifying the cause of
     * the exception (see the <tt>exceptionZZZ</tt> constants in the
     * <tt>Processor</tt> class). If the exception involves a bad virtual
     * address (e.g. page fault, TLB miss, read-only, bus error, or address
     * error), the processor's BadVAddr register identifies the virtual address
     * that caused the exception.
     */
    public void exceptionHandler() {
	Lib.assertTrue(KThread.currentThread() instanceof UThread);

	UserProcess process = ((UThread) KThread.currentThread()).process;
	int cause = Machine.processor().readRegister(Processor.regCause);
	process.handleException(cause);
    }

    /**
     * Start running user programs, by creating a process and running a shell
     * program in it. The name of the shell program it must run is returned by
     * <tt>Machine.getShellProgramName()</tt>.
     *
     * @see	nachos.machine.Machine#getShellProgramName
     */
    public void run() {
	super.run();

	UserProcess process = UserProcess.newUserProcess();

	
	String shellProgram = Machine.getShellProgramName();	
    
    // [added by hy 12/31/2013]
    Lib.debug('a', "Shell program: " + shellProgram);

	Lib.assertTrue(process.execute(shellProgram, new String[] { }));

	KThread.currentThread().finish();
    }

    /**
     * Terminate this kernel. Never returns.
     */
    public void terminate() {
	super.terminate();
    }

    /**
     * Return number of a free page.
     * If page talbe is empty, return -1 otherwise return free page number.
     */
    public static int getFreePage() {                              // @BBA
        int pageNumber = -1;                                       // @BBA
        Machine.interrupt().disable();                             // @BBA 
        if (pageTable.isEmpty() == false)                          // @BBA 
           pageNumber = pageTable.removeFirst();                   // @BBA
        Machine.interrupt().enable();                              // @BBA 
        return pageNumber;                                         // @BBA
    }                                                              // @BBA

    /**
     * Add a free page into page linked list.
     */
    public static void addFreePage(int pageNumber) {               // @BBA
       Lib.assertTrue(pageNumber >= 0                              // @BBA
           && pageNumber < Machine.processor().getNumPhysPages()); // @BBA
       Machine.interrupt().disable();                              // @BBA 
       pageTable.add(pageNumber);                                  // @BBA 
       Machine.interrupt().enable();                               // @BBA 
    }                                                              // @BBA 


    /**
     * return next Pid
     */
    public static int getNextPid() {                               // @BCA 
        int retval;                                                // @BCA
        Machine.interrupt().disable();                             // @BCA 
        retval = ++nextPid;                                        // @BCA 
        Machine.interrupt().enabled();                             // @BCA 
        return nextPid;                                            // @BCA
    }                                                              // @BCA

    /**
     * get process from process map by pid
     */
    public static UserProcess getProcessByID(int pid) {
        return processMap.get(pid);
    }

    /**
     * get process from process map by pid
     */
    public static UserProcess registerProcess(int pid, UserProcess process) {  // @BCA
        UserProcess insertedProcess;                               // @BCA 
        Machine.interrupt().disable();                             // @BCA 
        insertedProcess = processMap.put(pid, process);            // @BCA 
        Machine.interrupt().enabled();                             // @BCA 
        return insertedProcess;                                    // @BCA 
    }                                                              // @BCA


    /** Globally accessible reference to the synchronized console. */
    public static SynchConsole console;
        
    /** dummy variables to make javac smarter. */
    private static Coff dummy1 = null;

    /** maintain a global linked list of free physical pages.      */
    private static LinkedList<Integer> pageTable                   // @BBA
                         = new LinkedList<Integer>();              // @BBA
    
    /** Maintain a static counter which indicates the next process ID
     * to assign, assume that the process ID counter will not overflow.
     */
    private static int nextPid = 0;                                // @BCA

    /** maintain a map which stores processes, key is pid,
     * value is the process which holds the pid.                   */
    private static HashMap<Integer, UserProcess>                   // @BCA 
              processMap = new HashMap<Integer, UserProcess>();    // @BCA 

}



