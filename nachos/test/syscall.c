#include "syscall.h"
#include "stdio.h"

#define BUFSIZE 1024

char buf[BUFSIZE];

int main() {

    int fd, amount;

    char *filename = "abc";

    fd = open(filename);

    while((amount = read(fd, buf, BUFSIZE)) > 0) {
        write(1, buf, amount);
    }

    return 0;
} 
