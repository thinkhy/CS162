Phase 2: Multiprogramming
=============================

Overview
-----------

The second phase of Nachos is to support multiprogramming. As in the first assignment, we give you some of the code you need; your job is to complete the system and enhance it.

Setup
-----------

  * Step 1 
    
  > to read and understand the part of the system we have written for you
    - UserKernel.java - a multiprogramming kernel.
    - UserProcess.java - a user process; manages the address space, and loads a program into virtual memory.
    - UThread.java - a thread capable of executing user MIPS code.
    - SynchConsole.java - a synchronized console; makes it possible to share the machine's serial console among multiple threads.
    
  * This test program is found in test/halt.c and represents the simplest supported MIPS program.

  > The test directory includes C source files (.c files) and Nachos user program binaries (.coff files). The binaries can be built while in the test directory by running gmake, or from the proj2 directory by running gmake test.

  * To run the halt program, go to the test directory and gmake; then go to the proj2 directory, gmake, and run nachos `-d ma`. Trace what happens as the user program gets loaded, runs, and invokes a system call (the 'm' debug flag enables MIPS disassembly, and the 'a' debug flag prints process loading information).

  * There are multiple stages to building a Nachos-compatible MIPS binary (all of which are handled by the test Makefile):

    1. Source files (*.c) are compiled into object files (*.o) by mips-gcc.
    2. Some of the object files are linked into libnachos.a, the Nachos standard library.
    3. start.s is preprocessed and assembled into start.o. This file contains the assembly-language code to initialize a process. It also provides the system call "stub code" which allows system calls to be invoked. 
    4. An object file is linked with libnachos.a to produce a Nachos-compatible MIPS binary, which has the extension *.coff. (COFF stands for Common Object File Format and is an industry-standard binary format which the Nachos kernel understands.)
    5. Note that if you create a new test file (*.c), you will need to append your program name to the variable TARGETS in the Makefile inside test directory

  * You can run other test programs by running
      
    `nachos -x PROGNAME.coff`
      
    where PROGNAME.coff is the name of the MIPS program binary in the test directory. 


Task I: Implement the file system calls
------------------------------------------

(30%, 125 lines) Implement the file system calls (creat, open, read, write, close, and unlink, documented in syscall.h). You will see the code for halt in UserProcess.java; it is best for you to place your new system calls here too. Note that you are not implementing a file system; rather, you are simply giving user processes the ability to access a file system that we have implemented for you.

   * We have provided you the assembly code necessary to invoke system calls from user programs (see start.s; the SYSCALLSTUB macro generates assembly code for each syscall).

   * You will need to bullet-proof the Nachos kernel from user program errors; there should be nothing a user program can do to crash the operating system (with the exception of explicitly invoking the halt() syscall). In other words, **you must be sure that user programs do not pass bogus arguments to the kernel which causes the kernel to corrupt its internal state or that of other processes.** Also, you must take steps to ensure that if a user process does anything illegal -- such as attempting to access unmapped memory or jumping to a bad address -- that the process will be killed cleanly and its resources freed.

   * You should make it so that the halt() system call can only be invoked by the "root" process -- that is, the first process in the system. **If another process attempts to invoke halt(), the system call should be ignored and return immediately.**

   * Since the memory addresses passed as arguments to the system calls are virtual addresses, you need to **use UserProcess.readVirtualMemory and UserProcess.writeVirtualMemory to transfer memory between the user process and the kernel** .

   * User processes **store filenames and other string arguments as null-terminated strings in their virtual address space**. The maximum length of for strings passed as arguments to system calls is 256 bytes.

   * When a system call wishes to **indicate an error condition to the user, it should return -1 (not throw an exception within the kernel!)**. Otherwise, the system call should return the appropriate value as documented in test/syscall.h.

   * When any process is started, **its file descriptors 0 and 1 must refer to standard input and standard output.** Use UserKernel.console.openForReading() and UserKernel.console.openForWriting() to make this easier. A user process is allowed to close these descriptors, just like descriptors returned by open().

   * A stub file system interface to the UNIX file system is already provided for you; the interface is given by the class machine/FileSystem.java. You can access the stub filesystem through the static field ThreadedKernel.fileSystem. (Note that since UserKernel extends ThreadedKernel, you can still access this field.) This filesystem is capable of accessing the test directory in your Nachos distribution, which is going to be useful when you want to support the exec system call (see below). You do not need to implement any file system functionality. **You should examine carefully the specifications for FileSystem and StubFileSystem in order to determine what functionality you should provide in your syscalls, and what is handled by the file system.**

   * **Do not implement any kind of file locking; this is the file system's responsibility.** If ThreadedKernel.fileSystem.open() returns a non-null OpenFile, then the user process is allowed to access the given file; otherwise, you should signal an error. Likewise, you do not need to worry about the details of what happens if multiple processes attempt to access the same file at once; the stub filesystem handles these details for you.

   * Your implementation should **support up to 16 concurrently open files per process, including stdin and stdout.** Each file that a process has opened should have a unique file descriptor associated with it (see syscall.h for details). The file descriptor should be a non-negative integer that is simply used to index into a table of currently-open files by that process. Note that a given file descriptor can be reused if the file associated with it is closed, and that different processes can use the same file descriptor (i.e. integer) to refer to different files.


