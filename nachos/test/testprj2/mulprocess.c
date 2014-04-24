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

#define NUMVARS  7

void route(int, char);

int main(int argc, char *argv[]) { 

/******************************************************************************************
 *
 * Variations for Project 2 Task III: Implement the system calls for process management
 *
 * Var 0 : tests that your syscall exit finishes the thread of the process immediately
 * Var 1 : runs exec multiple times and checks each child gets unique PID
 * Var 2 : tests your syscall join to a child
 * Var 3 : tests exec with error arguments (e.g. bad file name)
 * Var 4 : tests your syscall join to a non-child
 * Var 5 : tests your syscall join to a child that caused unhandled exception
 * Var 6 : tests that your exit syscall releases all resources
 *
 ******************************************************************************************/
    int i;
    int variation;
    char dbg_flag = 'd';

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
            /*                                                           */
            /*************************************************************/


        case 2:
            /*************************************************************/
            /*                                                           */
            /* Variation 1:                                              */
            /*                                                           */
            /*************************************************************/

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
    }













}




