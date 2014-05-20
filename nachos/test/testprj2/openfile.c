/*prolog***************************************************************************
 *
 * cname:  openfile.c
 * desc:   open multiple files. This program will be exec'ed by filesyscall, VAR4
 * author: thinkhy
 * tccall: invoked by parent process(filesyscall.c)
 * 
 * env:    nachos 5.0j 
 * compile:test/make
 * Change activity:
 *   $BC,EPT     5/20/2014 - initial release
 *   
 ***********************************************************************************/

#include "../stdio.h"
#include "../stdlib.h"

#define NULL           0
#define NAN            (0xEFFFFFFF)
#define MAXARGC        20
#define MAXOPENFILES   13             /* MaxOpenFiles=16, 16-3(stdin/stdout/stderr)=13*/
#define LOG            printf
#define TRUE           1
#define FALSE          0
#define TESTFILE       "test1.txt"
#define TESTFILE2      "test2.txt"
#define TESTFILE3      "test3.txt"
#define MAXRUN         10

int main(int argc, char *argv[]) {
/*****************************************************************************************
 * Test Strategy *
 * 
 * open multiple test files and invoke syscall exit 
 * tests if all files get closed when process exits normally
 *
 *****************************************************************************************/

}


