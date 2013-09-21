package uct.snapvote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import uct.snapvote.util.PollListAdapter;
import uct.snapvote.util.PollManager;

public class MainActivity extends Activity {

    private static final int CAMERA_IMAGE_REQUEST_CODE = 101;
    private static final int GALLERY_IMAGE_REQUEST_CODE = 102;

    private ListView listPreviousPolls;
    private PollListAdapter previousPollAdapter;
    private JSONArray previousPolls;

    @Override
    // Activity constructor
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // bind views
        listPreviousPolls = (ListView) findViewById(R.id.listPreviousPolls);
        Button btnNewPoll = (Button) findViewById(R.id.btnNewPoll);

        // setup list of previous polls
        previousPolls = PollManager.getAllPolls(MainActivity.this);
        previousPollAdapter = new PollListAdapter(this, previousPolls);
        listPreviousPolls.setAdapter(previousPollAdapter);
        listPreviousPolls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                launchResultsScreen(id);
            }
        });

        // Setup button intent
        btnNewPoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Input type")
                        .setMessage("Please choose image source")
                        .setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                launchCamera();
                            }
                        }).setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        launchGallery();
                    }
                }).show();
            }
        });
    }

    @Override
    // Inflate the menu; this adds items to the action bar if it is present.
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    // Trigger an event depending on which item is selected in the popup menu
    // item: the selected menu item
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            // if settings button is pressed, launch a settings activity
            case R.id.action_settings:
            {
                Intent intent = new Intent();
                intent.setClass(this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
            // if delete polls button is pressed.. delete polls
            case R.id.action_delete_saves:
            {
                AlertDialog.Builder  yesNoBox = new AlertDialog.Builder(this);
                yesNoBox.setIcon(R.drawable.ic_launcher);
                yesNoBox.setMessage("Are you sure you want to delete all saved polls?");

                // If Yes is pressed, delete polls
                yesNoBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //clear file
                        try {
                            PollManager.clearFile(MainActivity.this);
                        } catch (IOException e) { }

                        //refresh view
                        previousPolls = new JSONArray();
                        previousPollAdapter.rebuild(previousPolls);
                        previousPollAdapter.notifyDataSetChanged();
                        listPreviousPolls.invalidate();

                    }
                });

                // If no was pressed, just continue
                yesNoBox.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { }
                });
                yesNoBox.show();
                break;
            }
        }
        return true;
    }

    // Activity result called when the camera or gallery intent returns from capturing an image
    // requestcode: Gallery or Cammera code
    // resultcode: was the intent performed successfully
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == CAMERA_IMAGE_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                Log.d("SnapVote", "Data: "+data.getData());
                Intent intent = new Intent(this, PreprocessActivity.class);
                intent.putExtra("ImageUri",data.getDataString());
                startActivity(intent);
            }else if(resultCode == RESULT_CANCELED){
                // Do nothing.
            }else{
                Toast.makeText(getApplicationContext(), "Image capture failed.", Toast.LENGTH_LONG).show();
            }
        }
        else if(requestCode == GALLERY_IMAGE_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                Log.d("SnapVote", "Data: "+data.getData());
                Intent intent = new Intent(this, PreprocessActivity.class);
                intent.putExtra("ImageUri",data.getDataString());
                startActivity(intent);
            } else if (resultCode == RESULT_CANCELED) {
                // Do nothing
            } else {
                Toast.makeText(getApplicationContext(), "Gallery intent failed.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // launch a camera intent
    private void launchCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_IMAGE_REQUEST_CODE);
    }

    // launch an intent to select an image
    private void launchGallery(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_IMAGE_REQUEST_CODE);
    }

    // launch a resultActivity instance to show the results of the selected poll.
    // id: index of the selected poll in the list.
    private void launchResultsScreen(long id) {
        try{
            // aquire JSON objects
            JSONObject poll = previousPolls.getJSONObject((int) id);
            Log.d("uct.snapvote", "showing old result: "+poll.toString());
            Intent intent = new Intent(MainActivity.this, ResultActivity.class);
            intent.putExtra("PollResult", poll.toString());
            //start
            startActivity(intent);
        }catch(JSONException e){
            Log.e("uct.snapvote", "Could not launch result screen for selected poll.");
        }
    }

}
