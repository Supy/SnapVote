package uct.snapvote;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import uct.snapvote.filter.GuassianTRF;
import uct.snapvote.util.DebugTimer;

/**
 * Created by Ben on 8/4/13.
 */
public class SquareDetectionAsyncTask extends AsyncTask<String, String, Integer> {

    ProcessActivity processActivity;

    public SquareDetectionAsyncTask(ProcessActivity processActivity) {
        this.processActivity = processActivity;
    }

    @Override
    protected Integer doInBackground(String... strings) {

        // hey hey, here we are, the real meat of the problem

        try {

            // 1 - image loading

            ImageByteBuffer grayscale = readAwesomeGrayscale(processActivity.imageUri);


            // blurring
            DebugTimer dbgtimer = new DebugTimer();

            ImageByteBuffer blurred = new ImageByteBuffer(grayscale.getWidth(), grayscale.getHeight());

            publishProgress("1", "B s1 " + dbgtimer.toString());
            dbgtimer.restart();

            GuassianTRF g1 = new GuassianTRF(grayscale, blurred, 2);


            publishProgress("1", "B s2 " + dbgtimer.toString());
            dbgtimer.restart();

            Thread t1 = new Thread(g1);
            t1.start();
            try {
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            publishProgress("1", "B s3 " + dbgtimer.toString());
            dbgtimer.restart();



        } catch (IOException e) {

        }

//        try {
//            for(int i =0;i<100;i++) {
//                publishProgress(1);
//                Thread.sleep(20);
//            }
//        } catch (Exception e) {
//            Log.e("E", e.toString());
//        }


        return 0;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        processActivity.pbMainProgress.incrementProgressBy(Integer.parseInt(values[0]));  // hax ;)
        if (values.length > 1) {
            processActivity.tvConsole.append(values[1] + "\n");
        }
    }

    private ImageByteBuffer readAwesomeGrayscale(String datastr) throws IOException {
        // first get content uri of image on phone
        Uri contentURI = Uri.parse(datastr);
        ContentResolver cr = processActivity.getContentResolver();
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

        publishProgress("1", String.format("dimensions read : width: %d height: %d", imageWidth, imageHeight) );

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

        publishProgress("1", String.format("image loaded : elapsed: %d.%ds", (t2-t1) / 1000, (t2-t1) % 1000) );
        return gbuffer;
    }
}
