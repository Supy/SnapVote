package uct.snapvote.util;

import android.util.Log;

/**
 * Created by Ben on 8/4/13.
 */
public class DebugTimer {
    long starttime, splittime;

    public DebugTimer(){
        starttime = System.currentTimeMillis();
        splittime = starttime;
    }

    public void split() {
        splittime = System.currentTimeMillis();
    }

    long splitMilliseconds()
    {
        return System.currentTimeMillis() - splittime;
    }

    long totalMilliseconds()
    {
        return System.currentTimeMillis() - starttime;
    }


    public String toStringSplit() {
        long elapsed = splitMilliseconds();
        return String.format("%d.%ds", elapsed / 1000 , elapsed % 1000);
    }

    public String toStringTotal() {
        long elapsed = totalMilliseconds();
        return String.format("%d.%ds", elapsed / 1000 , elapsed % 1000);
    }



}
