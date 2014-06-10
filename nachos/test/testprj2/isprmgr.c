/*prolog****************************************************************
 *
 * cname:  isprmgr.c
 * desc:   test cases for CS162 project 2 TaskIII: support multiprogramming
 * author: thinkhy
 * tccall: java nachos.machine.Machine -x isprmgr.coff  
 * 
 * env:    nachos 5.0j 
 * compile:test/make
 * Change activity:
 *   $BC,EPT     4/21/2014 - initial release
 *               5/8/2014  - TODO: add a VAR to exec a program that will fork child
 *   
 **********************************************************************/
#include "../stdio.h"

#define NULL        0
#define NUMVARS     9
#define NAN         (0xEFFFFFFF)
#define MAXARGC     20
#define MAXPROCESS  10
#define LOG         printf
#define TRUE        1
#define FALSE       0

void log(char *format, ...);
void route(int, char);
int  atoi(const char *str);

int  pid[10];                   /* array to store pid                                    */
char *executable;               /* executable file name for exec()                       */
char *_argv[MAXARGC];           /* argv for testing executable                           */
int  _argc;                     /* argc for testing executable                           */
int  i,j;                       /* counter for loop                                      */
int  exitstatus;                /* exit status of child process                          */           
int  retval;                    /* return value of system call                           */
int  flag;                      /* condition variable: TRUE or FALSE                     */



