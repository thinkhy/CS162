# 1 "start.s"
# 1 "<built-in>"
# 1 "<command line>"
# 1 "start.s"
# 10 "start.s"
# 1 "syscall.h" 1
# 11 "start.s" 2

        .text
        .align 2







        .globl __start
        .ent __start
__start:
        jal main
        addu $4,$2,$0
        jal exit
        .end __start

        .globl __main
        .ent __main
__main:
        jr $31
        .end __main
# 57 "start.s"
        .globl halt ; .ent halt ; halt: ; addiu $2,$0,0 ; syscall ; j $31 ; .end halt
        .globl exit ; .ent exit ; exit: ; addiu $2,$0,1 ; syscall ; j $31 ; .end exit
        .globl exec ; .ent exec ; exec: ; addiu $2,$0,2 ; syscall ; j $31 ; .end exec
        .globl join ; .ent join ; join: ; addiu $2,$0,3 ; syscall ; j $31 ; .end join
        .globl creat ; .ent creat ; creat: ; addiu $2,$0,4 ; syscall ; j $31 ; .end creat
        .globl open ; .ent open ; open: ; addiu $2,$0,5 ; syscall ; j $31 ; .end open
        .globl read ; .ent read ; read: ; addiu $2,$0,6 ; syscall ; j $31 ; .end read
        .globl write ; .ent write ; write: ; addiu $2,$0,7 ; syscall ; j $31 ; .end write
        .globl close ; .ent close ; close: ; addiu $2,$0,8 ; syscall ; j $31 ; .end close
        .globl unlink ; .ent unlink ; unlink: ; addiu $2,$0,9 ; syscall ; j $31 ; .end unlink
        .globl mmap ; .ent mmap ; mmap: ; addiu $2,$0,10 ; syscall ; j $31 ; .end mmap
        .globl connect ; .ent connect ; connect: ; addiu $2,$0,11 ; syscall ; j $31 ; .end connect
        .globl accept ; .ent accept ; accept: ; addiu $2,$0,12 ; syscall ; j $31 ; .end accept
