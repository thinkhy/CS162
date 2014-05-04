#!/bin/sh

#java nachos.machine.Machine $*
#java nachos.machine.Machine -x mulprocess.coff -#1 -d ma
java -Dsun.reflect.inflationThreshold=300 nachos.machine.Machine     -x isprmgr.coff   -d as
#java nachos.machine.Machine -x exittest.coff -d ma
#java nachos.machine.Machine -x exectest.coff  -d ma
#java nachos.machine.Machine -x sh.coff  


