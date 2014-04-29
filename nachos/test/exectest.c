/*prolog****************************************************************
 *
 * cname:  exectest.c
 * desc:   invoke exit
 * author: thinkhy
 * tccall: java nachos.machine.Machine -x exectest.coff  -# varnum
 * 
 * env:    nachos 5.0j 
 * compile:test/make
 * Change activity:
 *   $BC,EPT     4/21/2014 - initial release
 **********************************************************************/

#include "../stdio.h"
#include "../stdlib.h"

#define LOG         printf

void main() {
    char* argv[] = {"exittest.coff"};
    LOG("++EXECTEST: STARTED");
    exec("exittest.coff", 1, argv);          
    assert(0);
    LOG("++EXECTEST: ENDED");
}
