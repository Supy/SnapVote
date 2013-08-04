package uct.snapvote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import uct.snapvote.filter.InvertTRF;
import uct.snapvote.util.DebugTimer;

public class MainActivity extends Activity {

    private static final int CAMERA_IMAGE_REQUEST_CODE = 101;
    private static final int GALLERY_IMAGE_REQUEST_CODE = 102;

    private ListView listPreviousPolls;
    private Button btnNewPoll;

    private static Uri filePath;


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

    protected void onActivityResultDerp(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101 && resultCode == RESULT_OK){
            try {

                DebugTimer timer = new DebugTimer();
                ImageByteBuffer grayscale = readAwesomeGrayscale(data.getDataString());

                timer.printout("readawesomegrayscale");

                ImageByteBuffer blurred = new ImageByteBuffer(grayscale.getWidth(), grayscale.getHeight());

                //BaseRegionFilter bf = new BaseRegionFilter(grayscale, blurred);
                //bf.process();


                int halfy = grayscale.getHeight()/2;


                InvertTRF trf1 = new InvertTRF(grayscale, blurred, 0, 0, grayscale.getWidth(), halfy);
                InvertTRF trf2 = new InvertTRF(grayscale, blurred, 0, halfy, grayscale.getWidth(), halfy);

                Thread ti1 = new Thread(trf1);
                Thread ti2 = new Thread(trf2);

                ti1.start();
                ti2.start();

                try {
                    ti1.join();
                    ti2.join();
                } catch (Exception e) {
                    //lollzies
                }


                timer.printout("basefilter.process");

                Bitmap blueimg = blurred.createBitmap();

                timer.printout("createBitmap()");

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();

                blueimg.compress(Bitmap.CompressFormat.JPEG, 80, bytes);

                File f = new File(Environment.getExternalStorageDirectory() + File.separator + "test.jpg");
                f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());

                fo.close();


                timer.printout("saving bitmap");




            } catch (IOException e) {
                Log.d("main.onActivityResult",e.toString());
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private ImageByteBuffer readAwesomeGrayscale(String datastr) throws IOException {
        // first get content uri of image on phone
        Uri contentURI = Uri.parse(datastr);
        ContentResolver cr = getContentResolver();
        // open file stream
        InputStream in = cr.openInputStream(contentURI);

        // read image attributes
        BitmapFactory.Options preOptions = new BitmapFactory.Options();
        preOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in,null,preOptions);
        int imageHeight = preOptions.outHeight;
        int imageWidth = preOptions.outWidth;


        long t1 = System.currentTimeMillis();

        // hurrah now we know how big our grayscale byte buffer is!
        ImageByteBuffer gbuffer = new ImageByteBuffer(imageWidth, imageHeight);

        Log.d("main.readAwesomeGrayscale","Resolution: " + imageWidth + "x" +imageHeight);

        // reset input stream
        in = cr.openInputStream(contentURI);
        BitmapRegionDecoder regDec = BitmapRegionDecoder.newInstance(in, false);


        int stripheight = 500;  // TODO: this should really be determined by looking at max heap size
        int num_layers = imageHeight / stripheight;
        int rem_layer = imageHeight % stripheight;
        Bitmap stripbit;

        BitmapFactory.Options postOptions = new BitmapFactory.Options();
        preOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

        for(int i=0;i<num_layers;i++) {
            Rect r = new Rect(0, i*stripheight,imageWidth, (i+1)*stripheight);
            stripbit = regDec.decodeRegion(r, postOptions);
            int[] pixdata = new int[stripheight*imageWidth];
            stripbit.getPixels(pixdata, 0, imageWidth, 0,0,imageWidth, stripheight);

            for(int y=0;y<stripheight;y++)
                for(int x = 0;x<imageWidth;x++) {
                    int pix = pixdata[y*imageWidth+x];
                    int g = ((pix & 0xFF) + ((pix >> 8) & 0xFF) + ((pix >> 16) & 0xFF))/3;
                    gbuffer.set(x, i*stripheight+y, (byte)g);
                }
        }
        Rect r = new Rect(0, imageHeight-rem_layer,imageWidth, imageHeight);
        stripbit = regDec.decodeRegion(r, postOptions);
        int[] pixdata = new int[rem_layer*imageWidth];
        stripbit.getPixels(pixdata, 0, imageWidth, 0,0,imageWidth, rem_layer);

        for(int y=0;y<rem_layer;y++)
            for(int x = 0;x<imageWidth;x++) {
                int pix = pixdata[y*imageWidth+x];
                int g = ((pix & 0xFF) + ((pix >> 8) & 0xFF) + ((pix >> 16) & 0xFF))/3;
                gbuffer.set(x, imageHeight-rem_layer+y, (byte)g);
            }

        long t2 = System.currentTimeMillis();

        Log.d("main.readAwesomeGrayscale","elapsed: " + (t2-t1));
        return gbuffer;
    }

}
