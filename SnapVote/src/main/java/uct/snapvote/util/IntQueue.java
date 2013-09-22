package uct.snapvote.util;

import java.util.NoSuchElementException;

/**
 * Custom queue object for primitive integers. Initialised to a fixed length but allows start and end pointer wrapping.
 */
public class IntQueue {

    // backing data array
    private int[] data;
    // pointers to current start and end
    private int startptr, endptr;
    // number of elements currently in the buffer
    private int numElements;

    /**
     * Constructor
     * @param size Number of positions in the backing array
     */
    public IntQueue(int size)
    {
        data = new int[size];
        numElements = 0;
        startptr = 0;
        endptr = 0;
    }

    /**
     * Push an item onto the back of the queue
     * @param x the value to insert
     */
    public void push(int x)
    {
        data[endptr] = x;

        // invalid state, throw issue
        if (numElements == data.length) throw new IllegalStateException();

        numElements++;
        //wrap end pointer
        if (++endptr == data.length) endptr = 0;
    }

    /**
     * Pop an item from the front of the queue
     * @return The item at the front of the queue
     */
    public int pop()
    {
        if (numElements == 0) throw new NoSuchElementException();
        int p = data[startptr++];
        if (startptr == data.length) startptr = 0;
        numElements--;
        return p;
    }

    /**
     * Check if the queue is empty or not
     * @return True if the queue is empty.
     */
    public boolean isEmpty()
    {
        return numElements == 0;
    }
}
