package uct.snapvote.filter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import uct.snapvote.util.Blob;
import uct.snapvote.util.BlobSampler;
import uct.snapvote.util.ImageInputStream;

/**
 * Valid Vote filter operates on a list of detected regions and is used to identify and classify coloured
 * squares on white backgrounds.
 * 4 steps are involved:
 * 1 - remove blobs that are too small
 * 2 - construct a list of pixels samples for the sample colouriser
 * 3 - colourise the samples
 * 4 - use the coloured samples in order to classify the square
 */
public class ValidVoteFilter{

    private List<Blob> blobList;
    private ImageInputStream imageInputStream;
    private int imageWidth;
    private int imageHeight;

    private PriorityQueue<BlobSampler.Sample> pixelSamples;

    /**
     * Constructor and processor
     * The algorithm is run after the constructor is complete.
     * @param blobList The detected list of regions
     * @param imageInputStream An ImageInputStream for the source image. Used for sizes and colourising
     */
    public ValidVoteFilter(List<Blob> blobList, ImageInputStream imageInputStream){
        this.blobList = blobList;
        this.imageInputStream = imageInputStream;
        this.imageWidth = imageInputStream.width;
        this.imageHeight = imageInputStream.height;

        try{
            removeInvalidSizedBlobs();
            getPixelSamples();
            colourizeSamples();
            classifyBlobs();
        }catch(IOException e){
            Log.d("uct.snapvote", "Could not colourize pixel samples: "+e.getMessage());
        }
    }

    /**
     * Discard blobs that fall below the required size for their vertical coordinate
     */
    private void removeInvalidSizedBlobs()
    {
        // in and out lists
        List<Blob> before = blobList;
        List<Blob> after = new ArrayList<Blob>();
        for (Blob blob : before) {
            // height of the blob
            int height = blob.yMax - blob.yMin;
            // center y coordinate
            int yCenter = (blob.yMax + blob.yMin) / 2;

            // minimum height for specific y coordinate
            int minH = (int) (((float) yCenter / imageHeight) * 30);

            // only add it if it passes the test
            if (height > minH) after.add(blob);
        }
        blobList = after;
    }

    /**
     * Construct a list of samples using each of the blobs in the blob list.
     */
    private void getPixelSamples(){
        pixelSamples = BlobSampler.createSamples(blobList, imageWidth, imageHeight);
        Log.d("uct.snapvote", pixelSamples.size()+" blob samples created.");
    }

    /**
     * Take pixel samples and colourise them for the classifier.
     * Load strips of the image and add colour to all of the pixel samples that fall in
     * the strip.
     * @throws IOException if image could not be loaded
     */
    private void colourizeSamples() throws IOException {
        // strip decoder
        BitmapRegionDecoder regDec = BitmapRegionDecoder.newInstance(imageInputStream.getInputStream(), false);

        Bitmap strip;
        int[] pixelData = new int[0];
        BitmapFactory.Options postOptions = new BitmapFactory.Options();

        int currentRow = 0;
        int maxPixelIndexInRow = imageWidth-1;

        // keep reading until all pixel samples are done
        while(!pixelSamples.isEmpty()){
            BlobSampler.Sample sample = pixelSamples.poll();

            if(sample.pixelIndex > maxPixelIndexInRow){
                while(sample.pixelIndex > maxPixelIndexInRow){
                    currentRow++;   // Don't need to decode this row as we don't have any samples in it.
                    maxPixelIndexInRow = (currentRow + 1) * imageWidth - 1;
                }

                // Otherwise we decode rows in the image that have samples in them, row by row.
                Rect r = new Rect(0, currentRow, imageWidth, currentRow+1);
                strip = regDec.decodeRegion(r, postOptions);
                pixelData = new int[imageWidth];
                strip.getPixels(pixelData, 0, imageWidth, 0,0,imageWidth,  1);
            }

            int sampleX = sample.pixelIndex % imageWidth;

            sample.colour = pixelData[sampleX];
        }
    }

    /**
     * The remaining blobs have been sampled and have a square shape of the right size.
     * This method classifies the blobs by discarding some or assigning a colour and counting them.
     */
    private void classifyBlobs(){
        int correctBlobs = 0;
        int reds =0, blues=0, greens=0, blacks=0;

        // loop through all of the blobs
        for(int i = blobList.size()-1; i >= 0; i--){

            // if is is not valid, remote it from the list
            Blob blob = blobList.get(i);
            if(!classifyBlob(blob)) blobList.remove(i);

            // otherwise we have counted a blob
            correctBlobs++;
            if(blob.assignedColour== Color.RED) reds++;
            if(blob.assignedColour== Color.GREEN) greens++;
            if(blob.assignedColour== Color.BLACK) blacks++;
            if(blob.assignedColour== Color.BLUE) blues++;
        }

        Log.d("uct.snapvote", "Identified "+correctBlobs+" as actual votes. Red: "+reds+" Green: "+greens+" Blue: "+blues+" Black: "+blacks);
    }

