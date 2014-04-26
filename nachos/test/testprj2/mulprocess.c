/*prolog****************************************************************
 *
 * cname:  mulprocess.c
 * desc:   test cases for CS162 project 2: support multiprogramming
 * author: thinkhy
 * tccall: java nachos.machine.Machine -x mulprocess.coff  -# varnum
 * k
 * env:    nachos 5.0j 
 * compile:test/make
 * Change activity:
 *   $BC,EPT     4/21/2014 - initial release
 **********************************************************************/

#define NULL     0
#define NUMVARS  7
#define NAN      (0xEFFFFFFF)

void route(int, char);
int atoi(const char *str);

int pid[10];                    /* Array to store pid                                    */

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

    if (variation) {
        route(variation, dbg_flag);
    }
    else {
        for (i=1; i <= NUMVARS; i++)
            route(variation, dbg_flag);
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
            exit(1);
            exit(0);



        case 2:
            /*************************************************************/
            /*                                                           */
            /* Variation 2: runs exec multiple times and checks each     */
            /* child gets unique PID                                     */
            /*                                                           */
            /*************************************************************/
            pid[0] = exec("../test/exit");
            


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


