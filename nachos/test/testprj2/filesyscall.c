/*prolog***************************************************************************
 *
 * cname:  fssyscal.c
 * desc:   test cases for CS162 project 2 TaskI: Implement the file system calls
 * author: thinkhy
 * tccall: java nachos.machine.Machine -x fssyscal.coff  
 * 
 * env:    nachos 5.0j 
 * compile:test/make
 * Change activity:
 *   $BC,EPT     5/11/2014 - initial release
 *   
 ***********************************************************************************/
#include "../stdio.h"

#define NULL        0
#define NUMVARS     11
#define NAN         (0xEFFFFFFF)
#define MAXARGC     20
#define MAXPROCESS  10
#define LOG         printf
#define TRUE        1
#define FALSE       0

void log(char *format, ...);
void route(int, char);

int  retval;                    /* return value of system call                           */
int  flag;                      /* condition variable: TRUE or FALSE                     */

int main(int argc, char *argv[]) { 
/*****************************************************************************************
 * Test Strategy *
 * Tests file syscalls (creat, open, read, write, close, unlink), as well as stdin/stdout.
 * If anything is printed to the console aside from what is expected, the test will fail.
 *
 *  testID 0 : creates a file, checks syscall creat works
 *  testID 1 : calls syscall creat/close/unlink and checks that they work 
 *  testID 2 : tests if your syscall close actually closes the file
 *  testID 3 : tests if your syscall open fails gracefully when stubFileSystem's openfile limit's exceeded
 *  testID 4 : tests if all files get closed when process exits normally
 *  testID 5 : tests your syscall read by reading some bytes from a file
 *  testID 6 : tests that each of your processes's file descriptors are independent from other processes
 *  testID 7 : copies between files, tests creat, open, read, write, close
 *  testID 8 : tests that write fails gracefully on bad arguements (e.g. bad address)
 *  testID 9 : tests that read fails gracefully on bad arguments (e.g. writing back to a readonly part of virtual memory)
 *  testID 10: tests that stdin uses console
 *  testID 11: tests stdout
 *
 *****************************************************************************************/

    int i;
    int variation = 0;
    char dbg_flag = 'd';

    if(argc > 1)
        variation = atoi(argv[1]);

    LOG("++ISPRMGR: ARG[1] is %d \n", variation);

    if (variation) {
        route(variation, dbg_flag);
    }
    else {
        LOG("++ISPRMGR Run all the variations\n");
        for (i=1; i <= NUMVARS; i++)
            route(i, dbg_flag);
    }

    return 0;
}

void route(int variation, char dbg_flag)   
{

    /******************************************************************/                
    /* Route to the proper variation                                  */
    /******************************************************************/                
    switch (variation)
    {

        case 1:
            break;
        case 2:
            break;
        case 3:
            break;
        case 4:
            break;
        case 5:
            break;
        case 6:
            break;
        case 7:
            break;
        case 8:
            break;
        case 9:
            break;
        case 10:
            break;
        case 11:
            break;
        case 12:
            break;

    default:
        0;
    } /* switch */
}

void log(char *format, ...) {
     va_list ap;
     va_start(ap,format);
 
     vprintf(format, ap);
 
     va_end(ap);
}

int atoi(const char *str) {
    if (str == NULL)
        return NAN;

    int sum = 0;
    int i = 0;
    while(str[i] != NULL) {
        sum = sum*10 + (str[i]-'0');
        i++;
    }

    return sum;
}


