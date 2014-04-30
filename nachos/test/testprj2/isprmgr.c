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
 **********************************************************************/
#include "../stdio.h"

#define NULL        0
#define NUMVARS     7
#define NAN         (0xEFFFFFFF)
#define MAXARGC     20
#define MAXPROCESS  9
#define LOG         printf

void log(char *format, ...);
void route(int, char);
int  atoi(const char *str);

int pid[10];                    /* array to store pid                                    */
char *executable;               /* executable file name for exec()                       */
char *_argv[MAXARGC];           /* argv for testing executable                           */
int  _argc;                     /* argc for testing executable                           */
int  i,j;                       /* counter for loop                                      */

int main(int argc, char *argv[]) { 

/******************************************************************************************
 *
 * Variations for Project 2 Task III: Implement the system calls for process management
 *
 * Var 1 : tests that your syscall exit finishes the thread of the process immediately
 * Var 2 : runs exec multiple times and checks each child gets unique PID
 * Var 3 : tests your syscall join to a child
 * Var 4 : tests exec with error arguments (e.g. bad file name)
 * Var 5 : tests your syscall join to a non-child
 * Var 6 : tests your syscall join to a child that caused unhandled exception
 * Var 7 : tests that your exit syscall releases all resources
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
            executable = "exittest.coff";
            _argv[0] = executable;
            _argv[1] = NULL;
            _argc = 1;
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
            executable = "cp.coff";
            _argv[0] = executable;
            _argv[1] = "cat.coff";
            _argv[2] = "cat1.coff";
            _argc = 3;

            for (i = 0; i <  MAXPROCESS; i++) {
                // LOG("before");
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
            /* Variation 1:                                              */
            /*                                                           */
            /*************************************************************/

        case 4:
        case 5:
        case 6:
        case 7:
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


