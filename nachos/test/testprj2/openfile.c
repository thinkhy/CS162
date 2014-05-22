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
#define TESTFILE       "test1.in"
#define TESTFILE2      "test2.in"
#define TESTFILE3      "test3.in"

int fds[MAXOPENFILES];               /* Maximum number of  opened files */


int main(int argc, char *argv[]) {
/*****************************************************************************************
 * Test Strategy *
 * 
 * open multiple test files and invoke syscall exit 
 * tests if all files get closed when process exits normally
 *
 *****************************************************************************************/

    LOG("++OPENFILE: [STARTED]\n");

    LOG("++OPENFILE: open the first file %s\n", TESTFILE);
    fds[0] = open(TESTFILE);
    if (fds[0] == -1) {
        LOG("++OPENFILE: failed to open file %s \n", TESTFILE); 
        exit(-1);
    }

    LOG("++OPENFILE: open the second file %s\n", TESTFILE2);
    fds[1] = open(TESTFILE2);
    if (fds[1] == -1) {
        LOG("++OPENFILE: failed to open file %s \n", TESTFILE2); 
        exit(-1);
    }

    LOG("++OPENFILE: open the third file %s\n", TESTFILE3);
    fds[2] = open(TESTFILE3);
    if (fds[2] == -1) {
        LOG("++OPENFILE: failed to open file %s \n", TESTFILE3); 
        exit(-1);
    }

    LOG("++OPENFILE: [END]\n");

    exit(0);
}


