CC=gcc
CFLAGS=-c -Wall
LFLAGS=

all: shell

shell: shell.o mymemory.o
	$(CC) $(LFLAGS) -o shell shell.o mymemory.o

shell.o: shell.c 
	$(CC) $(CFLAGS) shell.c

mymemory.o: mymemory.c mymemory.h
	$(CC) $(CFLAGS) mymemory.c

clean:
	rm -f shell
	rm -f *.o