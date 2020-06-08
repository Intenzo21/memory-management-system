#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>
#include "mymemory.h"

// Main function to run the program
int main()
{
    printf ( "\nshell> start\n");
    initialize(); /* Call the initializer function which initializes the memory and segment table */

    // Place 3 pointers in ptrList which are used to access mymemory content
    ptrList[0] = (char *) mymalloc ( 10 ); /* Allocate memory of size 10 and place a pointer in ptrList to the start of that allocation */
	strcpy (ptrList[0], "this test"); /* Copy the string pointed by the source (second argument) to the character array destination */
    printf( "\nshell> content of allocated memory: %s\n", (char *) ptrList[0] ) ; /* Print the content at the destination to check that it is working. */

    ptrList[1] = (char *) mymalloc ( 10 ) ;
    strcpy (ptrList[1], "this tesT");
    printf( "\nshell> content of allocated memory: %s\n", (char *) ptrList[1] ) ;

    ptrList[2] = (char *) mymalloc ( 10 ) ;
    strcpy (ptrList[2], "that test");
    printf( "\nshell> content of allocated memory: %s\n", (char *) ptrList[2] ) ;

    printmemory() ; /* Print the memory */
    printsegmenttable() ; /* Print the segment table */


    myfree(ptrList[0]); /* Free memory at destination */
    myfree(ptrList[1]); /* Free memory at destination */

    // Put 1 more pointer to allocated memory in ptrList (in this case the pointer should point to the start address of mymemory)
    ptrList[3] = (char *) mymalloc ( 10 ) ;
    strcpy (ptrList[3], "This test");
    printf( "\nshell> content of allocated memory: %s\n", (char *) ptrList[3] ) ;

    mydefrag(ptrList); /* Call for defragmentation of memory */

    printmemory(); /* Print the memory */
    printsegmenttable(); /* Print the segment table */

    int len = sizeof(ptrList) / sizeof(ptrList[0]); /* Length of ptrList (MAXSEGMENTS) */
    for (int i = 0; i < len/15; i++) {
        printf("Pointer %d content value = %s at address = %p\n", i+1, (char *) ptrList[i], ptrList[i]); /* Print content value and address of each pointer in ptrList */
    }

    // Allocate new memory segment
    ptrList[3] = (char *) mymalloc ( 10 ) ;
    strcpy (ptrList[3], "This tesT");
    printf( "\nshell> content of allocated memory: %s\n", (char *) ptrList[3] ) ;

    mydefrag(ptrList); /* Call for defragmentation of memory */

    printmemory(); /* Print the memory */
    printsegmenttable(); /* Print the segment table */

    for (int i = 0; i < len/15; i++) {
        printf("Pointer %d content value = %s at address = %p\n", i+1, (char *) ptrList[i], ptrList[i]); /* Print content value and address of each pointer in ptrList */
    }

    printf ( "\nshell> end\n");
    return 0;
}