int main(int argc, char *argv[]) { 

/******************************************************************************************
 *
 * Variations for Project 2 Task III: Implement the system calls for process management
 *
 * Var 1 : tests that your syscall exit finishes the thread of the process immediately
 * Var 2 : runs exec multiple times and checks each child gets unique PID
 * Var 3 : tests your syscall join to a child
 * Var 4 : tests exec with error arguments (e.g. bad file name)
 * Var 5:  tests exec with error arguments: unmatched argc
 * Var 6 : tests your syscall join to a non-child
 * Var 7 : tests your syscall join to a child that caused unhandled exception
 * Var 8 : tests that your exit syscall releases all resources
 *
 ******************************************************************************************/
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
            /*************************************************************/
            /*                                                           */
            /* Variation 1:                                              */
            /* tests that your syscall exit finishes the thread of the   */
            /* process immediately.                                      */
            /*                                                           */
            /*************************************************************/
            LOG("++ISPRMGR VAR1: [STARTED]\n");
            LOG("++ISPRMGR VAR1: tests that your syscall exit finishes the thread of the process immediately\n");
            executable = "exittest.coff";
            _argv[0] = executable;
            _argv[1] = NULL;
            _argc = 1;
            LOG("++ISPRMGR VAR1: exec %s\n", executable);
            pid[0] = exec(executable, _argc, _argv);
            LOG("++ISPRMGR VAR1: Child process id is %d\n", pid[0]);

            executable = "loopawhile.coff";
            _argv[0] = executable;
            _argv[1] = NULL;
            _argc = 1;
            LOG("++ISPRMGR VAR1: exec %s\n", executable);
            pid[0] = exec(executable, _argc, _argv);
            LOG("++ISPRMGR VAR1: Child process id is %d\n", pid[0]);
            LOG("++ISPRMGR VAR1: [ENDED] SUCCESS\n");
            /* FIX ME [thinkhy 4/27/2014] */
            /* The second LOG will cause this program to get hung */
            /* LOG("++ProjectII TaskIII VAR1: FAILED"); */
            break;



        case 2:
            /*************************************************************/
            /*                                                           */
            /* Variation 2: runs exec multiple times and checks each     */
            /* child gets unique PID                                     */
            /*                                                           */
            /*************************************************************/
            // log("++ProjectII TaskIII VAR2");
            LOG("++ISPRMGR VAR2: [STARTED]\n");
            LOG("++ISPRMGR VAR2: runs exec multiple times and checks each child gets unique PID\n");
            executable = "cp.coff";
            _argv[0] = executable;
            _argv[1] = "cat.coff";
            _argv[2] = "cat1.coff";
            _argc = 3;

            for (i = 0; i <  MAXPROCESS; i++) {
                // LOG("before");
                LOG("++ISPRMGR VAR2: exec %s\n", executable);
                pid[i] = exec(executable, _argc, _argv);
                LOG("++ISPRMGR VAR2: Get PID %d after exec cp.coff\n", pid[i]);
            
                for (j = 0; j < i; ++j)  {
                    if (pid[j] == pid[i]) {
                        LOG("++ISPRMGR VAR2: FAILED, pid[%d] equals pid[%d]\n",
                                    pid[j], pid[i]);
                        exit(-1);
                    }
                }
            }

            LOG("++ISPRMGR VAR2: [ENDED] SUCCESS\n");

            break;


        case 3:
            /*************************************************************/
            /*                                                           */
            /* Variation 3:  tests your syscall join to a child          */
            /*                                                           */
            /*************************************************************/
            LOG("++ISPRMGR VAR3: [STARTED]\n");
            LOG("++ISPRMGR VAR3: tests your syscall join to a child\n");
            
            executable = "exittest.coff";
            _argv[0] = executable;
            _argv[1] = NULL;
            _argc = 1;
            LOG("++ISPRMGR VAR3: exec %s\n", executable);
            pid[0] = exec(executable, _argc, _argv);
            LOG("++ISPRMGR VAR3: Child process id is %d\n", pid[0]);
            LOG("++ISPRMGR VAR3: Issue join to get exit status of child process\n", pid[0]);
            retval = join(pid[0], &exitstatus);
            if (retval == 0) {
                LOG("++ISPRMGR VAR3: join successfully, exit status is %d\n", exitstatus);
                LOG("++ISPRMGR VAR3: [ENDED] SUCCESS\n");
            }
            else {
                LOG("++ISPRMGR VAR3: return value of join is %d\n", retval);
                LOG("++ISPRMGR VAR3: [ENDED] FAIL\n");
            }
            
            break;

        case 4:
            /*************************************************************************/
            /*                                                                       */
            /* Variation 4:  tests exec with error arguments: bad file name)         */
            /*                                                                       */
            /*************************************************************************/
            LOG("++ISPRMGR VAR4: [STARTED]\n");
            LOG("++ISPRMGR VAR4: tests exec with error arguments: bad file name)\n");
            LOG("++ISPRMGR VAR4: invoke exec with nonextent executable\n");
            executable = "inexistent.coff";
            _argv[0] = executable;
            _argv[1] = NULL;
            _argc = 1; 
            LOG("++ISPRMGR VAR4: exec %s\n", executable);
            retval = exec(executable, _argc, _argv);
            if (retval == -1)
                LOG("++ISPRMGR VAR4: [END] SUCCESS\n");
            else
                LOG("++ISPRMGR VAR4: [END] FAIL\n");

            break;

        case 5:
            /*************************************************************************/
            /*                                                                       */
            /* Variation 5:  tests exec with error arguments: unmatched argc         */
            /*                                                                       */
            /*************************************************************************/
            LOG("++ISPRMGR VAR5: [STARTED]\n");
            LOG("++ISPRMGR VAR5: invoke exec with unmatched argc\n");
            executable = "exittest.coff";
            _argv[0] = executable;
            _argv[1] = NULL;
            _argc = 100; 
            LOG("++ISPRMGR VAR5: exec %s\n", executable);
            retval = exec(executable, _argc, _argv);
            if (retval != -1) {
                LOG("++ISPRMGR VAR5: [END] FAIL\n");
                break;
            }

            /* TODO; figure out this case */
            LOG("++ISPRMGR VAR5:"
                    " invoke exec with unmatched argc when argv varies\n");
            executable = "exittest.coff";
            _argv[0] = executable;
            _argv[1] = "cat.coff";
            _argv[2] = "cat1.coff";
            _argc = 2; 
            LOG("++ISPRMGR VAR5: exec %s\n", executable);
            retval = exec(executable, _argc, _argv);
            if (retval != 0) {
                LOG("++ISPRMGR VAR5: Retval of exec is %d \n", retval);
                LOG("++ISPRMGR VAR5: [END] SUCCESS\n");
            }
            else
                LOG("++ISPRMGR VAR5: [END] FAIL\n");

            break;

        case 6:
        {
            /*************************************************************************/
            /*                                                                       */
            /* Var 6 : tests your syscall join to a non-child                        */
            /*                                                                       */
            /*************************************************************************/
            LOG("++ISPRMGR VAR6: [STARTED]\n");
            LOG("++ISPRMGR VAR6: Issue join to a non-child with pid=0\n");
            
            retval = join(0, &exitstatus);
            if (retval == 0) {
                LOG("++ISPRMGR VAR6: [ENDED] FAIL\n");
                break;
            }

            LOG("++ISPRMGR VAR6: Issue join to myself with pid=1\n");
            retval = join(1, &exitstatus);
            if (retval == 0) {
                LOG("++ISPRMGR VAR6: [ENDED] FAIL\n");
                break;
            }

            LOG("++ISPRMGR VAR6: [ENDED] SUCCESS\n");
        }

        case 7:
            /*************************************************************************/
            /*                                                                       */
            /* Var 7 : tests your syscall join to be invoked more than once          */
            /*                                                                       */
            /*************************************************************************/
            LOG("++ISPRMGR VAR7: [STARTED]\n");
            LOG("++ISPRMGR VAR7: test your syscall join to be invoked more than once\n");
            
            executable = "exittest.coff";
            _argv[0] = executable;
            _argv[1] = NULL;
            _argc = 1;
            LOG("++ISPRMGR VAR7: exec %s\n", executable);
            pid[0] = exec(executable, _argc, _argv);
            LOG("++ISPRMGR VAR7: Child process id is %d\n", pid[0]);
            LOG("++ISPRMGR VAR7: Issue join to get exit status of child process\n");
            retval = join(pid[0], &exitstatus);
            if (retval != 0) {
                LOG("++ISPRMGR VAR7: [ENDED] FAIL\n");
                break;
            }
            LOG("++ISPRMGR VAR7: first time invoke join successfully\n");

            LOG("++ISPRMGR VAR7: Issue join again to get exit status of child process\n", pid[0]);
            retval = join(pid[0], &exitstatus);
            if (retval == 0) {
                LOG("++ISPRMGR VAR7: [ENDED] FAILED to join process %d\n", pid[0]);
                break;
            }
            LOG("++ISPRMGR VAR7: failed to invoke join second time as exptected\n");

            LOG("++ISPRMGR VAR7: [ENDED] SUCCESS\n");

            break;

        case 8:
            /*************************************************************************/
            /*                                                                       */
            /* Var 8 : tests syscall join to a child                                 */   
            /* that caused unhandled exception                                       */
            /*                                                                       */
            /*************************************************************************/
            LOG("++ISPRMGR VAR8: [STARTED]\n");
            LOG("++ISPRMGR VAR8: tests syscall join to a child that caused unhandled exception\n");
            
            executable = "exception.coff";
            _argv[0] = executable;
            _argv[1] = NULL;
            _argc = 1; 
            LOG("++ISPRMGR VAR8: exec %s\n", executable);
            pid[0] = exec(executable, _argc, _argv);
            if (pid[0] < 0) {
                LOG("++ISPRMGR VAR8: [ENDED] FAIL to invoke exec\n");
                break;
            }

            LOG("++ISPRMGR VAR8: Issue join to child with pid=%d\n", pid[0]);
            retval = join(pid[0], &exitstatus);
            if (exitstatus == 0) {
                LOG("++ISPRMGR VAR8: Issue join successfully, but expect a failure at here\n");
                LOG("++ISPRMGR VAR8: [ENDED] FAIL\n");
                break;
            }
            else {
                LOG("++ISPRMGR VAR8: failed to issue join as expected\n");
                LOG("++ISPRMGR VAR8: [ENDED] SUCCESS\n");
            }
           
            break; 

        case 9:
            /*************************************************************************/
            /*                                                                       */
            /* Var 8: tests that your exit syscall releases all resources            */
            /*                                                                       */
            /*************************************************************************/
            /* TODO: it's difficult to write code for this case                      */

            LOG("++ISPRMGR VAR9: [STARTED]\n");
            LOG("++ISPRMGR VAR9: tests that your exit syscall releases all resources\n");


            i = 0;

            while(i++ < MAXPROCESS) {
                executable = "exittest.coff";
                _argv[0] = executable;
                _argv[1] = NULL;
                _argc = 1;
                LOG("++ISPRMGR VAR9: exec %s\n", executable);
                pid[0] = exec(executable, _argc, _argv);
                LOG("++ISPRMGR VAR9: Child process id is %d\n", pid[0]);

                LOG("++ISPRMGR VAR9: Issue join to get exit status of child process\n", pid[0]);
                retval = join(pid[0], &exitstatus);
                if (retval == 0) {
                    LOG("++ISPRMGR VAR9: join successfully, exit status is %d\n", exitstatus);
                }
                else {
                    LOG("++ISPRMGR VAR9: return value of join is %d\n", retval);
                    LOG("++ISPRMGR VAR9: [ENDED] FAIL\n");
                    flag = FALSE;
                    break;
                }
            }

            if (flag) {
                LOG("++ISPRMGR VAR9: [ENDED] SUCCESS\n");
            }

            break;


        default:
            0;
    }


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


