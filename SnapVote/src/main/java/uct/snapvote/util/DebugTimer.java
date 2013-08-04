package uct.snapvote.util;

import android.util.Log;

/**
 * Created by Ben on 8/4/13.
 */
public class DebugTimer {
    long starttime, endtime;

    public DebugTimer(){
        starttime = System.currentTimeMillis();
    }

    public void printout(String name) {
        long elapsed = System.currentTimeMillis() - starttime;
        Log.d("dbgtime", String.format("Timer: %s : %d.%ds", name, elapsed / 1000 , elapsed % 1000));
        starttime = System.currentTimeMillis();
    }


}
