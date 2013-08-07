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



    public void restart() {
        starttime = System.currentTimeMillis();
    }

    public void Logd(String name) {

        Log.d("uct.snapvote", "Timer: " + name + " : " + this.toString() );
    }

    public String toString() {
        long elapsed = System.currentTimeMillis() - starttime;
        return String.format("%d.%ds", elapsed / 1000 , elapsed % 1000);
    }



}
