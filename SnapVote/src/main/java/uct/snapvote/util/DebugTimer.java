package uct.snapvote.util;

/**
 * Simple wrapper around a stopwatch pattern. Provides easy time toString functionality.
 */
public class DebugTimer {
    long starttime, splittime;

    // start timing on construct
    public DebugTimer(){
        starttime = System.currentTimeMillis();
        splittime = starttime;
    }

    // set the split time
    public void split() {
        splittime = System.currentTimeMillis();
    }

    // get milliseconds
    long splitMilliseconds()
    {
        return System.currentTimeMillis() - splittime;
    }

    // get total milliseconds
    long totalMilliseconds()
    {
        return System.currentTimeMillis() - starttime;
    }

    // to string of elapsed time between now and splittime
    public String toStringSplit() {
        long elapsed = splitMilliseconds();
        return String.format("%d.%ds", elapsed / 1000 , elapsed % 1000);
    }

    // to string total elapsed time
    public String toStringTotal() {
        long elapsed = totalMilliseconds();
        return String.format("%d.%ds", elapsed / 1000 , elapsed % 1000);
    }



}
