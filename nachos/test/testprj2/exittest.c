/*prolog****************************************************************
 *
 * cname:  exittest.c
 * desc:   invoke syscall exit
 * author: thinkhy
 * tccall: java nachos.machine.Machine -x exittest.coff  
 * 
 * env:    nachos 5.0j 
 * compile:test/make
 * Change activity:
 *   $BC,EPT     4/21/2014 - initial release
 **********************************************************************/
#include "../stdio.h"
#include "../stdlib.h"

#define LOG printf

void main() {
        
    LOG("++TESTEXIT: invoke exit\n");
    exit(0);

    /* Should not get here */
    assert(0);
}
