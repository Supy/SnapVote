package uct.snapvote;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import uct.snapvote.filter.BaseRegionFilter;
import uct.snapvote.util.DebugTimer;

public class MainActivity extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void loadImage(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, IntCode.LOADIMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntCode.LOADIMAGE && resultCode == RESULT_OK){
            try {

                DebugTimer timer = new DebugTimer();
                ImageByteBuffer grayscale = readAwesomeGrayscale(data.getDataString());

                timer.printout("readawesomegrayscale");

                ImageByteBuffer blurred = new ImageByteBuffer(grayscale.getWidth(), grayscale.getHeight());

                BaseRegionFilter bf = new BaseRegionFilter(grayscale, blurred);
                bf.process();



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
