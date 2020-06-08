/* mymemory.c
 *
 * provides interface to memory management
 *
 */
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>
#include "mymemory.h"


// Our memory
Byte        mymemory [MAXMEM] ;
Segment_t * segmenttable = NULL;

// List of pointers
void * ptrList[MAXSEGMENTS];


// Initialize memory and the segment table (single free segment)
void initialize ()
{
    printf ( "\ninitialize> start\n");

    // Set memory to 0
    memset(mymemory, '\0', MAXMEM);

    // Create segment table that contains one segment description
    // that declares the whole memory as one free segment.
    // Create a single segment descriptor.
    // Initialize the segment.
    segmenttable = malloc(sizeof(Segment_t));
    segmenttable->allocated = FALSE;
    segmenttable->start = mymemory;
    segmenttable->size = MAXMEM;
    segmenttable->next = NULL;

    printf ( "initialize> end\n");
}

// Mymalloc function used to allocate memory in mymemory byte array
void * mymalloc ( size_t size )
{
	Segment_t * freeSegment = findFree(segmenttable, size); /* Segment pointer that points to the free segment */

	printf ( "\nmymalloc> start\n");

	if (freeSegment->size == size) { /* If the free segment size equals the size we are requesting then don't create a new segment for the remaining free memory */
        freeSegment->allocated = TRUE;
        freeSegment->size = size;
	}
	else {

        Segment_t * newSegment = malloc(sizeof(Segment_t)); /* New segment pointer that points to the start of the remaining non-allocated memory */

        // Implement the mymalloc functionality
        newSegment->allocated = FALSE; /* new segment represents the free memory */
        newSegment->start = (freeSegment->start) + size; /* set the start of the new segment */
        newSegment->size = (freeSegment->size) - size; /* set the size of the new segment */

        // Free segment is now allocated and the size is set to the size passed to mymalloc
        freeSegment->allocated = TRUE;
        freeSegment->size = size;
        insertAfter (freeSegment, newSegment); /* inserts the new non-allocated segment after the free one we just found */
	}

	return (void *)(freeSegment->start); /* Return a pointer to the start of the now allocated segment in the memory array */

	printf ( "mymalloc> end\n");
}

// Free allocated memory
void myfree ( void * ptr )
{
    Segment_t * segmentToFree = findSegment(segmenttable, ptr); /* Find the segment to be freed */

    printf ( "\nmyfree> start\n");

    // Deallocate the segment by setting it to 0
    memset(segmentToFree->start, 0, segmentToFree->size);
    segmentToFree->allocated = FALSE;

    printf ( "myfree> end\n");

}

// Defragment memory
void mydefrag ( void ** ptrlist)
{
    Segment_t *curr = segmenttable; /* Pointer to the start of the segment table */

    Segment_t *prev = NULL; /* Pointer to the previous segment (initialized to NULL) */

    size_t deletedSize = 0; /* Holds the size of the current non-allocated segment */
    size_t totalSize = 0; /* Used to hold the total size of newly freed memory */
    int ptrNum = 0; /* Integer used in iterating over the pointer list */

	printf ( "\nmydefrag> start\n");

    while (curr && curr->next) {
        if (!curr->allocated) { /* If not allocated */

            deletedSize = curr->size;  /* Equals the size of the current segment */
            totalSize += deletedSize; /* Incremented if current segment is deleted */

            if (prev) { /* If there is a previous segment */
                prev->next = curr->next; /* Remove segment by linking previous with the next segment */
            }
            else {
                curr->next->start = curr->start; /* The next segment start must equal the current segment start since we are replacing it */
                segmenttable = curr->next; /* Special case - first segment */
            }

            for (int i = 0; i < MAXSEGMENTS; i++){ /* Loop to look for pointer to be nullified */
                if (curr->start == ptrlist[i]) {
                    ptrNum = i;
                    *(ptrlist + ptrNum) = NULL; /* Set deleted segment pointers to NULL */
                    break;
                }
            }

            for (int i = ptrNum; i < MAXSEGMENTS; i++){ /* Loop to adjust the size of remaining pointer in ptrlist */
                ptrlist[i] = ptrlist[i+1];
                if (ptrlist[i]!= mymemory && ptrlist[i]) *(ptrlist + i) -= deletedSize ; /* Adjust start pointer in pointer list (= prev->start can be used instead of -=totalSize) */
            }

            // Shift allocated memory by the deleted deallocated segment size
            for (int k = curr->start - (void*)mymemory ; k < MAXMEM - deletedSize; k++) {
                mymemory[k] = mymemory[k + deletedSize];
            }
        }
        else {
            prev = curr; /* Current now becomes previous segment */
        }
        curr = curr->next; /* Go to next segment */
        if (curr!=segmenttable) curr->start -= totalSize; /* Adjust the new current segment start pointer */
    }
    curr->size += totalSize; /* Add the deallocated free space to the free memory */
    printf ( "mydefrag> end\n\n");
}

