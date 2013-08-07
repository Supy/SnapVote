package uct.snapvote;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import uct.snapvote.filter.PeakFindTRF;
import uct.snapvote.filter.GaussianTRF;
import uct.snapvote.filter.SobelTRF;
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

            ImageByteBuffer buffer1 = readGrayscale(processActivity.imageUri);
            ImageByteBuffer buffer2 = new ImageByteBuffer(buffer1.getWidth(), buffer1.getHeight());
            ImageByteBuffer buffer3 = new ImageByteBuffer(buffer1.getWidth(), buffer1.getHeight());

            // blurring
            DebugTimer dbgtimer = new DebugTimer();

            blur(buffer1, buffer2, 2);
            publishProgress("1", "Blur: " + dbgtimer.toString()); dbgtimer.restart();

            sobelFilter(buffer2, buffer1, buffer3);

            publishProgress("1", "Sobel: " + dbgtimer.toString()); dbgtimer.restart();
            // use the direction info in B3 to identify peaks in B1. put result in B2
            buffer2 = new ImageByteBuffer(buffer1.getWidth(), buffer1.getHeight());
            peakFilter(buffer1, buffer2, buffer3);

            publishProgress("1", "Peaked: " + dbgtimer.toString()); dbgtimer.restart();

            // save to sdcard in order to debug
            Bitmap testimg = buffer2.createBitmap();
            publishProgress("1", "Bitmap: " + dbgtimer.toString()); dbgtimer.restart();


            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            testimg.compress(Bitmap.CompressFormat.JPEG, 80, bytes);
            File f = new File(Environment.getExternalStorageDirectory() + File.separator + "test.jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
            publishProgress("1", "Save: " + dbgtimer.toString()); dbgtimer.restart();

        } catch (IOException e) {

        }
        return 0;
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

        // Lets allocate 20MB of memory for image loading.
        int maxPixels = 30 * 1024 * 1024 / 4;
        int stripHeight = maxPixels / imageWidth;

        publishProgress("1", String.format("Maximum pixels: %d. Strip height: %d", maxPixels, stripHeight) );

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

    private void blur(ImageByteBuffer buffer1, ImageByteBuffer buffer2, int blurRadius) {
        int halfy = buffer1.getHeight()/2;
        int halfx = buffer1.getWidth()/2;

        GaussianTRF g1 = new GaussianTRF(buffer1, buffer2, blurRadius, blurRadius, halfx, halfy, blurRadius);
        GaussianTRF g2 = new GaussianTRF(buffer1, buffer2, blurRadius, halfy, halfx, buffer1.getHeight()-blurRadius, blurRadius);
        GaussianTRF g3 = new GaussianTRF(buffer1, buffer2, halfx, blurRadius, buffer1.getWidth()-blurRadius, halfy, blurRadius);
        GaussianTRF g4 = new GaussianTRF(buffer1, buffer2, halfx, halfy, buffer1.getWidth()-blurRadius, buffer1.getHeight()-blurRadius, blurRadius);

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

    private void peakFilter(ImageByteBuffer source, ImageByteBuffer destination, ImageByteBuffer dirDataInput) {
        int halfy = source.getHeight()/2;
        int halfx = source.getWidth()/2;

        PeakFindTRF g1 = new PeakFindTRF(source, destination, 2, 2, halfx, halfy, dirDataInput);
        PeakFindTRF g2 = new PeakFindTRF(source, destination, 2, halfy, halfx, source.getHeight()-2, dirDataInput);
        PeakFindTRF g3 = new PeakFindTRF(source, destination, halfx, 2, source.getWidth()-2, halfy, dirDataInput);
        PeakFindTRF g4 = new PeakFindTRF(source, destination, halfx, halfy, source.getWidth()-2, source.getHeight()-2, dirDataInput);

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
    }


    @Override
    protected void onProgressUpdate(String... values) {
        processActivity.pbMainProgress.incrementProgressBy(Integer.parseInt(values[0]));  // hax ;)
        if (values.length > 1) {
            processActivity.tvConsole.append(values[1] + "\n");
        }
    }
}
