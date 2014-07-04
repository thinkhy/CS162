#!/bin/bash

#!/bin/bash
# Bash Menu Script Example

#######################################################
#
# Test project2-task1
#
#######################################################
test_task1() {
rm ../test/out
touch ../test/out

touch ../test/test1.in
touch ../test/test2.in
touch ../test/test3.in
touch ../test/test.out
echo "0 test #########################################################################################################################################################################################################################################################################################################################################################################################################################################################################">test.in

echo "FileSyscall.c VAR 7 $$ ">../test/cp.in
touch ../test/cp.out

file="../test/testVar1.txt"
if [ -e $file ]; then
    rm $file
fi

file="../test/cp.out"
if [ -e $file ]; then
    rm $file
fi


# For option of sun.reflect.inflationThreshold, it's suggested to set to 0
# Refer to: http://www-01.ibm.com/support/docview.wss?uid=swg21636746

#java -Dsun.reflect.inflationThreshold=50 nachos.machine.Machine  -x ../test..coff   -d as < test.in
java -Dsun.reflect.inflationThreshold=0 nachos.machine.Machine  -x isprmgr.coff>isprmgr.log
}

test_task3() {
    java -Dsun.reflect.inflationThreshold=50 nachos.machine.Machine  -x filesyscall.coff   < test.in > filesyscall.log

    cat filesyscall.log|grep "++FILESYSCALL End of this run"
    if [ $? -ne 0 ]; then
        echo "Failed to test file system call" 
        exit -1
    else
        echo "Test file system call successfully" 
    fi
}

PS3='Please enter your choice: '
options=("Task 1: Basic File syscall"
         "Task 2: Memory storage interfaces"
         "Task 3: Process Management syscall"
         "Task 4: Lottery Scheduler"
         "Quit")
select opt in "${options[@]}"
do
    case $opt in
        "Task 1: Basic File syscall")
            echo "Test Project2-Task1 - Basic File syscall"
            test_task1
            ;;

        "Task 2: Memory storage interfaces")
            echo "Note, Project2-Task2 is tested by other cases"
            ;;

        "Task 3: Process Management syscall")
            echo "Test Project2-Task3 - Process Management syscall"
            test_task3
            ;;

        "Task 4: Lottery Scheduler")
            echo "Test Project2-Task4 - Lottery Scheduler"
            java -Dsun.reflect.inflationThreshold=0 nachos.machine.Machine  -d t -[] task4.conf
            #java -Dsun.reflect.inflationThreshold=0 nachos.machine.Machine  -[] task4.conf
            ;;

        "Quit")
            break
            ;;
        *) echo invalid option;;
    esac
done

#java nachos.machine.Machine $*
#java nachos.machine.Machine -x mulprocess.coff -#1 -d ma
#java -Dsun.reflect.inflationThreshold=50 nachos.machine.Machine  -x isprmgr.coff   -d as
#java nachos.machine.Machine -x exittest.coff -d ma
#java nachos.machine.Machine -x exectest.coff  -d ma
#java nachos.machine.Machine -x sh.coff  



echo "Bye"
exit 0


# 6/13/2014 Reading paper about lottery Scheduling
# 6/14/2014 Reading paper about lottery Scheduling: "Loottery Scheduling: Flexible Proportional-Share Resource Management
# 6/15/2014 Reading paper "Loottery Scheduling: Flexible Proportional-Share Resource Management", almost got it!
# 6/18/2014 Smash my 10k PB at Omlypic Park in Beijing, need to go to sleep now, will work on CS162 tomorrow morning 
# 6/26/2014 Attend a technical meeting, so today I've no time to make a streak ...



