package uct.snapvote;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import uct.snapvote.filter.BlobDetectorFilter;
import uct.snapvote.filter.PeakFindTRF;
import uct.snapvote.filter.GaussianTRF;
import uct.snapvote.filter.SobelTRF;
import uct.snapvote.filter.ThreadedBaseRegionFilter;
import uct.snapvote.filter.ValidVoteFilter;
import uct.snapvote.util.Blob;
import uct.snapvote.util.DebugTimer;
import uct.snapvote.util.ImageInputStream;
import uct.snapvote.util.DetectedSquare;
import uct.snapvote.util.SobelAngleClassifier;

/**
 * Created by Ben on 8/4/13.
 *
 */
public class SquareDetectionAsyncTask extends AsyncTask<String, String, Integer> {

    ProcessActivity processActivity;
    int gaussianBlurRadius, cannyPeakLow, cannyPeakHigh;

    public SquareDetectionAsyncTask(ProcessActivity processActivity) {
        this.processActivity = processActivity;
    }

    /* doInBackground
    This is the main async task run by the activity. It uses an async task rather than a thread
    because of the built in progress reporting tools and better integration into the android
    environment.
     */
    @Override
    protected Integer doInBackground(String... strings) {

        try {

            // Load configuration values from the settings page
            loadPreferences();
            SobelAngleClassifier.prepare();

            // Start timing
            DebugTimer timer = new DebugTimer();

            // 0. == Read the image into the first buffer
            ImageInputStream imageInputStream = new ImageInputStream(processActivity.imageUri, processActivity.getContentResolver());
            ImageByteBuffer buffer1 = readGrayscale(imageInputStream);
            publishProgress("13", "Image Loaded: " + timer.toStringSplit()); timer.split();

            // Create two additional buffers for processing stages
            ImageByteBuffer buffer2 = new ImageByteBuffer(buffer1.getWidth(), buffer1.getHeight());
            ImageByteBuffer buffer3 = new ImageByteBuffer(buffer1.getWidth(), buffer1.getHeight());


            // 1. == Gaussian blur (buffer1 = input, buffer2 = output)
            blur(buffer1, buffer2, gaussianBlurRadius);
            publishProgress("12", "Blurred: " + timer.toStringSplit()); timer.split();

            // 2. == Sobel filter (buffer2 = input, buffer1 = output, buffer3 = edge angle output)
            sobelFilter(buffer2, buffer1, buffer3);
            publishProgress("7", "Sobel Filter: " + timer.toStringSplit()); timer.split();

            // 3. == Canny edge detection
            BitSet visitedPixels = new BitSet(buffer1.getHeight() * buffer1.getWidth());

            // buffer1 = input, input direction data
            peakFilter(buffer1, buffer3, cannyPeakLow, cannyPeakHigh);  // INPLACE on buffer1


            buffer2 = new ImageByteBuffer(buffer1.getWidth(), buffer1.getHeight());
            runExpansionFilter(buffer1, buffer2, visitedPixels);

            publishProgress("19", "Canny Edge Detection: " + timer.toStringSplit()); timer.split();



            // == Garbage Collection
            buffer1 = null; buffer3 = null;
            System.gc();

            // 4. == Blob Detection
            BlobDetectorFilter bdf = new BlobDetectorFilter(buffer2, visitedPixels, buffer2.getWidth(), buffer2.getHeight());
            bdf.run();
            publishProgress("39", "Blob Detect: " + timer.toStringSplit());timer.split();

            // 5. == Blob Filtering
            ValidVoteFilter vvf = new ValidVoteFilter(bdf.getBlobList(), imageInputStream, buffer2);
            publishProgress("9", "Valid Vote Filter: " + timer.toStringSplit()); timer.split();
            publishProgress("1", "Total Load & Process Time: " + timer.toStringTotal());

            // TODO: Remove the saving when not required
            // 6. == Create output bitmap
            Bitmap testImage = buffer2.createBitmap();
            publishProgress("1", "Created Bitmap");

            // 7. == Save to sdcard0/Pictures
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            testImage.compress(Bitmap.CompressFormat.JPEG, 80, bytes);
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File f = new File(path, "test.jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
            publishProgress("1", "Saved.");

            // 6. Output Data
            List<DetectedSquare> detectedSquares = new ArrayList<DetectedSquare>();

            processActivity.colourArray = new int[]{Color.BLACK, Color.RED, Color.GREEN, Color.BLUE};


            // Convert blobs to coloured squares that will be
            for(Blob b : bdf.getBlobList()) {
                int c = processActivity.colourArray[b.assignedColour];
                detectedSquares.add( new DetectedSquare(b.xMin, b.yMin, b.xMax, b.yMax, c) );
            }

            if(isCancelled()) throw new InterruptedException();

            // Write data to process activity and finish
            processActivity.signalResult(detectedSquares);

            return 0;

        } catch (InterruptedException e) {
            // async task was cancelled
            processActivity.signalError(e);
        } catch (IOException e) {
            // some unexpected error occured
            processActivity.signalError(e);
        }
        return 1;
    }

    private void loadPreferences(){
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(processActivity);

        gaussianBlurRadius = Integer.parseInt(mySharedPreferences.getString("gaussian_blur_radius", "2"));
        cannyPeakLow = Integer.parseInt(mySharedPreferences.getString("canny_peak_low", "50"));
        cannyPeakHigh = Integer.parseInt(mySharedPreferences.getString("canny_peak_high", "100"));
    }

    /* readGrayscale
    Reads the image into a grayscale byte buffer using strip-based loading techniques. Because
    the entire source image can't fit into RAM, strips have to be used to keep this under control.
    note: 500 pixel high strips are used. (unless image is <500px high)
     */
    private ImageByteBuffer readGrayscale(ImageInputStream iis) throws IOException {
        int imageWidth = iis.width;
        int imageHeight = iis.height;

        ImageByteBuffer gbuffer = new ImageByteBuffer(imageWidth, imageHeight);
        publishProgress("1", String.format("Dimensions read - %dx%d", imageWidth, imageHeight) );

        BitmapRegionDecoder regDec = BitmapRegionDecoder.newInstance(iis.getInputStream(), false);

        int stripHeight = 500;

        int num_layers = imageHeight /  stripHeight;
        int rem_layer = imageHeight % stripHeight;

        if (stripHeight > imageHeight)
        {
            num_layers = 0;
            rem_layer = imageHeight;
        }

        Bitmap strip;
        BitmapFactory.Options postOptions = new BitmapFactory.Options();

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

        return gbuffer;
    }

    // Rectangle Struct
    public class Rectangle {
        int x1, y1;
        int x2, y2;
    }

    /* splitRegion
    Split the given region into a number of Rectangles.
    Region is divided like this:

    #######################################
    #######################################
    ##          |         |              ##
    ##          |         |              ##
    ##          |         |              ##
    ##          |         |              ##
    ##  Region0 | Region1 |      ...     ##
    ##          |         |              ##
    ##          |         |              ##
    ##          |         |              ##
    ##          |         |              ##
    #######################################  | border px
    #######################################  /

     */
    private Rectangle[] splitRegion(int width, int height, int num, int border) {
        int subwidth = width / num;
        Rectangle[] o = new Rectangle[num];
        for(int i=0;i<num;i++)
        {
            o[i] = new Rectangle();
            o[i].y1 = border;
            o[i].y2 = height - border;
            o[i].x1 = i * subwidth;
            o[i].x2 = o[i].x1 + subwidth;

            if(i==0) o[i].x1 += border;
            if(i==num-1) o[i].x2 = width - border;
        }
        return o;
    }

    private void runTRFs(ThreadedBaseRegionFilter[] trfs) {
        // initialise and start threads
        Thread[] threads = new Thread[trfs.length];
        for(int i=0;i<trfs.length;i++)
        {
            threads[i] = new Thread(trfs[i]);
            threads[i].start();
        }

        // wait for completion
        for(int i=0;i<trfs.length;i++)
        {
            try { threads[i].join(); } catch (InterruptedException e) {  }
        }
    }

    private void blur(ImageByteBuffer source, ImageByteBuffer destination, int blurRadius) {
        int numthreads = 4;

        Rectangle[] regions = splitRegion(source.getWidth(), source.getHeight(), numthreads, blurRadius);

        GaussianTRF[] trfs = new GaussianTRF[numthreads];
        for(int i=0;i<numthreads;i++)
        {
            trfs[i] = new GaussianTRF(source, destination, regions[i].x1, regions[i].y1, regions[i].x2, regions[i].y2, blurRadius);
        }

        runTRFs(trfs);
    }

    private void sobelFilter(ImageByteBuffer source, ImageByteBuffer destination, ImageByteBuffer dirDataOutput) {
        int numthreads = 4;

        Rectangle[] regions = splitRegion(source.getWidth(), source.getHeight(), numthreads, 1);

        SobelTRF[] trfs = new SobelTRF[numthreads];
        for(int i=0;i<numthreads;i++)
        {
            trfs[i] = new SobelTRF(source, destination, regions[i].x1, regions[i].y1, regions[i].x2, regions[i].y2, dirDataOutput);
        }

        runTRFs(trfs);
    }

    private void peakFilter(ImageByteBuffer source, ImageByteBuffer dirDataInput, int peakLow, int peakHigh) {
        Rectangle[] regions = splitRegion(source.getWidth(), source.getHeight(), 4, 1);

        boolean[][] peakList = new boolean[source.getHeight()][source.getWidth()];

        PeakFindTRF[] trfs = new PeakFindTRF[4];
        for(int i=0;i<4;i++)
        {
            trfs[i] = new PeakFindTRF(source, regions[i].x1, regions[i].y1, regions[i].x2, regions[i].y2, dirDataInput, peakLow, peakHigh, peakList);
        }
        runTRFs(trfs);


        Log.d("uct.snapvote", "Identified first peaks.");

        joinPeaksByPotential(source, peakList);

        Log.d("uct.snapvote", "Joined peaks by potential.");
    }

    private void joinPeaksByPotential(ImageByteBuffer source, boolean[][] peakList) {

        // Turn potential peaks that surround peaks into
        // peaks themselves. Repeat this process until no
        // more peaks are created.
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

                                // Don't mark original peak as peak again
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
    }

    private void runExpansionFilter(ImageByteBuffer source, ImageByteBuffer destination, BitSet visitpixels)
    {
        // TODO: Move this into settings.
        final int MIN_EXPANSION = 1;
        final int MAX_EXPANSION = 3;

        // Clear out remaining potential peaks that have lost their potential ;(
        for(int y = MIN_EXPANSION; y < source.getHeight()-MAX_EXPANSION; y++)
        {

            // Dilate each pixel. Dilation amount increases the nearer to the bottom of the image
            // you are. This is because depth of students is further away at the back.
            int expand = MIN_EXPANSION + (int) (((MAX_EXPANSION - MIN_EXPANSION) *  (y * 1.0 / source.getHeight())) + 0.5);

            for(int x = expand; x < source.getWidth()-expand; x++)
            {
                boolean peak = source.get(x, y) == (byte) 255;

                if(peak)
                {
                    for(int p = -expand; p <= expand; p++)
                    {
                        visitpixels.set((y+p) * source.getWidth() + x - expand, (y+p) * source.getWidth() + x + expand);
                        for(int q = -expand; q <= expand; q++)
                        {
                            destination.set(x+q, y+p, (byte) 60);
                        }
                    }
                }



            }
        }

    }

    private void runErosionFilter(ImageByteBuffer source, ImageByteBuffer destination, BitSet visitpixels)
    {
        // TODO: Move this into settings.
        int MIN_EXPANSION = 1;
        int MAX_EXPANSION = 2;

        // Clear out remaining potential peaks that have lost their potential ;(
        for(int y = MIN_EXPANSION; y < source.getHeight()-MAX_EXPANSION; y++)
        {

            // Dilate each pixel. Dilation amount increases the nearer to the bottom of the image
            // you are. This is because depth of students is further away at the back.
            int expand = MIN_EXPANSION + (int) (((MAX_EXPANSION - MIN_EXPANSION) *  (y * 1.0 / source.getHeight())) + 0.5);

            for(int x = expand+1; x < source.getWidth()-expand-1; x++)
            {
                boolean filled = true;
                for(int p = -expand; p <= expand; p++)
                {
                    for(int q = -expand; q <= expand; q++)
                    {
                        filled = filled && visitpixels.get((y+p) * source.getWidth() + (x+q) - expand);
                    }
                }

                // Expand any peaks out by 1px to fill small gaps.
                if(filled)
                {
                    destination.set(x, y, (byte) 255);
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
