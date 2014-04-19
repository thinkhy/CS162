package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.util.LinkedList; 
import java.util.Iterator; 
import java.io.EOFException;

/****************************************************************************************
 *
 * 01* CHANGE-ACTIVITY:
 *                                                                        
 *  $BA=PROJECT2 TASK1, 140125, THINKHY: Implement the file system calls  
 *  $BB=PROJECT2 TASK2, 140205, THINKHY: Implement support for multiprogramming  
 *  $BC=PROJECT2 TASK3, 140302, THINKHY: Implement system calls for process management
 *                                                                        
 ****************************************************************************************/

/**
 * Encapsulates the state of a user process that is not contained in its
 * user thread (or threads). This includes its address translation state, a
 * file table, and information about the program being executed.
 *
 * <p>
 * This class is extended by other classes to support additional functionality
 * (such as additional syscalls).
 *
 * @see	nachos.vm.VMProcess
 * @see	nachos.network.NetProcess
 */
public class UserProcess {
    /**
     * Allocate a new process.
     */
    public UserProcess() {
    
    /**************************************************************************/
    /* Need to create elements of fds one by one, any better code here?  @BAA */
    /**************************************************************************/
    for (int i=0; i<MAXFD; i++) {                                       /*@BAA*/
         fds[i] = new FileDescriptor();                                 /*@BAA*/
    }                                                                   /*@BAA*/

    /**************************************************************************/
    /* Create STDIN and STDOUT                                           @BAA */
    /**************************************************************************/
    fds[STDIN].file = UserKernel.console.openForReading();              /*@BAA*/
    fds[STDIN].position = 0;
	Lib.assertTrue(fds[STDIN] != null);                                    /*@BAA*/  
    /* fds[STDOUT].file = UserKernel.console.openForWriting();*/        /*@BAA*/
    /* fds[STDOUT].position = 0; */
	/* Lib.assertTrue(fds[STDOUT] != null); */                          /*@BAA*/ 
    // Just for TEST!!! [140203 hy]                                     /*@BAA*/
    OpenFile retval  = UserKernel.fileSystem.open("out", false);        /*@BAA*/

    int fileHandle = findEmptyFileDescriptor();                         /*@BAA*/ 
    System.out.println("*** File handle: " + fileHandle);               /*@BAA*/
    fds[fileHandle].file = retval;                                      /*@BAA*/
    fds[fileHandle].position = 0;                                       /*@BAA*/


    pid = UserKernel.getNextPid();                                      /*@BCA*/

    /* register this new process in UserKenel's map                           */
    UserKernel.registerProcess(pid, this);                              /*@BCA*/

    }                                                                   /*@BAA*/
    
    /**
     * Allocate and return a new process of the correct class. The class name
     * is specified by the <tt>nachos.conf</tt> key
     * <tt>Kernel.processClassName</tt>.
     *
     * @return	a new process of the correct class.
     */
    public static UserProcess newUserProcess() {
	return (UserProcess)Lib.constructObject(Machine.getProcessClassName());
    }

    /**
     * Execute the specified program with the specified arguments. Attempts to
     * load the program, and then forks a thread to run it.
     *
     * @param	name	the name of the file containing the executable.
     * @param	args	the arguments to pass to the executable.
     * @return	<tt>true</tt> if the program was successfully executed.
     */
    public boolean execute(String name, String[] args) {
	if (!load(name, args))
	    return false;
	
	/* new UThread(this).setName(name).fork();                     @BCD*/
    thread = new UThread(this);                                  /*@BCA*/ 
    thread.setName(name).fork();                                 /*@BCA*/

	return true;
    }

    /**
     * Save the state of this process in preparation for a context switch.
     * Called by <tt>UThread.saveState()</tt>.
     */
    public void saveState() {
    }

    /**
     * Restore the state of this process after a context switch. Called by
     * <tt>UThread.restoreState()</tt>.
     */
    public void restoreState() {
	Machine.processor().setPageTable(pageTable);
    }

    /**
     * Read a null-terminated string from this process's virtual memory. Read
     * at most <tt>maxLength + 1</tt> bytes from the specified address, search
     * for the null terminator, and convert it to a <tt>java.lang.String</tt>,
     * without including the null terminator. If no null terminator is found,
     * returns <tt>null</tt>.
     *
     * @param	vaddr	the starting virtual address of the null-terminated
     *			string.
     * @param	maxLength	the maximum number of characters in the string,
     *				not including the null terminator.
     * @return	the string read, or <tt>null</tt> if no null terminator was
     *		found.
     */
    public String readVirtualMemoryString(int vaddr, int maxLength) {
	Lib.assertTrue(maxLength >= 0);

	byte[] bytes = new byte[maxLength+1];

	int bytesRead = readVirtualMemory(vaddr, bytes);

	for (int length=0; length<bytesRead; length++) {
	    if (bytes[length] == 0)
		return new String(bytes, 0, length);
	}

	return null;
    }

    /**
     * Transfer data from this process's virtual memory to all of the specified
     * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param	vaddr	the first byte of virtual memory to read.
     * @param	data	the array where the data will be stored.
     * @return	the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data) {
	return readVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from this process's virtual memory to the specified array.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param	vaddr	the first byte of virtual memory to read.
     * @param	data	the array where the data will be stored.
     * @param	offset	the first byte to write in the array.
     * @param	length	the number of bytes to transfer from virtual memory to
     *			the array.
     * @return	the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data, int offset,
				 int length) {
	Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);

    Processor processor = Machine.processor();                               /* @BBA  */
	byte[] memory = processor.getMemory();
	