    /**
     * Classify a blob.
     * @param blob The blob
     * @return False if the region is not valid. True if it was given a colour.
     */
    public boolean classifyBlob(Blob blob)
    {
        // average colour for the interior pixel sample set
        int totalred = 0;
        int totalgreen = 0;
        int totalblue = 0;

        // Interior pixel set
        List<Integer> insideSquare = new ArrayList<Integer>();

        // White border pixel set
        List<Integer> whiteBorder = new ArrayList<Integer>();

        // group pixel samples
        for(BlobSampler.Sample sample : blob.samples)
        {
            if (sample.insideSample)
                insideSquare.add(sample.colour);
            else
                whiteBorder.add(sample.colour);
        }

        // compute average
        for(Integer colour : insideSquare)
        {
            totalred += (colour >> 16) & 255;
            totalgreen += (colour >> 8) & 255;
            totalblue += colour & 255;
        }

        // calculate average inside colour
        totalred = totalred/insideSquare.size();
        totalgreen = totalgreen/insideSquare.size();
        totalblue = totalblue/insideSquare.size();

        // check colour consistancies
        float tdistance = 0;
        for(Integer colour : insideSquare)
        {
            int red = (colour >> 16) & 255;
            int green = (colour >> 8) & 255;
            int blue = colour & 255;

            tdistance += colourDistance(red, green, blue, totalred, totalgreen, totalblue);
        }
        tdistance /= insideSquare.size();

        // check for whiteness
        int numWhitesOutside = 0;
        for(Integer colour : whiteBorder)
        {
            float[] hsv = new float[3];
            Color.colorToHSV(colour, hsv);

            if(hsv[2] >= 0.65 && hsv[1] <= 0.13) numWhitesOutside++;
        }

        // Check white pixel count
        if(numWhitesOutside >= 4){

            // check the interior pixel consistency
            if (tdistance < 35)
            {
                // distance to each of the target colours
                float[] colourDistances = new float[4];
                colourDistances[0] = colourDistance(totalred, totalgreen, totalblue, 255,60,60);
                colourDistances[1] = colourDistance(totalred, totalgreen, totalblue, 35,129,63);
                colourDistances[2] = colourDistance(totalred, totalgreen, totalblue, 50,70,200);
                colourDistances[3] = colourDistance(totalred, totalgreen, totalblue, 37,37,37) + monochromeDistance(totalred, totalgreen, totalblue);

                // find minimum distance
                int imin = 1;
                for(int i=0;i<4;i++)
                {
                    if(colourDistances[i] < colourDistances[imin]) imin = i;
                }

                switch (imin)
                {
                    case 0:
                        blob.assignedColour = Color.RED;
                        break;
                    case 1:
                        blob.assignedColour = Color.GREEN;
                        break;
                    case 2:
                        blob.assignedColour = Color.BLUE;
                        break;
                    case 3:
                        blob.assignedColour = Color.BLACK;
                        break;
                }

                return true;
            }
        }
        return false;
    }

    /**
     * Distance between specified colour and target colour
     * = cuberoot(dR^3 + dB^3 + dG^3)
     * @param r1 Red value
     * @param g1 Green value
     * @param b1 Blue value
     * @param r2 Target red value
     * @param g2 Target green value
     * @param b2 Target blue value
     * @return distance (float)
     */
    public float colourDistance(int r1, int g1, int b1, int r2, int g2, int b2)
    {
        int d1 = r1-r2;
        int d2 = g1-g2;
        int d3 = b1-b2;

        float r = (float)Math.cbrt(Math.abs(d1*d1*d1) + Math.abs(d2*d2*d2) + Math.abs(d3*d3*d3));

        return r;
    }

    /**
     * Monochrome distance weighting
     *
     * Pixel colours should be within a certain threshold of the average colour.
     * Otherwise return a high distance.
     *
     * @return
     */
    public float monochromeDistance(int r, int g, int b)
    {
        int av = (r+g+b) / 3;
        if (Math.abs(r-av) > 15) return 255;
        if (Math.abs(g-av) > 12) return 255;
        if (Math.abs(b-av) > 15) return 255;
        return av-170;
    }

    /**
     * @return the final blob list
     */
    public List<Blob> getBlobList() {
        return blobList;
    }
}
