# GNU Makefile for building user programs to run on top of Nachos
#
# Things to be aware of:
#
#    The value of the ARCHDIR environment variable must be set before using
#	this makefile. If you are using an instructional machine, this should
#	be automatic. However, if you are not using an instructional machine,
#	you need to point ARCHDIR at the cross-compiler directory, e.g.
#		setenv ARCHDIR ../mips-x86.win32-xgcc

# you need to point to the right executables
# HY 1/12/2014  add syscall to test code of syscall
# ##############################################################################
ARCHDIR = ..
GCCDIR = $(ARCHDIR)/mips-x86.linux-xgcc/
#GCCDIR = $(ARCHDIR)/mips-x86.win32-xgcc/

ASFLAGS = -mips1
CPPFLAGS =
CFLAGS = -O2 -B$(GCCDIR) -G 0 -Wa,-mips1 -nostdlib -ffreestanding
LDFLAGS = -s -T script -N -warn-common -warn-constructors -warn-multiple-gp

#CC = $(GCCDIR)gcc
CC = $(GCCDIR)mips-gcc
AS = $(GCCDIR)mips-as
LD = $(GCCDIR)mips-ld
CPP = $(GCCDIR)mips-cpp
AR = $(GCCDIR)mips-ar
RANLIB = $(GCCDIR)mips-ranlib

STDLIB_H = stdio.h stdlib.h ag.h
STDLIB_C = stdio.c stdlib.c
STDLIB_O = start.o stdio.o stdlib.o

LIB = assert atoi printf readline stdio strncmp strcat strcmp strcpy strlen memcpy memset
NLIB = libnachos.a

TARGETS = halt sh matmult sort echo cat cp mv rm syscall test exectest isprmgr filesyscall openfile #chat chatserver

.SECONDARY: $(patsubst %.c,%.o,$(wildcard *.c))

all: $(patsubst %,%.coff,$(TARGETS))

ag: grade-file.coff grade-exec.coff grade-mini.coff grade-dumb.coff

clean:
	rm -f strt.s *.o *.coff $(NLIB)

agclean: clean
	rm -f f1-* f2-*

$(NLIB): $(patsubst %,$(NLIB)(%.o),$(LIB)) start.o
	$(RANLIB) $(NLIB)

start.o: start.s syscall.h
	$(CPP) $(CPPFLAGS) start.s > strt.s
	$(AS) $(ASFLAGS) -o start.o strt.s
	rm strt.s

%.o: %.c *.h
	$(CC) $(CFLAGS) -c $<

%.coff: %.o $(NLIB)
	$(LD) $(LDFLAGS) -o $@ $< start.o -lnachos

filesyscall.coff: ./testprj2/filesyscall.c
	$(CC) $(CFLAGS)  -c $<
	$(LD) $(LDFLAGS) -o filesyscall.coff filesyscall.o start.o -lnachos

test.coff: ./testprj2/exittest.c ./testprj2/loopawhile.c ./testprj2/exception.c ./testprj2/openfile.c
	$(CC) $(CFLAGS)  -c $?
	$(LD) $(LDFLAGS) -o exittest.coff exittest.o start.o -lnachos
	$(LD) $(LDFLAGS) -o loopawhile.coff loopawhile.o start.o -lnachos
	$(LD) $(LDFLAGS) -o exception.coff exception.o start.o -lnachos
	$(LD) $(LDFLAGS) -o openfile.coff openfile.o start.o -lnachos

isprmgr.coff: ./testprj2/isprmgr.c
	$(CC) $(CFLAGS)  -c $<
	$(LD) $(LDFLAGS) -o isprmgr.coff isprmgr.o start.o -lnachos


