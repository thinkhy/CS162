/**
 * Test the nachos system call interfaces. these are nachos kernel operations that
 * can be invoked from user programs using the syscall instruction.
 * 
 * this interface is derived from the unix syscalls.
 */
#include "syscall.h"
#include "stdio.h"

int main() {
            
    /* test syscall create */
    char *filename = "abc";
    creat(filename);
    open(filename);
    /* printf("%d\n",fh); */

    return 0;
}