    /** original code [removed by hy 3/1/2014] @BBD
	// for now, just assume that virtual addresses equal physical addresses
	if (vaddr < 0 || vaddr >= memory.length)             
	    return 0;                                       
    */
          
	// calculate virtual page number from the virtual address               /* @BBA */
    int vpn = processor.pageFromAddress(vaddr);                             /* @BBA */
    int addressOffset = processor.offsetFromAddress(vaddr);                 /* @BBA */

	TranslationEntry entry = null;                                          /* @BBA */
    entry = pageTable[vpn];                                                 /* @BBA */
	entry.used = true;                                                      /* @BBA */

    int ppn = entry.ppn;                                                    /* @BBA */
	int paddr = (ppn*pageSize) + addressOffset;                             /* @BBA */
    // check if physical page number is out of range
    if (ppn < 0 || ppn >= processor.getNumPhysPages())  {                   /* @BBA */
        Lib.debug(dbgProcess,                                               /* @BBA */ 
                "\t\t UserProcess.readVirtualMemory(): bad ppn "+ppn);      /* @BBA */
        return 0;                                                           /* @BBA */
    }                                                                       /* @BBA */

	int amount = Math.min(length, memory.length-paddr);
	System.arraycopy(memory, paddr, data, offset, amount);

	return amount;
    }

    /**
     * Transfer all data from the specified array to this process's virtual
     * memory.
     * Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param	vaddr	the first byte of virtual memory to write.
     *
     * @param	data	the array containing the data to transfer.
     * @return	the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data) {
	return writeVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from the specified array to this process's virtual memory.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param	vaddr	the first byte of virtual memory to write.
     * @param	data	the array containing the data to transfer.
     * @param	offset	the first byte to transfer from the array.
     * @param	length	the number of bytes to transfer from the array to
     *			virtual memory.
     * @return	the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data, int offset,
				  int length) {
	Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);

    Processor processor = Machine.processor();                  /* @BBA */
	byte[] memory = Machine.processor().getMemory();
	
    /** removed by hy [3/1/2014]
	// for now, just assume that virtual addresses equal physical addresses
	if (vaddr < 0 || vaddr >= memory.length)
	    return 0;
    */

	// calculate virtual page number from the virtual address
    int vpn = processor.pageFromAddress(vaddr);                 /* @BBA */            
    int addressOffset = processor.offsetFromAddress(vaddr);     /* @BBA */

	TranslationEntry entry = null;                              /* @BBA */
    entry = pageTable[vpn];                                     /* @BBA */
	entry.used = true;                                          /* @BBA */
	entry.dirty = true;                                         /* @BBA */

    int ppn = entry.ppn;                                        /* @BBA */
	int paddr = (ppn*pageSize) + addressOffset;                 /* @BBA */

    if (entry.readOnly) {                                       /* @BBA */
        Lib.debug(dbgProcess,                                   /* @BBA */
                 "\t\t UserProcess.writeVirtualMemory(): write read-only page "+ppn); /* @BBA */
        return 0;                                               /* @BBA */
    }                                                           /* @BBA */

    // check if physical page number is out of range
    if (ppn < 0 || ppn >= processor.getNumPhysPages())  {       /* @BBA */
        Lib.debug(dbgProcess, "\t\t UserProcess.writeVirtualMemory(): bad ppn "+ppn); /* @BBA */
        return 0;                                               /* @BBA */
    }                                                           /* @BBA */

	int amount = Math.min(length, memory.length-vaddr);
	System.arraycopy(data, offset, memory, vaddr, amount);

	return amount;
    }

    /**
     * Load the executable with the specified name into this process, and
     * prepare to pass it the specified arguments. Opens the executable, reads
     * its header information, and copies sections and arguments into this
     * process's virtual memory.
     *
     * @param	name	the name of the file containing the executable.
     * @param	args	the arguments to pass to the executable.
     * @return	<tt>true</tt> if the executable was successfully loaded.
     */
    private boolean load(String name, String[] args) {
	Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");
	
	OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
	if (executable == null) {
	    Lib.debug(dbgProcess, "\topen failed");
	    return false;
	}

	try {
	    coff = new Coff(executable);
	}
	catch (EOFException e) {
	    executable.close();
	    Lib.debug(dbgProcess, "\tcoff load failed");
	    return false;
	}

	// make sure the sections are contiguous and start at page 0
	numPages = 0;
	for (int s=0; s<coff.getNumSections(); s++) {
	    CoffSection section = coff.getSection(s);
	    if (section.getFirstVPN() != numPages) {
		coff.close();
		Lib.debug(dbgProcess, "\tfragmented executable");
		return false;
	    }
	    numPages += section.getLength();
	}

	// make sure the argv array will fit in one page
	byte[][] argv = new byte[args.length][];
	int argsSize = 0;
	for (int i=0; i<args.length; i++) {
	    argv[i] = args[i].getBytes();
	    // 4 bytes for argv[] pointer; then string plus one for null byte
	    argsSize += 4 + argv[i].length + 1;
	}
	if (argsSize > pageSize) {
	    coff.close();
	    Lib.debug(dbgProcess, "\targuments too long");
	    return false;
	}

	// program counter initially points at the program entry point
	initialPC = coff.getEntryPoint();	

	// next comes the stack; stack pointer initially points to top of it
	numPages += stackPages;
	initialSP = numPages*pageSize;

	// and finally reserve 1 page for arguments
	numPages++;

    pageTable = new TranslationEntry[numPages];                                        /* @BBA */
    for (int i = 0; i < numPages; i++) {                                               /* @BBA */
        int ppn = UserKernel.getFreePage();                                            /* @BBA */
        pageTable[i] =  new TranslationEntry(i, ppn, true, false, false, false);       /* @BBA */
    }                                                                                  /* @BBA */

	if (!loadSections())
	    return false;

	// store arguments in last page
	int entryOffset = (numPages-1)*pageSize;
	int stringOffset = entryOffset + args.length*4;

	this.argc = args.length;
	this.argv = entryOffset;
	
	for (int i=0; i<argv.length; i++) {
	    byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
	    Lib.assertTrue(writeVirtualMemory(entryOffset,stringOffsetBytes) == 4);
	    entryOffset += 4;
	    Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) ==
		       argv[i].length);
	    stringOffset += argv[i].length;
	    Lib.assertTrue(writeVirtualMemory(stringOffset,new byte[] { 0 }) == 1);
	    stringOffset += 1;
	}

	return true;
    }

    /**
     * Allocates memory for this process, and loads the COFF sections into
     * memory. If this returns successfully, the process will definitely be
     * run (this is the last step in process initialization that can fail).
     *
     * @return	<tt>true</tt> if the sections were successfully loaded.
     */
    protected boolean loadSections() {
	if (numPages > Machine.processor().getNumPhysPages()) {
	    coff.close();
	    Lib.debug(dbgProcess, "\tinsufficient physical memory");
	    return false;
	}

	// load sections
	for (int s=0; s<coff.getNumSections(); s++) {
	    CoffSection section = coff.getSection(s);
	    
        Lib.debug(dbgProcess, "\tinitializing " + section.getName()
                + " section (" + section.getLength() + " pages)");

	    for (int i=0; i<section.getLength(); i++) {
		int vpn = section.getFirstVPN()+i;

        /** removed by hy [3/1/2014] @BBD
		// for now, just assume virtual addresses=physical addresses
		// section.loadPage(i, vpn);
        */
         
        // translate virtual page number from physical page number
        TranslationEntry entry = pageTable[vpn];                                   /* @BBA */ 
        entry.readOnly = section.isReadOnly();                                     /* @BBA */ 
        int ppn = entry.ppn;                                                       /* @BBA */ 
        
        section.loadPage(i, ppn);                                                  /* @BBA */ 
	    }
	}
	
	return true;
    }

    /**
     * Release any resources allocated by <tt>loadSections()</tt>.
     */
    protected void unloadSections() {                                              /*@BBA*/
        /* back out physical pages and make page entry invalid                           */
        for (int i = 0; i < numPages; i++) {                                       /*@BBA*/
            UserKernel.addFreePage(pageTable[i].ppn);                              /*@BBA*/
            pageTable[i].valid = false;                                            /*@BBA*/
        }                                                                          /*@BBA*/
    }                                                                              /*@BBA*/

    /**
     * Initialize the processor's registers in preparation for running the
     * program loaded into this process. Set the PC register to point at the
     * start function, set the stack pointer register to point at the top of
     * the stack, set the A0 and A1 registers to argc and argv, respectively,
     * and initialize all other registers to 0.
     */
    public void initRegisters() {
	Processor processor = Machine.processor();

	// by default, everything's 0
	for (int i=0; i<processor.numUserRegisters; i++)
	    processor.writeRegister(i, 0);

	// initialize PC and SP according
	processor.writeRegister(Processor.regPC, initialPC);
	processor.writeRegister(Processor.regSP, initialSP);

	// initialize the first two argument registers to argc and argv
	processor.writeRegister(Processor.regA0, argc);
	processor.writeRegister(Processor.regA1, argv);
    }
     
    /**
     * Handle the halt() system call. 
     */
    private int handleHalt() {

	Machine.halt();
	Lib.assertNotReached("Machine.halt() did not halt machine!");
	return 0;
    }

    /**
     * Attempt to open the named disk file, creating it if does not exist,    *
     * and return a file descriptor that can be used to access the file.      *
     * Note that creat() can only be used to create files on disk; creat()    *
     * will never return a file descriptor referrring to a stream             *
     * Returns the new file descriptor, or -1 if an error occurred            *
     *                                                                        *
     * added by hy 1/18/2014                                                  *
     *
     */
    private int handleCreate(int a0) {
        // private int handleCreate() {                                 
	    Lib.debug(dbgProcess, "handleCreate()");                           /*@BAA*/

        // a0 is address of filename 
        String filename = readVirtualMemoryString(a0, MAXSTRLEN);          /*@BAA*/

	    Lib.debug(dbgProcess, "filename: "+filename);                      /*@BAA*/

        // invoke open through stubFilesystem
        OpenFile retval  = UserKernel.fileSystem.open(filename, true);     /*@BAA*/

        if (retval == null) {                                              /*@BAA*/
            return -1;                                                     /*@BAA*/
        }                                                                  /*@BAA*/
        else {                                                             /*@BAA*/
            int fileHandle = findEmptyFileDescriptor();                    /*@BAA*/ 
            if (fileHandle < 0)                                            /*@BAA*/ 
                return -1;                                                 /*@BAA*/ 
            else {                                                         /*@BAA*/
                fds[fileHandle].filename = filename;                       /*@BAA*/
                fds[fileHandle].file = retval;                             /*@BAA*/
                fds[fileHandle].position = 0;                              /*@BAA*/
                return fileHandle;                                         /*@BAA*/
            }                                                              /*@BAA*/ 
        }                                                                  /*@BAA*/
    }                                                                      /*@BAA*/

    /**
     * Attempt to open the named file and return a file descriptor.
     *
     * Note that open() can only be used to open files on disk; open() will never
     * return a file descriptor referring to a stream.
     *
     * Returns the new file descriptor, or -1 if an error occurred.
     */
    private int handleOpen(int a0) {
	    Lib.debug(dbgProcess, "handleOpen()");                             /*@BAA*/

        // a0 is address of filename 
        String filename = readVirtualMemoryString(a0, MAXSTRLEN);          /*@BAA*/

	    Lib.debug(dbgProcess, "filename: "+filename);                      /*@BAA*/

        // invoke open through stubFilesystem, truncate flag is set to false
        OpenFile retval  = UserKernel.fileSystem.open(filename, false);    /*@BAA*/

        if (retval == null) {                                              /*@BAA*/
            return -1;                                                     /*@BAA*/
        }                                                                  /*@BAA*/
        else {                                                             /*@BAA*/
            int fileHandle = findEmptyFileDescriptor();                    /*@BAA*/ 
            if (fileHandle < 0)                                            /*@BAA*/ 
                return -1;                                                 /*@BAA*/ 
            else {                                                         /*@BAA*/
                fds[fileHandle].filename = filename;                       /*@BAA*/
                fds[fileHandle].file = retval;                             /*@BAA*/
                fds[fileHandle].position = 0;                              /*@BAA*/
                return fileHandle;                                         /*@BAA*/
            }                                                              /*@BAA*/ 
        }                                                                  /*@BAA*/
    }                                                                      /*@BAA*/
 

    /**
     * Attempt to read up to count bytes into buffer from the file or stream
     * referred to by fileDescriptor.
     *
     * On success, the number of bytes read is returned. If the file descriptor
     * refers to a file on disk, the file position is advanced by this number.
     *
     * It is not necessarily an error if this number is smaller than the number of
     * bytes requested. If the file descriptor refers to a file on disk, this
     * indicates that the end of the file has been reached. If the file descriptor
     * refers to a stream, this indicates that the fewer bytes are actually
     * available right now than were requested, but more bytes may become available
     * in the future. Note that read() never waits for a stream to have more data;
     * it always returns as much as possible immediately.
     *
     * On error, -1 is returned, and the new file position is undefined. This can
     * happen if fileDescriptor is invalid, if part of the buffer is read-only or
     * invalid, or if a network stream has been terminated by the remote host and
     * no more data is available.
     */
    private int handleRead(int a0, int a1, int a2) {                      /*@BAA*/
	    Lib.debug(dbgProcess, "handleRead()");                            /*@BAA*/
         
        int handle = a0;                    /* a0 is file descriptor handle @BAA*/
        int vaddr = a1;                     /* a1 is buf address            @BAA*/
        int bufsize = a2;                   /* a2 is buf size               @BAA*/

	    Lib.debug(dbgProcess, "handle: " + handle);                       /*@BAA*/
	    Lib.debug(dbgProcess, "buf address: " + vaddr);                   /*@BAA*/
	    Lib.debug(dbgProcess, "buf size: " + bufsize);                    /*@BAA*/

        // get data regarding to file descriptor
        if (handle < 0 || handle > MAXFD                                  /*@BAA*/
                || fds[handle].file == null)                              /*@BAA*/
            return -1;                                                    /*@BAA*/

        FileDescriptor fd = fds[handle];                                  /*@BAA*/
        byte[] buf = new byte[bufsize];                                   /*@BAA*/

        // invoke read through stubFilesystem
        int retval = fd.file.read(fd.position, buf, 0, bufsize);          /*@BAA*/

        if (retval < 0) {                                                 /*@BAA*/
            return -1;                                                    /*@BAA*/
        }                                                                 /*@BAA*/
        else {                                                            /*@BAA*/
            int number = writeVirtualMemory(vaddr, buf);                  /*@BAA*/
            fd.position = fd.position + number;                           /*@BAA*/
            return retval;                                                /*@BAA*/
        }                                                                 /*@BAA*/
    }                                                                     /*@BAA*/
    
    /**
     * Attempt to write up to count bytes from buffer to the file or stream
     * referred to by fileDescriptor. write() can return before the bytes are
     * actually flushed to the file or stream. A write to a stream can block,
     * however, if kernel queues are temporarily full.
     *
     * On success, the number of bytes written is returned (zero indicates nothing
     * was written), and the file position is advanced by this number. It IS an
     * error if this number is smaller than the number of bytes requested. For
     * disk files, this indicates that the disk is full. For streams, this
     * indicates the stream was terminated by the remote host before all the data
     * was transferred.
     *
     * On error, -1 is returned, and the new file position is undefined. This can
     * happen if fileDescriptor is invalid, if part of the buffer is invalid, or
     * if a network stream has already been terminated by the remote host.
     *
     * Syscall: 
     *       int write(int fileDescriptor, void *buffer, int count);
     *
     */
     private int handleWrite(int a0, int a1, int a2) {
	    Lib.debug(dbgProcess, "handleWrite()");                           /*@BAA*/
         
        int handle = a0;                    /* a0 is file descriptor handle @BAA*/
        int vaddr = a1;                     /* a1 is buf address            @BAA*/
        int bufsize = a2;                   /* a2 is buf size               @BAA*/

	    Lib.debug(dbgProcess, "handle: " + handle);                       /*@BAA*/
	    Lib.debug(dbgProcess, "buf address: " + vaddr);                   /*@BAA*/
	    Lib.debug(dbgProcess, "buf size: " + bufsize);                    /*@BAA*/

        // get data regarding to file descriptor
        if (handle < 0 || handle > MAXFD                                  /*@BAA*/
                || fds[handle].file == null)                              /*@BAA*/
            return -1;                                                    /*@BAA*/

        FileDescriptor fd = fds[handle];                                  /*@BAA*/

        byte[] buf = new byte[bufsize];                                   /*@BAA*/  

        int bytesRead = readVirtualMemory(vaddr, buf);                    /*@BAA*/

        // invoke read through stubFilesystem                             /*@BAA*/
        int retval = fd.file.write(fd.position, buf, 0, bytesRead);       /*@BAA*/

        if (retval < 0) {                                                 /*@BAA*/
            return -1;                                                    /*@BAA*/
        }                                                                 /*@BAA*/
        else {                                                            /*@BAA*/
            fd.position = fd.position + retval;                           /*@BAA*/
            return retval;                                                /*@BAA*/
        }                                                                 /*@BAA*/
    }

    /**
     * Close a file descriptor, so that it no longer refers to any file or stream
     * and may be reused.
     *
     * If the file descriptor refers to a file, all data written to it by write()
     * will be flushed to disk before close() returns.
     * If the file descriptor refers to a stream, all data written to it by write()
     * will eventually be flushed (unless the stream is terminated remotely), but
     * not necessarily before close() returns.
     *
     * The resources associated with the file descriptor are released. If the
     * descriptor is the last reference to a disk file which has been removed using
     * unlink, the file is deleted (this detail is handled by the file system
     * implementation).
     *
     * Returns 0 on success, or -1 if an error occurred.
     */
    private int handleClose(int a0) {                                     /*@BAA*/
	    Lib.debug(dbgProcess, "handleClose()");                           /*@BAA*/
        
        int handle = a0;                                                  /*@BAA*/
        if (a0 < 0 || a0 >= MAXFD)                                        /*@BAA*/
            return -1;                                                    /*@BAA*/

        boolean retval = true;                                            /*@BAA*/

        FileDescriptor fd = fds[handle];                                  /*@BAA*/

        fd.position = 0;                                                  /*@BAA*/
        fd.file.close();                                                  /*@BAA*/

        // remove this file if necessary                                  /*@BAA*/
        if (fd.toRemove) {                                                /*@BAA*/
            retval = UserKernel.fileSystem.remove(fd.filename);           /*@BAA*/
            fd.toRemove = false;                                          /*@BAA*/  
        }                                                                 /*@BAA*/

        fd.filename = "";                                                 /*@BAA*/

        return retval ? 0 : -1;                                           /*@BAA*/
    }                                                                     /*@BAA*/

    /**
     * Delete a file from the file system. If no processes have the file open, the
     * file is deleted immediately and the space it was using is made available for
     * reuse.
     *
     * If any processes still have the file open, the file will remain in existence
     * until the last file descriptor referring to it is closed. However, creat()
     * and open() will not be able to return new file descriptors for the file
     * until it is deleted.
     *
     * Returns 0 on success, or -1 if an error occurred.
     */
    private int handleUnlink(int a0) {
	    Lib.debug(dbgProcess, "handleUnlink()");

        boolean retval = true;

        // a0 is address of filename 
        String filename = readVirtualMemoryString(a0, MAXSTRLEN);         /*@BAA*/

	    Lib.debug(dbgProcess, "filename: " + filename);                   /*@BAA*/

        int fileHandle = findFileDescriptorByName(filename);              /*@BAA*/ 
        if (fileHandle < 0) {                                             /*@BAA*/  
           /* invoke open through stubFilesystem, truncate flag is set to false
            * If no processes have the file open, the file is deleted immediately 
            * and the space it was using is made available for reuse.
            */
            retval = UserKernel.fileSystem.remove(fds[fileHandle].filename); /*@BAA*/
        }                                                                    /*@BAA*/ 
        else {                                                               /*@BAA*/
            /* If any processes still have the file open, 
             * the file will remain in existence until the 
             * last file descriptor referring to it is closed.
             * However, creat() and open() will not be able to 
             * return new file descriptors for the file until 
             * it is deleted.
             */
            /* 
             * TODO: If any processes still have the file open, 
             * the file will remain in existence until the 
             * last file descriptor referring to it is closed.
             * 2/4/2014 HY
             */
             fds[fileHandle].toRemove = true;                             /*@BAA*/  
        }                                                                 /*@BAA*/

        return retval ? 0 : -1;                                           /*@BAA*/  
    }

    /**
     * Terminate the current process immediately. Any open file descriptors
     * belonging to the process are closed. Any children of the process no longer
     * have a parent process.
     *
     * status is returned to the parent process as this process's exit status and
     * can be collected using the join syscall. A process exiting normally should
     * (but is not required to) set status to 0.
     *
     * exit() never returns.
     * 
     * syscall prototype:
     *
     *   void exit(int status);
     *
     */
    /**
     * Procedure*    
     *               
     *   1. close open file descriptors belonging to the process
     *   2. set pid of parent process to null
     *   3. set any children of the process no longer have a parent process(null).
     *   4. set the process's exit status to status that caller specifies(normal) or -1(exception)
     *   5. unloadSections and release memory pages
     *   6. finish associated thread
     *
     */
    private void handleExit(int exitStatus) {                              /*@BCA*/
	    Lib.debug(dbgProcess, "handleExit()");                             /*@BCA*/


        /* close open file descriptors belonging to the process                  */           
        for (int i = 0; i < MAXFD; i++) {                                  /*@BCA*/
            if (fds[i].file != null)                                       /*@BCA*/
                handleClose(i);                                            /*@BCA*/
        }                                                                  /*@BCA*/

        if (this.pid != ROOT) {                                            /*@BCA*/
            /* set pid of parent process to null                                 */
            UserProcess parentProcess =                                    /*@BCA*/
                UserKernel.getProcessByID(this.ppid);                      /*@BCA*/
            this.ppid = 0;                                                 /*@BCA*/
            Iterator<Integer> ts = parentProcess.children.iterator();      /*@BCA*/ 
            while(ts.hasNext()) {                                          /*@BCA*/
                int childpid = ts.next();                                  /*@BCA*/ 
                if (childpid == this.pid) {                                /*@BCA*/
                 ts.remove();                                              /*@BCA*/
                 break;                                                    /*@BCA*/ 
                }                                                          /*@BCA*/ 
            }                                                              /*@BCA*/
        }                                                                  /*@BCA*/ 

        /* set any children of the process no longer have a parent process(null).*/ 
        while (children != null && !children.isEmpty())  {                 /*@BCA*/
            int childPid = children.removeFirst();                         /*@BCA*/ 
            UserProcess childProcess = UserKernel.getProcessByID(childPid);/*@BCA*/
            childProcess.ppid = ROOT;                                      /*@BCA*/
        }                                                                  /*@BCA*/

        /*  set the process's exit status to status that caller specifies(normal)* 
         *  or -1(exception)                                                     */
        this.exitStatus = exitStatus;                                      /*@BCA*/ 

        /* unloadSections and release memory pages                               */
        this.unloadSections();

        /* finish associated thread                                              */
        if (this.pid == ROOT) {
            Kernel.kernel.terminate(); /* Terminate this kernel              @BCA*/
        }                                                                  /*@BCA*/
        else {                                                             /*@BCA*/
            Lib.assertTrue(KThread.currentThread() == this.thread);        /*@BCA*/ 
            KThread.currentThread().finish();                              /*@BCA*/
        }                                                                  /*@BCA*/

        Lib.assertNotReached();                                            /*@BCA*/
    }                                                                      /*@BCA*/


   /**
    * Execute the program stored in the specified file, with the specified
    * arguments, in a new child process. The child process has a new unique
    * process ID, and starts with stdin opened as file descriptor 0, and stdout
    * opened as file descriptor 1.
    *
    * file is a null-terminated string that specifies the name of the file
    * containing the executable. Note that this string must include the ".coff"
    * extension.
    *
    * argc specifies the number of arguments to pass to the child process. This
    * number must be non-negative.
    *
    * argv is an array of pointers to null-terminated strings that represent the
    * arguments to pass to the child process. argv[0] points to the first
    * argument, and argv[argc-1] points to the last argument.
    *
    * exec() returns the child process's process ID, which can be passed to
    * join(). On error, returns -1.
    *
    * syscall prototype:
    *    int  exec(char *name, int argc, char **argv);
    *
    */
    /**
     * Procedure *
     *
     *  + if argc is less than 1
        +   return -1
        +if filename doesn't have the ".coff" extension
        +   return -1;
        get args from address of argv
        create a new process by invoking UserProcess.newUserProcess()
        allocate an unique pid for child process
        * copy file descriptors to the new process. [NOT required in this]
        set new process's parent to this process.
        add new process into this process's children list.
        register this new process in UserKernel
        invoke UserProcess.execute to load executable file and create new UThread
        + If normal, return new process's pid.
        + Otherwise, on error return -1.
    */
    private int handleExec(int file, int argc, int argv) {                  /*@BCA*/
        System.out.println("*** [DEBUG] Inside exec:");               
	    Lib.debug(dbgProcess, "handleExec()");                              /*@BCA*/

        if (argc < 1) {                                                     /*@BCA*/
            Lib.debug(dbgProcess, "handleExec(): argc < 1");                /*@BCA*/
            return -1;                                                      /*@BCA*/
        }                                                                   /*@BCA*/

        String filename = readVirtualMemoryString(file, MAXSTRLEN);         /*@BCA*/
        if (filename == null) {                                             /*@BCA*/
            Lib.debug(dbgProcess, "handleExec(): filename == null");        /*@BCA*/
            return -1;                                                      /*@BCA*/
        }                                                                   /*@BCA*/

        /* filename doesn't have the ".coff" extension                            */
        String suffix =                                                     /*@BCA*/
            filename.substring(filename.length()-4, filename.length());     /*@BCA*/
        if (suffix.equals(".coff")) {                                       /*@BCA*/
            Lib.debug(dbgProcess,                                           /*@BCA*/
              "handleExec(): filename doesn't have the "+coff+" extension");/*@BCA*/
            return -1;                                                      /*@BCA*/
        }                                                                   /*@BCA*/

        /* get args from address of argv                                          */  
        String args[] = new String[argc];                                   /*@BCA*/
        byte   temp[] = new byte[4];                                        /*@BCA*/
        for (int i = 0; i < argc; i++) {                                    /*@BCA*/
            int cntBytes = readVirtualMemory(argv+i*4, temp);               /*@BCA*/
            if (cntBytes != 4) {                                            /*@BCA*/
                return -1;                                                  /*@BCA*/
            }                                                               /*@BCA*/

            int argAddress = Lib.bytesToInt(temp, 0);                       /*@BCA*/
            args[i] = readVirtualMemoryString(argAddress, MAXSTRLEN);       /*@BCA*/
        }                                                                   /*@BCA*/

        /* create a new child process                                       /*@BCA*/
        UserProcess childProcess = UserProcess.newUserProcess();            /*@BCA*/
        childProcess.ppid = this.pid;                                       /*@BCA*/
        this.children.add(childProcess.pid);                                /*@BCA*/

         
        /* invoke UserProcess.execute to load executable and create a new UThread */
        boolean retval = this.execute(filename, args);                      /*@BCA*/

        if (retval) {                                                       /*@BCA*/
            return childProcess.pid;                                        /*@BCA*/    
        }                                                                   /*@BCA*/
        else {                                                              /*@BCA*/
            return -1;                                                      /*@BCA*/
        }                                                                   /*@BCA*/
    }                                                                       /*@BCA*/


    /**
     * Suspend execution of the current process until the child process specified
     * by the processID argument has exited. If the child has already exited by the
     * time of the call, returns immediately. When the current process resumes, it
     * disowns the child process, so that join() cannot be used on that process
     * again.
     *
     * processID is the process ID of the child process, returned by exec().
     *
     * status points to an integer where the exit status of the child process will
     * be stored. This is the value the child passed to exit(). If the child exited
     * because of an unhandled exception, the value stored is not defined.
     *
     * If the child exited normally, returns 1. If the child exited as a result of
     * an unhandled exception, returns 0. If processID does not refer to a child
     * process of the current process, returns -1.
     *
     * prototype:
     *
     *    int  join(int pid, int *status)
     *
     */
    /**
     * Procedure *
     *
     * If processID does not refer to a child process of the current process, 
     *          returns -1.
     *   If the child has already exited by the time of the call,
     *          returns -2.
     *   Else
     *     
     *   Child process's thread joins current thread
     *   Store the exit status of child process to status pointed by the second argument
     *   If the child exited normally, 
     *          returns 1;
     *   Else If the child exited as a result of an unhandled exception, 
     *          returns 0;
     */
    private int handleJoin(int childPid, int adrStatus) {                  /*@BCA*/
	    Lib.debug(dbgProcess, "handleJoin()");                             /*@BCA*/
        
        /* childpid does not refer to a child process of the current process     */
        boolean childFlag = false;                                         /*@BCA*/
        int childpid = 0;                                                  /*@BCA*/
        Iterator<Integer> it = this.children.iterator();                   /*@BCA*/
        while(it.hasNext()) {                                              /*@BCA*/
            childpid = it.next();                                          /*@BCA*/ 
            if (childpid == this.pid) {                                    /*@BCA*/
                childFlag = true;                                          /*@BCA*/
                break;                                                     /*@BCA*/
            }                                                              /*@BCA*/
        }                                                                  /*@BCA*/
                                                                           /*@BCA*/
        if (childFlag == false) {                                          /*@BCA*/
            Lib.debug(dbgProcess,                                          /*@BCA*/ 
                    "not refer to a child process of the current process");/*@BCA*/                         
            return -1;                                                     /*@BCA*/
        }                                                                  /*@BCA*/

        /* the child has already exited by the time of the call                  */   
        UserProcess childProcess = UserKernel.getProcessByID(childpid);    /*@BCA*/
        /* TODO: can't check exit state according to value of ppid [140406]/*@BCA*/          
        if (childProcess.ppid == 0) {                                      /*@BCA*/
            Lib.debug(dbgProcess,                                          /*@BCA*/ 
                 "the child has already exited by the time of the call");  /*@BCA*/                         
            return -2;                                                     /*@BCA*/
        }                                                                  /*@BCA*/
         
        /* child process's thread joins current thread                           */
        childProcess.thread.join();                                        /*@BCA*/ 

        /* store the exit status to status pointed by the second argument        */
        byte temp[] = new byte[4];                                         /*@BCA*/
        temp=Lib.bytesFromInt(childProcess.exitStatus);                    /*@BCA*/
        int cntBytes = writeVirtualMemory(adrStatus, temp);                /*@BCA*/
        if (cntBytes != 4)                                                 /*@BCA*/
            return 1;                                                      /*@BCA*/ 
        else                                                               /*@BCA*/
           return 0;                                                       /*@BCA*/
    }                                                                      /*@BCA*/
     
     
    private static final int
    syscallHalt = 0,
	syscallExit = 1,
	syscallExec = 2,
	syscallJoin = 3,
	syscallCreate = 4,
	syscallOpen = 5,
	syscallRead = 6,
	syscallWrite = 7,
	syscallClose = 8,
	syscallUnlink = 9;

    /**
     * Handle a syscall exception. Called by <tt>handleException()</tt>. The
     * <i>syscall</i> argument identifies which syscall the user executed:
     *
     * <table>
     * <tr><td>syscall#</td><td>syscall prototype</td></tr>
     * <tr><td>0</td><td><tt>void halt();</tt></td></tr>
     * <tr><td>1</td><td><tt>void exit(int status);</tt></td></tr>
     * <tr><td>2</td><td><tt>int  exec(char *name, int argc, char **argv);
     * 								</tt></td></tr>
     * <tr><td>3</td><td><tt>int  join(int pid, int *status);</tt></td></tr>
     * <tr><td>4</td><td><tt>int  creat(char *name);</tt></td></tr>
     * <tr><td>5</td><td><tt>int  open(char *name);</tt></td></tr>
     * <tr><td>6</td><td><tt>int  read(int fd, char *buffer, int size);
     *								</tt></td></tr>
     * <tr><td>7</td><td><tt>int  write(int fd, char *buffer, int size);
     *								</tt></td></tr>
     * <tr><td>8</td><td><tt>int  close(int fd);</tt></td></tr>
     * <tr><td>9</td><td><tt>int  unlink(char *name);</tt></td></tr>
     * </table>
     * 
     * @param	syscall	the syscall number.
     * @param	a0	the first syscall argument.
     * @param	a1	the second syscall argument.
     * @param	a2	the third syscall argument.
     * @param	a3	the fourth syscall argument.
     * @return	the value to be returned to the user.
     */
    public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
	switch (syscall) {
	case syscallHalt:
	    return handleHalt();

    case syscallCreate:
        /* the first argument is filename              @BAA*/
	    return handleCreate(a0); 

    case syscallOpen:
        /* the first argument is filename              @BAA*/
	    return handleOpen(a0);   

    case syscallRead:
        /* the first argument is filename              @BAA*/
        /* the second argument is buf address          @BAA*/
        /* the third argument is buf size              @BAA*/
	    return handleRead(a0, a1, a2); 
         
    case syscallWrite:
        /* the first argument is filename              @BAA*/
        /* the second argument is buf address          @BAA*/
        /* the third argument is buf size              @BAA*/
	    return handleWrite(a0, a1, a2);

    case syscallClose:
        /* the first argument is file handle           @BAA*/
	    return handleClose(a0);

    case syscallUnlink:
        /* the first argument is filename              @BAA*/
	    return handleUnlink(a0);                     /*@BAA*/

    case syscallExit:                                /*@BCA*/
        /* the first argument is specified exit status @BAA*/
	    handleExit(a0);                              /*@BCA*/
        Lib.assertNotReached();                      /*@BCA*/
        return 0;                                    /*@BCA*/           

    case syscallExec:                                /*@BCA*/
	    return handleExec(a0, a1, a2);               /*@BCA*/

    case syscallJoin:                                /*@BCA*/
	    return handleJoin(a0, a1);                   /*@BCA*/

	default:
	    Lib.debug(dbgProcess, "Unknown syscall " + syscall);
	    Lib.assertNotReached("Unknown system call!");
	}

	return 0;
    }

    /**
     * Handle a user exception. Called by
     * <tt>UserKernel.exceptionHandler()</tt>. The
     * <i>cause</i> argument identifies which exception occurred; see the
     * <tt>Processor.exceptionZZZ</tt> constants.
     *
     * @param	cause	the user exception that occurred.
     */
    public void handleException(int cause) {
	Processor processor = Machine.processor();

	switch (cause) {
	case Processor.exceptionSyscall:
	    int result = handleSyscall(processor.readRegister(Processor.regV0),
				       processor.readRegister(Processor.regA0),
				       processor.readRegister(Processor.regA1),
				       processor.readRegister(Processor.regA2),
				       processor.readRegister(Processor.regA3)
				       );
	    processor.writeRegister(Processor.regV0, result);
	    processor.advancePC();
	    break;				       
				       
	default:
	    Lib.debug(dbgProcess, "Unexpected exception: " +
		      Processor.exceptionNames[cause]);
	    Lib.assertNotReached("Unexpected exception");
	}
    }

    /* Find the first empty position in FD array               @BAA */
    private int findEmptyFileDescriptor() {                 /* @BAA */
        for (int i = 0; i < MAXFD; i++) {                   /* @BAA */
            if (fds[i].file == null)                        /* @BAA */
                return i;                                   /* @BAA */
        }                                                   /* @BAA */

        return -1;                                          /* @BAA */
    }                                                       /* @BAA */

    /* Find the first empty position in FD array by filename   @BAA */
    private int findFileDescriptorByName(String filename) { /* @BAA */
        for (int i = 0; i < MAXFD; i++) {                   /* @BAA */
            if (fds[i].filename == filename)                /* @BAA */
                return i;                                   /* @BAA */
        }                                                   /* @BAA */

        return -1;                                          /* @BAA */
    }                                                       /* @BAA */
 
    /** The program being run by this process. */
    protected Coff coff;

    /** This process's page table. */
    protected TranslationEntry[] pageTable;

    /** The number of contiguous pages occupied by the program. */
    protected int numPages;

    /** The number of pages in the program's stack. */
    protected final int stackPages = 8;
    
    private int initialPC, initialSP;
    private int argc, argv;
	
    private static final int pageSize = Processor.pageSize;
    private static final char dbgProcess = 'a';

    /**
     * variables added by hy 1/18/2014 *
     */
    public class FileDescriptor {                                 /*@BAA*/
        public FileDescriptor() {                                 /*@BAA*/ 
        }                                                         /*@BAA*/
        private  String   filename = "";   // opened file name    /*@BAA*/
        private  OpenFile file = null;     // opened file object  /*@BAA*/
        private  int      position = 0;    // IO position         /*@BAA*/

        private  boolean  toRemove = false;// if need to remove   /*@BAA*/
                                           // this file           /*@BAA*/
                                            
    }                                                             /*@BAA*/

    /* maximum number of opened files per process                       */
    public static final int MAXFD = 16;                           /*@BAA*/

    /* standard input file descriptor                                   */
    public static final int STDIN = 0;                            /*@BAA*/ 

    /* standard output file descriptor                                  */
    public static final int STDOUT = 1;                           /*@BAA*/

    /* maximum length of strings passed as arguments to system calls    */
    public static final int MAXSTRLEN = 256;                      /*@BAA*/  

    /* pid of root process(first user process)                          */
    public static final int ROOT = 1;                             /*@BCA*/  

    /* file descriptors per process                                     */
    private FileDescriptor fds[] = new FileDescriptor[MAXFD];     /*@BAA*/   

    /* number of opened files                                           */
    private int cntOpenedFiles = 0;                               /*@BAA*/

    /* process ID                                                       */
    private int pid;                                              /*@BCA*/

    /* parent process's ID                                              */
    private int ppid;                                             /*@BCA*/

    /* child processes                                                  */
    private LinkedList<Integer> children                          /*@BCA*/
                   = new LinkedList<Integer>();                   /*@BCA*/

    /* exit status                                                      */
    private int exitStatus;                                       /*@BCA*/

    /* user thread that's associated with this process                  */
    private UThread thread;                                       /*@BCA*/
                                   
}




