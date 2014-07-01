#!/bin/sh

i=0
while [ $i -lt 1000 ]; do
java -Dsun.reflect.inflationThreshold=0 nachos.machine.Machine  -[] task4.conf
let i=$i+1
done
