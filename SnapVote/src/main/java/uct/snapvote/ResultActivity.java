package uct.snapvote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import uct.snapvote.components.BarGraph;
import uct.snapvote.util.PollManager;

/**
 * Created by Ben on 2013/09/03.
 */
public class ResultActivity extends Activity {

    // Components
    BarGraph barGraph;
    Button btnSave;
    JSONObject poll;
    String title;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        barGraph = (BarGraph) findViewById(R.id.results_bargraph);
        btnSave = (Button) findViewById(R.id.btnSave);

        String pollData = getIntent().getStringExtra("PollResult");
        Log.d("uct.snapvote", "ResultActivity.OnCreate : polldata : '"+pollData+"'");
        try{
            poll = new JSONObject(pollData);
            // Set correct poll title if it's been saved before
            title = poll.optString("title", "Untitled poll");
            drawGraph();
        }catch(JSONException e){
            Log.e("uct.snapvote", "Error parsing poll data: "+pollData);
        }

        // Disable save button if this isn't a new poll to prevent saving duplicate poll
        if(!getIntent().hasExtra("FlagNewPoll")){
            btnSave.setEnabled(false);
            btnSave.setClickable(false);
            btnSave.setText("Poll already saved.");
            btnSave.getBackground().setColorFilter(new LightingColorFilter(Color.GRAY, Color.DKGRAY));

        }else
            setupSaveButton();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.results, menu);
        return true;
    }

    @Override
    // Trigger an event depending on which item is selected in the popup menu
    // item: the selected menu item
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            // if settings button is pressed, launch a settings activity
            case R.id.action_export:
            {
                //first make sure folder exists
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Polls");
                if (!dir.exists())
                {
                    dir.mkdirs();
                }

                // create filename using date and title
                Date d = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                String fn = String.format("poll.%s.%s.png",barGraph.getTitle(),sdf.format(d));

                File filedir = new File(dir, fn);

                Bitmap bmp = barGraph.getBitmap();

                // save to file
                try {
                    FileOutputStream out = new FileOutputStream(filedir);
                    bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
                    out.close();

                    Toast.makeText(this, "Image saved to " + filedir.getAbsolutePath(), Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    Toast.makeText(this, "Failed to save image to " + filedir.getAbsolutePath() + "!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    // Takes in a poll result in the form of a JSONObject and draws the graph for it.
    private void drawGraph() {
        try{
            JSONArray results = poll.getJSONArray("results");
            for(int i=0; i < results.length(); i++){
                JSONObject result = results.getJSONObject(i);
                int colour = result.getInt("colour");
                int count = result.getInt("count");

                barGraph.addBar(count, getColourName(colour), colour);
            }
            barGraph.setTitle(title);
            barGraph.invalidate();
        }catch(JSONException e){}
    }

    private String getColourName(int colour)
    {
        if (colour == Color.BLACK) return "Black";
        if (colour == Color.GREEN) return "Green";
        if (colour == Color.BLUE) return "Blue";
        if (colour == Color.RED) return "Red";
        return "Unknown";
    }

    private void savePoll(String pollTitle){
        PollManager.saveResult(pollTitle, poll, ResultActivity.this);
    }

    private void setupSaveButton() {
        // Setup prompt for poll title
        final EditText inPollTitle = new EditText(ResultActivity.this);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(ResultActivity.this)
                        .setTitle("Save poll results")
                        .setMessage("Please enter a title for this poll")
                        .setView(inPollTitle)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String title = inPollTitle.getText().toString();

                                // Save the current poll and go back to the main screen
                                savePoll(title);
                                Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
            }
        });
        // --- End prompt setup
    }
}