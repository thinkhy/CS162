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
#include "../stdlib.h"

#define NULL           0
#define NUMVARS        11
#define NAN            (0xEFFFFFFF)
#define MAXARGC        20
#define MAXPROCESS     10
#define MAXOPENFILES   13             /* MaxOpenFiles=16, 16-3(stdin/stdout/stderr)=13*/
#define LOG            printf
#define TRUE           1
#define FALSE          0
#define TESTFILE       "testVar1.txt"
#define TESTFILE2      "testVar2.txt"

void log(char *format, ...);
void route(int, char);

int  retval;                    /* return value of system call                           */
int  fd;                        /* file handle                                           */        
int  flag;                      /* condition variable: TRUE or FALSE                     */
int  i;                         /* loop counter                                          */
int  fds[MAXOPENFILES];         /* file hadle array                                      */

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
        for (i=0; i <= NUMVARS; i++) {
            LOG("++ISPRMGR Run the %dth variations\n", i);
            route(i, dbg_flag);
        }
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

        case 0:
            /***********************************************************/
            /*                                                         */
            /* Variation 0:                                            */ 
            /* creates a file, checks syscall creat works              */ 
            /*                                                         */
            /***********************************************************/
            LOG("++FILESYSCALL VAR0: [STARTED]\n");
            LOG("++FILESYSCALL VAR0: creates a file, checks syscall creat works\n");
            retval = creat(TESTFILE);
            if (retval == -1) {
                LOG("++FILESYSCALL VAR0: Failed to create %s \n", TESTFILE);
                exit(-1);
            }
            close(retval);
            LOG("++FILESYSCALL VAR0: SUCCESS\n");

            break;

        case 1:
            /**************************************************************/
            /*                                                            */
            /* Variation 1:                                               */ 
            /* calls syscall creat/close/unlink and checks that they work */ 
            /*                                                            */
            /**************************************************************/
            LOG("++FILESYSCALL VAR1: [STARTED]\n");
            LOG("++FILESYSCALL VAR1: calls syscall creat/close/unlink and checks that they work\n");
            LOG("++FILESYSCALL VAR1: calls syscall creat to create file %s\n",
                    TESTFILE2);
            fd  = creat(TESTFILE2);
            if (fd == -1) {
                LOG("++FILESYSCALL VAR1: Failed to create %s \n", TESTFILE2);
                exit(-1);
            }

            LOG("++FILESYSCALL VAR1: calls syscall close\n");
            close(fd);

            LOG("++FILESYSCALL VAR1: calls syscall unlink to delete %s\n",
                    TESTFILE2);
            retval = unlink(TESTFILE2);
            if (retval == -1) {
                LOG("++FILESYSCALL VAR1: Failed to delete %s \n", TESTFILE2);
                exit(-1);
            }
            LOG("++FILESYSCALL VAR1: calls syscall creat again to create file %s\n",
                    TESTFILE2);
            fd  = creat(TESTFILE2);
            if (fd == -1) {
                LOG("++FILESYSCALL VAR1: Failed to create %s \n", TESTFILE2);
                exit(-1);
            }
            LOG("++FILESYSCALL VAR1: calls syscall unlink to delete %s without close\n",
                    TESTFILE2);
            retval = unlink(TESTFILE2);
            if (retval == -1) {
                LOG("++FILESYSCALL VAR1: Failed to delete %s \n", TESTFILE2);
                exit(-1);
            }

            LOG("++FILESYSCALL VAR1: calls syscall unlink again to see if %s is existed.\n",
                    TESTFILE2);
            retval = unlink(TESTFILE2);
            if (retval != -1) {
                LOG("++FILESYSCALL VAR1: %s should be deleted by last call of unlink \n", 
                        TESTFILE2);
                exit(-1);
            }

            LOG("++FILESYSCALL VAR1: SUCCESS\n");
             
            break;
             
        case 2:
            /***********************************************************/
            /*                                                         */
            /* Variation 2:                                            */ 
            /*  tests if your syscall close actually closes the file   */
            /*                                                         */
            /***********************************************************/
            LOG("++FILESYSCALL VAR2: tests if your syscall close actually closes the file");
            /* TODO */
            LOG("++FILESYSCALL VAR2: [STARTED]\n");
            LOG("++FILESYSCALL VAR2: creates a file, checks syscall creat works\n");
            while(1) {
            retval = creat(TESTFILE);
            if (retval == -1) {
                LOG("++FILESYSCALL VAR2: Failed to create %s \n", TESTFILE);
                exit(-1);
            }
            LOG("++FILESYSCALL VAR2: close the file created just now\n");
            close(retval);

            LOG("++FILESYSCALL VAR2: open the file created just now\n");
            fd = open(TESTFILE);

            /* TODO: write some content to test file */

            LOG("++FILESYSCALL VAR2: close the file just opened\n");
            close(retval);

            LOG("++FILESYSCALL VAR2: calls syscall unlink to delete %s\n",
                    TESTFILE);
            retval = unlink(TESTFILE);
            if (retval == -1) {
                LOG("++FILESYSCALL VAR2: Failed to delete %s \n", TESTFILE2);
                exit(-1);
            }
            }


            LOG("++FILESYSCALL VAR2: SUCCESS\n");

            break;

        case 3:
            /***********************************************************/
            /*                                                         */
            /* Variation 3:                                            */ 
            /*  tests if your syscall open fails gracefully            */
            /*  when stubFileSystem's openfile limit's exceeded        */
            /*                                                         */
            /***********************************************************/
             
            LOG("++FILESYSCALL VAR3: [STARTED]\n");
            LOG("++FILESYSCALL VAR3: tests if your syscall open fails gracefully when stubFileSystem's openfile limit's exceeded\n");
            LOG("++FILESYSCALL VAR3: will open %d files\n", MAXOPENFILES);
            for (i = 0; i <= MAXOPENFILES; i++) {
                LOG("++FILESYSCALL VAR3: opening the %dth file\n", i);
                fds[i] = open("out"); 
                if (fds[i] == -1) {
                    LOG("++FILESYSCALL VAR3: failed to open file \"out\" \n"); 
                    exit(-1);
                }
            }

            for (i = 0; i <= MAXOPENFILES; i++) {
                close(fds[i]);
            }
             
            LOG("++FILESYSCALL VAR3: SUCCESS\n");
             
            break;
              
        case 4:
            /***************************************************************/
            /*                                                             */
            /* Variation 4:                                                */ 
            /* tests if all files get closed when process exits normally   */
            /*  when stubFileSystem's openfile limit's exceeded            */
            /*                                                             */
            /***************************************************************/
            
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
            assert(0);;
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