// Helper functions for management segmentation table

// Finds non-allocated segment recursively
Segment_t * findFree ( Segment_t * list, size_t size )
{
	Segment_t *curr = list; /* Pointer to the start of the segment table */

	printf ( "\nfindFree> start\n");

	if( (curr->size >= size) && !(curr->allocated) ){ /* If current segment size is larger than or equal to the required size and is not allocated */
		printf("Segment allocated\n");
		return curr; /* Then return the current segment */
    	}

	if (!curr->next) { /* If there is no more segments to check */
		printf("Sorry. No sufficient memory to allocate\n");
		return NULL;
	}
    printf("Moving to next segment...\n");
	return findFree(curr->next, size); /* Move to next segment recursively */
}

// Inserts after a segment
void insertAfter ( Segment_t * oldSegment, Segment_t * newSegment)
{
	newSegment->next = oldSegment->next;
	oldSegment->next = newSegment;
}

// Find a segment recursively
Segment_t * findSegment ( Segment_t * list, void * ptr )
{
    Segment_t *curr = list; /* Pointer to the start of the segment table */

	printf ("\nfindSegment> start\n");

	if( curr->start == ptr ){ /* If current segment start equals the segment start we are looking for */
		printf("Segment found\n");
		return curr; /* Then return the current segment */
    	}

	if (!curr->next) { /* If there is no more segments to check */
		printf("Please provide a valid pointer allocated by mymalloc\n");
		return NULL;
	}

    printf ("Moving to next segment...\n");
	return findSegment(curr->next, ptr); /* Move to next segment recursively */
}

// Check whether character is printable
int isPrintable ( int c )
{
   if ( c >= 0x20 && c <= 0x7e ) return c ;

   return 0 ;
}

// Function to print the memory 10 bytes per line in both hexadecimal and as character
void printmemory ()
{
    // 2 for-loops
    for ( int i = 0 ; i < MAXMEM ; i++) {
        if (i == 0) {
            printf ("\n[%4d] ", i);
        }

        // Do every 10 bytes
        if (i%10 == 0 && i != 0){
            printf(" | ");
            for ( int j = 10; j > 0; j--) {
                if (isPrintable(mymemory[i - j])) {
                    printf("%c", mymemory[i - j]);
                }
                else {
                    printf(".");
                }
            }
            printf("\n[%4d] ", i);
        }
        printf(" %02x ", mymemory[i]);
    }
    printf("\n\n");
}

// Function to print a single segment description
void printsegmentdescriptor ( Segment_t * descriptor )
{
        printf ( "\tallocated = %s\n" , (descriptor->allocated == FALSE ? "FALSE" : "TRUE" ) ) ;
        printf ( "\tstart     = %p\n" , descriptor->start ) ;
        printf ( "\tsize      = %lu\n\n", descriptor->size  ) ;
}

// Function to print the segment table
void printsegmenttable()
{
    // Pointer to the start of the segment table
    Segment_t *curr = segmenttable;

    // Iterate over the segment descriptors and print their content
    for ( int i=0; curr; i++ ) {
            printf ( "\nSegment %d\n", i);
            printsegmentdescriptor(curr);
            curr = curr->next;
    }
}
