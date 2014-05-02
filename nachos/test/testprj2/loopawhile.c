/*prolog******************************************************************************
 *
 * cname:  loopawhile.c
 * desc:   loop lots of time. this program will be exec'ed by test cases in isprmgr.c
 * author: thinkhy
 * tccall: java nachos.machine.Machine -x loopawhile.coff  
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
    for (i = 0; i < LOOPTIME; i++) {
        for (j = 0; j < LOOPTIME; j++) {
            s = s + 100;
        }
    }
}
