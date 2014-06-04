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
#define STDIN          0
#define STDOUT         1
#define STDERR         2
#define FALSE          0
#define TESTFILE       "testVar1.txt"
#define TESTFILE2      "testVar2.txt"
#define INPUTFILE      "mv.c"
#define VAR7IN         "cp.in"
#define VAR7OUT        "cp.out"
#define OUTPUTFILE     "test.out"
#define MAXRUN         10
#define BUFSIZE        100

void log(char *format, ...);
void route(int, char);

int  retval;                    /* return value of system call                           */
int  fd;                        /* file handle                                           */        
int  exitstatus;                /* exit status of child process                          */
int  flag;                      /* condition variable: TRUE or FALSE                     */
int  i;                         /* loop counter                                          */
int  cnt,tmp;                         
int  fds[MAXOPENFILES];         /* file hadle array                                      */
int  pid;                       /* child process id                                      */

char *executable;               /* executable file name for exec()                       */
char *_argv[MAXARGC];           /* argv for testing executable                           */
int  _argc;                     /* argc for testing executable                           */
char buf[BUFSIZE+1];            /* IO buf for read/write                                 */
char buf2[BUFSIZE+1];           /* The second buf that will be used to compare string    */
char *p;                        /* buf pointer                                           */
int  amount;                    /* amount(byte) per each read/write                      */


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

    LOG("++FILESYSCALL: Start this run\n");
    LOG("++FILESYSCALL: ARG[1] is %d \n", variation);

    if (variation) {
        route(variation, dbg_flag);
    }
    else {
        LOG("++FILESYSCALL Run all the variations\n");
        for (i=0; i <= NUMVARS; i++) {
            LOG("++FILESYSCALL Run the %dth variations\n", i);
            route(i, dbg_flag);
        }
    }
    LOG("++FILESYSCALL End of this run\n");

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
            LOG("++FILESYSCALL VAR2: [STARTED]\n");
            LOG("++FILESYSCALL VAR2: creates a file, checks syscall creat works\n");

            i = 0;
            while( i++ < MAXRUN) { 
            /* while (1) { */
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
            /*                                                             */
            /***************************************************************/
            LOG("++FILESYSCALL VAR4: [STARTED]\n");
            LOG("++FILESYSCALL VAR4: tests if all files get closed when process exits normally\n");
            LOG("++FILESYSCALL VAR4: invoke syscall exec openfile.coff\n");
            executable = "openfile.coff";
            _argv[0] = executable;
            _argv[1] = NULL;
            _argc = 1;
            pid = exec(executable, _argc, _argv);
            if (pid <= 0) {
                LOG("++FILESYSCALL VAR4: failed to exec %s \n", executable); 
                exit(-1);
            }
            LOG("++FILESYSCALL VAR4: Child process id is %d\n", pid);
            
            LOG("++FILESYSCALL VAR4: SUCCESS\n");
            
            break;

        case 5:
            /***************************************************************/
            /*                                                             */
            /* Variation 5:                                                */ 
            /* tests your syscall read by reading some bytes from a file   */
            /*                                                             */
            /***************************************************************/
            LOG("++FILESYSCALL VAR5: [STARTED]\n");
            LOG("++FILESYSCALL VAR5: open %s\n", INPUTFILE);
            fds[0] = open(INPUTFILE);
            if (fds[0] == -1) {
                LOG("++FILESYSCALL: failed to open %s \n", INPUTFILE); 
                exit(-1);
            }

            LOG("++FILESYSCALL VAR5: file handle %d \n", fds[0]);
            LOG("++FILESYSCALL VAR5: invoke read/write in a loop\n");
            while((amount = read(fds[0], buf, BUFSIZE)) > 0) {
                write(1, buf, amount);
            }
            close(fds[0]); 

            LOG("++FILESYSCALL VAR5: Please check above content manually that read from %s\n", INPUTFILE);
            LOG("++FILESYSCALL VAR5: END\n");

            break;
   
        case 6:
            /***************************************************************/
            /*                                                             */
            /* Variation 6:                                                */ 
            /*  tests that each of your processes's file descriptors are   */
            /*   descriptors are independent from other processes          */
            /*                                                             */
            /***************************************************************/
            LOG("++FILESYSCALL VAR6: [STARTED]\n");
            LOG("++FILESYSCALL VAR6: open %s\n", INPUTFILE);
            fds[0] = open(INPUTFILE);
            if (fds[0] == -1) {
                LOG("++FILESYSCALL: failed to open %s \n", INPUTFILE); 
                exit(-1);
            }

            LOG("++FILESYSCALL VAR6: file handle %d \n", fds[0]);

            fds[1] = open(OUTPUTFILE);
            if (fds[1] == -1) {
                LOG("++FILESYSCALL: failed to open %s \n", OUTPUTFILE); 
                exit(-1);
            }

            LOG("++FILESYSCALL VAR6: file handle %d \n", fds[1]);

            LOG("++FILESYSCALL VAR6: invoke read/write in a loop\n");
            while((amount = read(fds[0], buf, BUFSIZE)) > 0) {
                write(STDOUT, buf, amount);
            }

            executable = "openfile.coff";
            _argv[0] = executable;
            _argv[1] = NULL;
            _argc = 1;
            pid = exec(executable, _argc, _argv);
            if (pid <= 0) {
                LOG("++FILESYSCALL VAR6: failed to exec %s \n", executable); 
                exit(-1);
            }
            LOG("++FILESYSCALL VAR6: Child process id is %d\n", pid);

            close(fds[0]);
            close(fds[1]);

            LOG("++FILESYSCALL VAR6: SUCCESS\n");

            break;


        case 7:
            /****************************************************************/
            /*                                                              */
            /* Variation 7:                                                 */ 
            /*  copies between files, tests creat, open, read, write, close */
            /*                                                              */
            /****************************************************************/
            LOG("++FILESYSCALL VAR7: [STARTED]\n");
            LOG("++FILESYSCALL VAR7: copies between files, tests creat, open, read, write, close\n");
            LOG("++FILESYSCALL VAR7: invoke syscall exec cp.coff\n");
            executable = "cp.coff";
            _argv[0] = executable;
            _argv[1] = VAR7IN;
            _argv[2] = VAR7OUT;
            _argc = 3;
            pid = exec(executable, _argc, _argv);
            if (pid <= 0) {
                LOG("++FILESYSCALL VAR7: failed to exec %s \n", executable); 
                exit(-1);
            }

            retval = join(pid, &exitstatus);
            if (retval != 0) {
                LOG("++FILESYSCALL VAR7: failed to exec cp command \n");
                exit(-1);
            }

            p = buf;
            cnt = 0;

            LOG("++FILESYSCALL VAR7: open %s \n", VAR7OUT); 
            fds[0] = open(VAR7OUT);
            if (fds[0] == -1) {
                LOG("++FILESYSCALL VAR7: failed to open %s \n", VAR7OUT); 
                exit(-1);
            }

            LOG("++FILESYSCALL VAR7: read content from %s \n", VAR7OUT); 
            while((amount = read(fds[0], p, 1024)) > 0) {
                p += amount;
                cnt += amount;
            }
            buf[cnt] = '\0';

            LOG("++FILESYSCALL VAR7: open %s \n", VAR7IN); 
            fds[1] = open(VAR7IN);
            if (fds[1] == -1) {
                LOG("++FILESYSCALL VAR7: failed to open %s \n", VAR7IN); 
                exit(-1);
            }

            close(fds[0]);


            p = buf2;
            cnt = 0;
            LOG("++FILESYSCALL VAR7: read content from %s \n", VAR7IN); 
            while((amount = read(fds[1], p, 1024)) > 0) {
                p += amount;
                cnt += amount;
            }
            buf2[cnt] = '\0';

            close(fds[1]);

            buf[BUFSIZE]  = '\0';
            buf2[BUFSIZE] = '\0';

            LOG("++FILESYSCALL VAR7: DST: %s \n", buf); 
            LOG("++FILESYSCALL VAR7: SRC: %s \n", buf2); 

            if (strcmp(buf, buf2) != 0) {
                LOG("++FILESYSCALL VAR7: failed to copy file \n");
                exit(-1);
            }
             
            retval = unlink(VAR7OUT); 
            if (retval == -1) {
                LOG("++FILESYSCALL VAR7: failed to unlink %s \n", VAR7OUT); 
                exit(-1);
            }
                       

            LOG("++FILESYSCALL VAR7: Child process id is %d\n", pid);
            LOG("++FILESYSCALL VAR7: SUCCESS\n");
            
            break;


        case 8:
            /**************************************************************************/
            /*                                                                        */
            /* Variation 8:                                                           */ 
            /*  tests that write fails gracefully on bad arguements (e.g. bad address)*/
            /*                                                                        */
            /**************************************************************************/
            LOG("++FILESYSCALL VAR8: [STARTED]\n");
            LOG("++FILESYSCALL VAR8: open %s\n", INPUTFILE);
            fds[0] = open(INPUTFILE);
            if (fds[0] == -1) {
                LOG("++FILESYSCALL VAR8: failed to open %s \n", INPUTFILE); 
                exit(-1);
            }

            LOG("++FILESYSCALL VAR8: file handle %d \n", fds[0]);

            fds[1] = open(OUTPUTFILE);
            if (fds[1] == -1) {
                LOG("++FILESYSCALL VAR8: failed to open %s \n", OUTPUTFILE); 
                exit(-1);
            }

            LOG("++FILESYSCALL VAR8: file handle %d \n", fds[1]);

            LOG("++FILESYSCALL VAR8: invoke read/write some times\n");

            LOG("++FILESYSCALL VAR8: invoke write as buf address is a negative number\n");
            amount = read(fds[0], buf, BUFSIZE);
            retval = write(fds[1], (void*)(-1), amount);
            if (retval != -1) {
                LOG("++FILESYSCALL VAR8: failed \n");
                exit(-1);
            }

            LOG("++FILESYSCALL VAR8: invoke write as buf address is NULL\n");
            amount = read(fds[0], buf, BUFSIZE);
            retval = write(fds[1], NULL, amount);
            if (retval != amount) {
                LOG("++FILESYSCALL VAR8: failed, expected return value is %d \n", retval);
                exit(-1);
            }
            
            LOG("++FILESYSCALL VAR8: invoke write as amount is ZERO\n");
            amount = read(fds[0], buf, BUFSIZE);
            retval = write(fds[1], buf, 0);
            /* it may reasonable to read zero address */
            if (retval != 0) {
                LOG("++FILESYSCALL VAR8: failed, expected return value is 0 \n");
                exit(-1);
            }

            LOG("++FILESYSCALL VAR8: invoke write as amount is a negative number\n");
            amount = read(fds[0], buf, BUFSIZE);
            retval = write(fds[1], buf, -1);
            /* it may reasonable to read zero address */
            if (retval != -1) {
                LOG("++FILESYSCALL VAR8: failed, expected return value is -1\n");
                exit(-1);
            }

            LOG("++FILESYSCALL VAR8: invoke write with wrong file handle\n");
            amount = read(fds[0], buf, BUFSIZE);
            retval = write(-1, buf, 1);
            if (retval != -1) {
                LOG("++FILESYSCALL VAR8: failed, "
                        "actual return value is %d, "
                        "expected return value is -1 \n", retval);
                exit(-1);
            }

            amount = read(fds[0], buf, BUFSIZE);
            retval = write(11, buf, 1);
            if (retval != -1) {
                LOG("++FILESYSCALL VAR8: failed, "
                        "actual return value is %d, "
                        "expected return value is -1 \n", retval);
                exit(-1);
            }

            close(fds[0]);    
            close(fds[1]);    

            LOG("++FILESYSCALL VAR8: SUCCESS\n");

            break;

        case 9:
            /**************************************************************************/
            /*                                                                        */
            /* Variation 9:                                                           */ 
            /*  tests that read fails gracefully on bad arguments                     */
            /*  (e.g. writing back to a readonly part of virtual memory)              */
            /*                                                                        */
            /**************************************************************************/
            LOG("++FILESYSCALL VAR9: [STARTED]\n");
            LOG("++FILESYSCALL VAR9: open %s\n", INPUTFILE);
            fds[0] = open(INPUTFILE);
            if (fds[0] == -1) {
                LOG("++FILESYSCALL VAR9: failed to open %s \n", INPUTFILE); 
                exit(-1);
            }

            LOG("++FILESYSCALL VAR9: file handle %d \n", fds[0]);

            LOG("++FILESYSCALL VAR9: file handle %d \n", fds[1]);

            LOG("++FILESYSCALL VAR9: invoke read/write some times\n");

            LOG("++FILESYSCALL VAR9: invoke read as buf address is NULL\n");
            amount = read(fds[0], 0, 1024*5);
            if (amount != -1) {
                LOG("++FILESYSCALL VAR9: failed, actual value is %d,"
                      " expected value is %d \n", amount, -1);
                exit(-1);
            }
            
            LOG("++FILESYSCALL VAR9: invoke read as buf address is a negative number\n");
            amount = read(fds[0], buf, -1);
            if (amount != -1) {
                LOG("++FILESYSCALL VAR9: failed \n");
                exit(-1);
            }

            LOG("++FILESYSCALL VAR9: invoke read as bufsize is a negative number\n");
            amount = read(fds[0], buf, -1);
            if (amount != -1) {
                LOG("++FILESYSCALL VAR9: failed \n");
                exit(-1);
            }

            LOG("++FILESYSCALL VAR9: invoke write with wrong file handle\n");
            amount = read(999, buf, BUFSIZE);
            if (amount != -1) {
                LOG("++FILESYSCALL VAR9: failed \n");
                exit(-1);
            }

            close(fds[0]);    

            LOG("++FILESYSCALL VAR9: SUCCESS\n");

            break;



        case 10:
            /**************************************************************************/
            /*                                                                        */
            /* Variation 10:                                                          */ 
            /*  tests that stdin uses console                                         */
            /*                                                                        */
            /**************************************************************************/
            LOG("++FILESYSCALL VAR10: tests that stdin uses console\n");
            LOG("++FILESYSCALL VAR10: [STARTED]\n");

            printf("++FILESYSCALL VAR10: invoke fgetc:\n");
            tmp = fgetc(0);
            fgetc(0);

            /* fgetc(0); */
            printf("\n++FILESYSCALL VAR10: input character is %c\n", tmp);
            printf("++FILESYSCALL VAR10: invoke readline to input a line :\n");
            readline(buf, 80); 

            LOG("++FILESYSCALL VAR10: END\n");

            break;
            



        case 11:
            /**************************************************************************/
            /*                                                                        */
            /* Variation 11:                                                          */ 
            /*  tests stdout                                                          */
            /*                                                                        */
            /**************************************************************************/
            LOG("++FILESYSCALL VAR11: [STARTED]\n");
            strcpy(buf, "TESTTEST");
            write(1, buf, strlen(buf));
            printf("\n++FILESYSCALL VAR11: this number is %d\n", tmp);
            i = 0;
            while(i++ < 10) {
                printf("++FILESYSCALL VAR11: Test stdout\n");
            }

            LOG("++FILESYSCALL VAR11: SUCCESS\n");

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


