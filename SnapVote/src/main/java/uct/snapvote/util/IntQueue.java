package uct.snapvote.util;

import java.util.NoSuchElementException;

public class IntQueue {

    private int[] data;
    private int startptr, endptr;
    private int numElements;

    public IntQueue(int size)
    {
        data = new int[size];
        numElements = 0;
        startptr = 0;
        endptr = 0;
    }

    public void push(int x)
    {
        data[endptr] = x;

        if (numElements == data.length) throw new IllegalStateException();

        numElements++;
        if (++endptr == data.length) endptr = 0;
    }

    public int pop()
    {
        if (numElements == 0) throw new NoSuchElementException();
        int p = data[startptr++];
        if (startptr == data.length) startptr = 0;
        numElements--;
        return p;
    }

    public boolean isEmpty()
    {
        return numElements == 0;
    }
}
