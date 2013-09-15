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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int CAMERA_IMAGE_REQUEST_CODE = 101;
    private static final int GALLERY_IMAGE_REQUEST_CODE = 102;

    private ListView listPreviousPolls;
    private Button btnNewPoll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listPreviousPolls = (ListView) findViewById(R.id.listPreviousPolls);
        btnNewPoll = (Button) findViewById(R.id.btnNewPoll);


        // Setup the previous polls list
        String[] values = new String[] {"How many toes?", "Best path-finding algorithm?"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_2, android.R.id.text1, values);


        listPreviousPolls.setAdapter(adapter);
        listPreviousPolls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), "Clicked.", Toast.LENGTH_SHORT).show();
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
        //intent.putExtra(MediaStore.EXTRA_OUTPUT, getDestinationFilePath());
        startActivityForResult(intent, CAMERA_IMAGE_REQUEST_CODE);
    }

    private void launchGallery(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_IMAGE_REQUEST_CODE);
    }



}
