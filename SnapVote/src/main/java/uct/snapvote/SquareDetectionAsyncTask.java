package uct.snapvote;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Guard;

import uct.snapvote.filter.BlobDetectorFilter;
import uct.snapvote.filter.PeakFindTRF;
import uct.snapvote.filter.GaussianTRF;
import uct.snapvote.filter.SobelTRF;
import uct.snapvote.util.DebugTimer;

/**
 * Created by Ben on 8/4/13.
 */
public class SquareDetectionAsyncTask extends AsyncTask<String, String, Integer> {

    ProcessActivity processActivity;
    int gaussianBlurRadius, cannyPeakLow, cannyPeakHigh;

    public SquareDetectionAsyncTask(ProcessActivity processActivity) {
        this.processActivity = processActivity;
    }

    @Override
    protected Integer doInBackground(String... strings) {

        loadPreferences();

        try {

            ImageByteBuffer buffer1 = readGrayscale(processActivity.imageUri);
            ImageByteBuffer buffer2 = new ImageByteBuffer(buffer1.getWidth(), buffer1.getHeight());
            ImageByteBuffer buffer3 = new ImageByteBuffer(buffer1.getWidth(), buffer1.getHeight());

            DebugTimer totalTimer = new DebugTimer();
            DebugTimer debugTimer = new DebugTimer();
            // Gaussian blur
            blur(buffer1, buffer2, gaussianBlurRadius);
            publishProgress("1", "Blur: " + debugTimer.toString()); debugTimer.restart();

            // Sobel filter
            sobelFilter(buffer2, buffer1, buffer3);
            publishProgress("1", "Sobel: " + debugTimer.toString()); debugTimer.restart();

            // Ideally free up limited memory ASAP. Debatable if this helps.
            buffer2 = null;
            System.gc();

            // Canny edge detection
            // In-place editing, don't need another buffer.
            peakFilter(buffer1, buffer3, cannyPeakLow, cannyPeakHigh);
            publishProgress("1", "Peaked: " + debugTimer.toString()); debugTimer.restart();

            buffer3 = null;
            System.gc();

            BlobDetectorFilter bdf = new BlobDetectorFilter(buffer1);
            bdf.run();
            publishProgress("1", "Blob label: " + debugTimer.toString()); debugTimer.restart();
            publishProgress("1", "Total Load & Process Time: " + totalTimer.toString());

            // save to sdcard in order to debug
            Bitmap testImage = buffer1.createBitmap();
            publishProgress("1", "Bitmap: " + debugTimer.toString()); debugTimer.restart();

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            testImage.compress(Bitmap.CompressFormat.JPEG, 80, bytes);
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File f = new File(path, "test.jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
            publishProgress("1", "Save: " + debugTimer.toString()); debugTimer.restart();
            Log.d("uct.snapvote", "Saved image to "+f.getAbsolutePath());

        } catch (IOException e) {

        }
        return 0;
    }

    private void loadPreferences(){
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(processActivity);

        gaussianBlurRadius = Integer.parseInt(mySharedPreferences.getString("gaussian_blur_radius", "2"));
        cannyPeakLow = Integer.parseInt(mySharedPreferences.getString("canny_peak_low", "70"));
        cannyPeakHigh = Integer.parseInt(mySharedPreferences.getString("canny_peak_high", "150"));

        Log.d("uct.snapvote", "Preferences loaded.");
    }

    private ImageByteBuffer readGrayscale(String datastr) throws IOException {
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

        publishProgress("1", String.format("Dimensions read - %dx%d", imageWidth, imageHeight) );

        // reset input stream
        in = cr.openInputStream(contentURI);
        BitmapRegionDecoder regDec = BitmapRegionDecoder.newInstance(in, false);

        int stripHeight = 500;

        int num_layers = (stripHeight > imageHeight) ? 1 : (imageHeight /  stripHeight);
        int rem_layer = imageHeight % stripHeight;
        Bitmap strip;

        BitmapFactory.Options postOptions = new BitmapFactory.Options();
        preOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

        for(int i=0;i<num_layers;i++) {
            Rect r = new Rect(0, i* stripHeight,imageWidth, (i+1)* stripHeight);
            strip= regDec.decodeRegion(r, postOptions);
            int[] pixelData = new int[ stripHeight*imageWidth];
            strip.getPixels(pixelData, 0, imageWidth, 0,0,imageWidth,  stripHeight);

            for(int y=0;y< stripHeight;y++)
                for(int x = 0;x<imageWidth;x++) {
                    int pix = pixelData[y*imageWidth+x];
                    int g = ((pix & 0xFF) + ((pix >> 8) & 0xFF) + ((pix >> 16) & 0xFF))/3;
                    gbuffer.set(x, i* stripHeight+y, (byte)g);
                }
        }
        Rect r = new Rect(0, imageHeight-rem_layer,imageWidth, imageHeight);
        strip = regDec.decodeRegion(r, postOptions);
        int[] pixelData = new int[rem_layer*imageWidth];
        strip.getPixels(pixelData, 0, imageWidth, 0,0,imageWidth, rem_layer);

        for(int y=0;y<rem_layer;y++)
            for(int x = 0;x<imageWidth;x++) {
                int pix = pixelData[y*imageWidth+x];
                int g = ((pix & 0xFF) + ((pix >> 8) & 0xFF) + ((pix >> 16) & 0xFF))/3;
                gbuffer.set(x, imageHeight-rem_layer+y, (byte)g);
            }

        long t2 = System.currentTimeMillis();

        publishProgress("1", String.format("Image loaded time: %d.%ds", (t2-t1) / 1000, (t2-t1) % 1000) );
        return gbuffer;
    }

    private void blur(ImageByteBuffer source, ImageByteBuffer destination, int blurRadius) {
        int bordersize = blurRadius;
        int numthreads = 4;
        int threadwidth = source.getWidth() / numthreads;

        // initialise processors
        GaussianTRF[] trfs = new GaussianTRF[numthreads];
        for(int i=0;i<numthreads;i++)
        {
            int fx = i* threadwidth;
            int tx = fx + threadwidth;
            if (fx==0) fx+=bordersize;
            if (tx >= source.getWidth()-1) tx-= bordersize;
            trfs[i] = new GaussianTRF(source, destination, fx, bordersize, tx, source.getHeight()-bordersize, blurRadius);
        }

        // initialise and start threads
        Thread[] threads = new Thread[numthreads];
        for(int i=0;i<numthreads;i++)
        {
            threads[i] = new Thread(trfs[i]);
            threads[i].start();
        }

        // wait for completion
        for(int i=0;i<numthreads;i++)
        {
            try { threads[i].join(); } catch (InterruptedException e) {  }
        }
    }

    private void sobelFilter(ImageByteBuffer source, ImageByteBuffer destination, ImageByteBuffer dirDataOutput) {
        int halfy = source.getHeight()/2;
        int halfx = source.getWidth()/2;

        SobelTRF g1 = new SobelTRF(source, destination, 2, 2, halfx, halfy, dirDataOutput);
        SobelTRF g2 = new SobelTRF(source, destination, 2, halfy, halfx, source.getHeight()-2, dirDataOutput);
        SobelTRF g3 = new SobelTRF(source, destination, halfx, 2, source.getWidth()-2, halfy, dirDataOutput);
        SobelTRF g4 = new SobelTRF(source, destination, halfx, halfy, source.getWidth()-2, source.getHeight()-2, dirDataOutput);

        Thread t1 = new Thread(g1);
        Thread t2 = new Thread(g2);
        Thread t3 = new Thread(g3);
        Thread t4 = new Thread(g4);

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        // Let's just say blurring is 20% of the total time for now ;)
        try { t1.join(); } catch (InterruptedException e) {  }
        publishProgress("5");
        try { t2.join(); } catch (InterruptedException e) {  }
        publishProgress("5");
        try { t3.join(); } catch (InterruptedException e) {  }
        publishProgress("5");
        try { t4.join(); } catch (InterruptedException e) {  }
        publishProgress("5");
    }

    private void peakFilter(ImageByteBuffer source, ImageByteBuffer dirDataInput, int peakLow, int peakHigh) {
        int halfy = source.getHeight()/2;
        int halfx = source.getWidth()/2;

        boolean[][] peakList = new boolean[source.getHeight()][source.getWidth()];

        PeakFindTRF g1 = new PeakFindTRF(source, 1, 1, halfx, halfy, dirDataInput, peakLow, peakHigh, peakList);
        PeakFindTRF g2 = new PeakFindTRF(source, 1, halfy, halfx, source.getHeight()-1, dirDataInput, peakLow, peakHigh, peakList);
        PeakFindTRF g3 = new PeakFindTRF(source, halfx, 1, source.getWidth()-1, halfy, dirDataInput, peakLow, peakHigh, peakList);
        PeakFindTRF g4 = new PeakFindTRF(source, halfx, halfy, source.getWidth()-1, source.getHeight()-1, dirDataInput, peakLow, peakHigh, peakList);

        Thread t1 = new Thread(g1);
        Thread t2 = new Thread(g2);
        Thread t3 = new Thread(g3);
        Thread t4 = new Thread(g4);

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        try { t1.join(); } catch (InterruptedException e) {  }
        try { t2.join(); } catch (InterruptedException e) {  }
        try { t3.join(); } catch (InterruptedException e) {  }
        try { t4.join(); } catch (InterruptedException e) {  }

        Log.d("uct.snapvote", "Identified first peaks.");

        boolean more;
        do{
            more = false;

            for(int y = 1; y < source.getHeight(); y++){
                for(int x = 1; x < source.getWidth(); x++){
                    boolean peak = peakList[y][x];

                    if(peak){
                        peakList[y][x] = false;

                        for(int p = -1; p < 1; p++){
                            for(int q = -1; q < 1; q++){

                                if(q == 0 && p == 0)
                                    continue;

                                byte neighbour = source.get(x+p, y+q);

                                if(neighbour != 0 && neighbour != (byte) 255){
                                    source.set(x+p, y+q, (byte) 255);
                                    peakList[y+p][x+q] = true;
                                    more = true;
                                }
                            }
                        }
                    }
                }
            }
        }while(more);

        // Clear out remaining potential peaks that have lost their potential ;(
        for(int y = 1; y < source.getHeight()-1; y++){
            for(int x = 1; x < source.getWidth()-1; x++){

                byte pixel = source.get(x, y);

                // Expand any peaks out by 1px to fill small gaps.
                if(pixel == (byte) 255){
                    for(int p = -1; p < 1; p++)
                        for(int q = -1; q < 1; q++){
                            source.set(x+p, y+q, (byte) 255);
                        }
                }else if(pixel != 0){
                    source.set(x, y, (byte) 0);
                }
            }
        }
    }


    @Override
    protected void onProgressUpdate(String... values) {
        processActivity.pbMainProgress.incrementProgressBy(Integer.parseInt(values[0]));  // hax ;)
        if (values.length > 1) {
            processActivity.tvConsole.append(values[1] + "\n");
        }
    }
}
