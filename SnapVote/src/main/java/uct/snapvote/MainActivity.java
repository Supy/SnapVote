package uct.snapvote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
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

import java.util.ArrayList;
import java.util.List;

import uct.snapvote.util.PollListAdapter;
import uct.snapvote.util.PollManager;

public class MainActivity extends Activity {

    private static final int CAMERA_IMAGE_REQUEST_CODE = 101;
    private static final int GALLERY_IMAGE_REQUEST_CODE = 102;

    private ListView listPreviousPolls;
    private Button btnNewPoll;
    private JSONArray previousPolls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listPreviousPolls = (ListView) findViewById(R.id.listPreviousPolls);
        btnNewPoll = (Button) findViewById(R.id.btnNewPoll);

        previousPolls = PollManager.getAllPolls(MainActivity.this);

        listPreviousPolls.setAdapter(new PollListAdapter(this, previousPolls));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        intent.setClass(this, SettingsActivity.class);
        startActivity(intent);

        return true;
    }

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

    private void launchCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_IMAGE_REQUEST_CODE);
    }

    private void launchGallery(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_IMAGE_REQUEST_CODE);
    }

    private void launchResultsScreen(long id) {
        try{
            JSONObject poll = previousPolls.getJSONObject((int) id);
            Log.d("uct.snapvote", "showing old result: "+poll.toString());
            Intent intent = new Intent(MainActivity.this, ResultActivity.class);
            intent.putExtra("PollResult", poll.toString());
            startActivity(intent);
        }catch(JSONException e){
            Log.e("uct.snapvote", "Could not launch result screen for selected poll.");
        }
    }

}
