package uct.snapvote;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProcessActivity extends Activity {

    String imageUri;
    int[] colourArray;

    TextView tvConsole;
    ProgressBar pbMainProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);

        // first extract uri and required colours
        imageUri = getIntent().getStringExtra("ImageUri");
        colourArray = getIntent().getIntArrayExtra("ColourArray");

        // bind stuff
        tvConsole = (TextView) findViewById(R.id.textView);
        pbMainProgress = (ProgressBar) findViewById(R.id.progressBar);

        // run shit
        SquareDetectionAsyncTask task = new SquareDetectionAsyncTask(this);
        task.execute(imageUri);



    }

    
}
