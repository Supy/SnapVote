package uct.snapvote;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Ben on 8/4/13.
 */
public class SquareDetectionAsyncTask extends AsyncTask<String, Integer, Integer> {

    ProcessActivity processActivity;

    public SquareDetectionAsyncTask(ProcessActivity processActivity) {
        this.processActivity = processActivity;
    }

    @Override
    protected Integer doInBackground(String... strings) {

        try {
            for(int i =0;i<100;i++) {
                publishProgress(1);
                Thread.sleep(20);
            }
        } catch (Exception e) {
            Log.e("E", e.toString());
        }


        return 0;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        processActivity.pbMainProgress.incrementProgressBy(values[0]);
    }
}
