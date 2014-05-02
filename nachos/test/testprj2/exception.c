/*prolog******************************************************************************
 *
 * cname:  loopawhile.c
 * desc:   Make the code to hit an unhandled exception in VM
 * author: thinkhy
 * tccall: java nachos.machine.Machine -x exception.coff  
 * 
 * env:    nachos 5.0j 
 * compile:test/make
 * Change activity:
 *   $BC,EPT     5/2/2014 - initial release
 *************************************************************************************/
#define LOOPTIME (1000L)

void main() {
    int i = 0, j = 0;
    int s = 0;

    s = i / j;
}
