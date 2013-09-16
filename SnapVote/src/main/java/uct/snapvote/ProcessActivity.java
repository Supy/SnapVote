package uct.snapvote;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uct.snapvote.util.DetectedSquare;
import uct.snapvote.util.ImageInputStream;

public class ProcessActivity extends Activity {


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
                // TODO : cancel async task properly
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
        if(processingTask != null && !processingTask.isCancelled())
        {
            processingTask.cancel(true);
        }

        // then exit the activity
        ProcessActivity.this.finish();
    }

    public void signalError(Exception e) {
        Log.e("uct.snapvote", e.toString());
        ProcessActivity.this.finish();
    }


    // SUCCESS : Set the results returned by the async task
    public void signalResult(List<DetectedSquare> squareList) {

        // Save the original image with valid votes identified drawn on it,
        saveOverlayImage(squareList);

        // Generate tallies for each colour
        HashMap<Integer, Integer> colourCounts = new HashMap<Integer, Integer>();
        for(DetectedSquare s : squareList) {
            int c = s.Colour();

            if(!colourCounts.containsKey(c)) {
                colourCounts.put(c, 1);
            }else{
                colourCounts.put(c, colourCounts.get(c)+1);
            }
        }

        // Convert map with tallies into a serializable JSON object
        JSONObject pollResult = new JSONObject();
        try{
            JSONArray results = new JSONArray();
            for(Map.Entry<Integer, Integer> entry : colourCounts.entrySet()){
                JSONArray r = new JSONArray();
                r.put(entry.getKey());
                r.put(entry.getValue());
                results.put(r);
            }
            pollResult.put("results", results);
        }catch(JSONException e){
            Log.e("uct.snapvote", "Failed to encode poll result to JSON.");
        }

        // Launch result screen with poll result data.
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("FlagNewPoll", true);
        intent.putExtra("PollResult", pollResult.toString());
        startActivity(intent);
        // close this one
        finish();
    }

    private void saveOverlayImage(List<DetectedSquare> squareList) {
        try {
            ImageInputStream imageInputStream = new ImageInputStream(imageUri, this.getContentResolver());

            int imageHeight = imageInputStream.height;
            int imageWidth = imageInputStream.width;

            // read image attributes
            BitmapFactory.Options iOptions = new BitmapFactory.Options();
            iOptions.inJustDecodeBounds = true;
            iOptions.inPreferredConfig = Bitmap.Config.RGB_565;
            iOptions.inSampleSize = 4;

            Bitmap bm = BitmapFactory.decodeStream(imageInputStream.getInputStream(), null, iOptions);

            android.graphics.Bitmap.Config bitmapConfig = bm.getConfig();
            Bitmap bmcpy = bm.copy(bitmapConfig, true);

            Canvas canvas = new Canvas(bmcpy);

            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL);

            float divx = (float)bm.getWidth() / imageWidth;
            float divy = (float)bm.getHeight() / imageHeight;

            for(DetectedSquare square : squareList) {
                p.setColor(square.Colour());

                int x = (int)(divx * square.Left());
                int y = (int)(divy * square.Top());
                int x2 = (int)(divx * square.Right())+1;
                int y2 = (int)(divy * square.Bottom())+1;

                canvas.drawRect(x,y,x2,y2,p);
            }

            // 7. == Save to sdcard0/Pictures
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bmcpy.compress(Bitmap.CompressFormat.JPEG, 80, bytes);
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File f = new File(path, "test-results.jpg");

            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();

        }catch (Exception e) {
            e.printStackTrace();
            Log.d("uct.snapvote", "Could not save overlay image.");
        }
    }

    
}