**System Calls**

  * creat

   > Attempt to open the named disk file, creating it if it does not exist,
   > and return a file descriptor that can be used to access the file.
   >
   > Note that creat() can only be used to create files on disk; creat() will
   > never return a file descriptor referring to a stream.
   >
   > Returns the new file descriptor, or -1 if an error occurred.


  * open

   > Attempt to open the named file and return a file descriptor.
   >
   > Note that open() can only be used to open files on disk; open() will never
   > return a file descriptor referring to a stream.
   >
   > Returns the new file descriptor, or -1 if an error occurred.


  * read
   
   > Attempt to read up to count bytes into buffer from the file or stream
   > referred to by fileDescriptor.
   >
   > On success, the number of bytes read is returned. If the file descriptor
   > refers to a file on disk, the file position is advanced by this number.
   >
   > It is not necessarily an error if this number is smaller than the number of
   > bytes requested. If the file descriptor refers to a file on disk, this
   > indicates that the end of the file has been reached. If the file descriptor
   > refers to a stream, this indicates that the fewer bytes are actually
   > available right now than were requested, but more bytes may become available
   > in the future. Note that read() never waits for a stream to have more data;
   > it always returns as much as possible immediately.
   >
   > On error, -1 is returned, and the new file position is undefined. This can
   > happen if fileDescriptor is invalid, if part of the buffer is read-only or
   > invalid, or if a network stream has been terminated by the remote host and
   > no more data is available.


  * write
    
   > Attempt to write up to count bytes from buffer to the file or stream
   > referred to by fileDescriptor. write() can return before the bytes are
   > actually flushed to the file or stream. A write to a stream can block,
   > however, if kernel queues are temporarily full.
   >
   > On success, the number of bytes written is returned (zero indicates nothing
   > was written), and the file position is advanced by this number. It IS an
   > error if this number is smaller than the number of bytes requested. For
   > disk files, this indicates that the disk is full. For streams, this
   > indicates the stream was terminated by the remote host before all the data
   > was transferred.
   >
   > On error, -1 is returned, and the new file position is undefined. This can
   > happen if fileDescriptor is invalid, if part of the buffer is invalid, or
   > if a network stream has already been terminated by the remote host.


  * close

   > Close a file descriptor, so that it no longer refers to any file or stream
   > and may be reused.
   >
   > If the file descriptor refers to a file, all data written to it by write()
   > will be flushed to disk before close() returns.
   > If the file descriptor refers to a stream, all data written to it by write()
   > will eventually be flushed (unless the stream is terminated remotely), but
   > not necessarily before close() returns.
   >
   > The resources associated with the file descriptor are released. If the
   > descriptor is the last reference to a disk file which has been removed using
   > unlink, the file is deleted (this detail is handled by the file system
   > implementation).
   >
   > Returns 0 on success, or -1 if an error occurred.


  * unlink,
    
   > Delete a file from the file system. If no processes have the file open, the
   > file is deleted immediately and the space it was using is made available for
   > reuse.
   >
   > If any processes still have the file open, the file will remain in existence
   > until the last file descriptor referring to it is closed. However, creat()
   > and open() will not be able to return new file descriptors for the file
   > until it is deleted.
   >
   > Returns 0 on success, or -1 if an error occurred.
    

Task II: Implement support for multiprogramming
----------------------------------------------------
    
(25%, 100 lines) Implement support for multiprogramming. The code we have given you is restricted to running one user process at a time; your job is to make it work for multiple user processes.

Come up with a way of allocating the machine's physical memory so that different processes do not overlap in their memory usage. Note that **the user programs do not make use of malloc() or free(), meaning that user programs effectively have no dynamic memory allocation needs (and therefore, no heap).** What this means is that you know the complete memory needs of a process when it is created. You can allocate a fixed number of pages for the processe's stack; 8 pages should be sufficient.

We suggest **maintaining a global linked list of free physical pages (perhaps as part of the UserKernel class).**  Be sure to use synchronization where necessary when accessing this list. Your solution must make efficient use of memory by allocating pages for the new process wherever possible. This means that it is not acceptable to only allocate pages in a contiguous block; **your solution must be able to make use of "gaps" in the free memory pool.**

Also be sure that **all of a process's memory is freed on exit** (whether it exits normally, via the syscall exit(), or abnormally, due to an illegal operation).

Modify UserProcess.readVirtualMemory and UserProcess.writeVirtualMemory, which copy data between the kernel and the user's virtual address space, so that they work with multiple user processes.

The physical memory of the MIPS machine is accessed through the method **Machine.processor().getMemory()**; the total number of physical pages is **Machine.processor().getNumPhysPages()**. You should maintain the pageTable for each user process, which maps the user's virtual addresses to physical addresses. The TranslationEntry class represents a single virtual-to-physical page translation.

**The field TranslationEntry.readOnly should be set to true if the page is coming from a COFF section which is marked as read-only.** You can determine this using the method CoffSection.isReadOnly().

Note that **these methods should not throw exceptions when they fail; instead, they must always return the number of bytes transferred (even if that number is zero).**

**Modify UserProcess.loadSections() so that it allocates the number of pages that it needs** (that is, based on the size of the user program), using the allocation policy that you decided upon above. This method should also set up the pageTable structure for the process so that the process is loaded into the correct physical memory pages. If the new user process cannot fit into physical memory, exec() should return an error.

Note that the user threads (see the UThread class) already save and restore user machine state, as well as process state, on context switches. So, you are not responsible for these details.


**TODO**
   * We suggest maintaining a global linked list of free physical pages (perhaps as part of the UserKernel class). Be sure to use synchronization where necessary when accessing this list.
   * Modify UserProcess.readVirtualMemory and UserProcess.writeVirtualMemory
   * Modify UserProcess.loadSections() so that it allocates the number of pages that it needs  
   * The field TranslationEntry.readOnly should be set to true if the page is coming from a COFF section which is marked as read-only.    
   * You should maintain the pageTable for each user process, which maps the user's virtual addresses to physical addresses. 


   * You can allocate a fixed number of pages for the processe's stack; 8 pages should be sufficient.    

      /** The number of pages in the program's stack. */
      protected final int stackPages = 8;


