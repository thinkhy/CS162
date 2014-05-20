#!/bin/sh

#java nachos.machine.Machine $*
#java nachos.machine.Machine -x mulprocess.coff -#1 -d ma
#java -Dsun.reflect.inflationThreshold=50 nachos.machine.Machine  -x isprmgr.coff   -d as
#java nachos.machine.Machine -x exittest.coff -d ma
#java nachos.machine.Machine -x exectest.coff  -d ma
#java nachos.machine.Machine -x sh.coff  

#######################################################
#
# Test project2-task1
#
#######################################################
rm ../test/out
touch ../test/out

touch ../test/test1.in
touch ../test/test2.in
touch ../test/test3.in

rm ../test/testVar1.txt
java -Dsun.reflect.inflationThreshold=50 nachos.machine.Machine  -x filesyscall.coff   -d as






