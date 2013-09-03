package uct.snapvote;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.preference.DialogPreference;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import uct.snapvote.util.DetectedSquare;

public class ProcessActivity extends Activity {

    // SIGNAL VALUES for the async task
    static final int COMPLETE = 0x1000;
    static final int FAILED = 0x1001;

    String imageUri;
    int[] colourArray;

    TextView tvConsole;
    ProgressBar pbMainProgress;

    SquareDetectionAsyncTask processingTask = null;

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

        // start processing
        processingTask = new SquareDetectionAsyncTask(this);
        processingTask.execute(imageUri);

    }

    @Override
    // When back button is pressed, the user may want to cancel processing
    public void onBackPressed() {

        // Create an alert box with Yes / No buttons
        AlertDialog.Builder  yesNoBox = new AlertDialog.Builder(this);
        yesNoBox.setIcon(R.drawable.ic_launcher);
        yesNoBox.setMessage("Are you sure you want to exit image processing?");

        // If Yes is pressed,
        yesNoBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                cancelProcessingAndReturn();
            }
        });

        // If no was pressed, just continue
        yesNoBox.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        yesNoBox.show();
    }

    private void cancelProcessingAndReturn() {
        // cancel the async task if it is running
        if(processingTask == null || !processingTask.isCancelled())
        {
            processingTask.cancel(true);
        }

        // then exit the activity
        ProcessActivity.this.finish();
    }


    // SUCCESS : Set the results returned by the async task
    public void signalResult(List<DetectedSquare> squareList) {

    }

    // FAILURE : Something went wrong
    public void signalFailure() {
        
    }

    
}
