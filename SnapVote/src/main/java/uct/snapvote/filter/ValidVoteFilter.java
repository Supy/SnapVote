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

import uct.snapvote.ImageByteBuffer;
import uct.snapvote.util.Blob;
import uct.snapvote.util.BlobSampler;
import uct.snapvote.util.ImageInputStream;

/**
 * Created by Justin on 2013/09/03.
 *
 */
public class ValidVoteFilter{

    private List<Blob> blobList;
    private ImageInputStream imageInputStream;
    private int imageWidth;
    private int imageHeight;
    private ImageByteBuffer source;

    private PriorityQueue<BlobSampler.Sample> pixelSamples;

    public ValidVoteFilter(List<Blob> blobList, ImageInputStream imageInputStream, ImageByteBuffer source){
        this.blobList = blobList;
        this.imageInputStream = imageInputStream;
        this.imageWidth = imageInputStream.width;
        this.imageHeight = imageInputStream.height;
        this.source = source;

        try{
            getPixelSamples();
            colourizeSamples();
            classifyBlobs();
        }catch(IOException e){
            Log.d("uct.snapvote", "Could not colourize pixel samples: "+e.getMessage());
        }
    }

    private void getPixelSamples(){
        pixelSamples = BlobSampler.createSamples(blobList, imageWidth, imageHeight, source);
        Log.d("uct.snapvote", pixelSamples.size()+" blob samples created.");
    }

    private void colourizeSamples() throws IOException {
        BitmapRegionDecoder regDec = BitmapRegionDecoder.newInstance(imageInputStream.getInputStream(), false);

        Bitmap strip;
        int[] pixelData = new int[0];
        BitmapFactory.Options postOptions = new BitmapFactory.Options();

        int currentRow = 0;
        int maxPixelIndexInRow = imageWidth-1;

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

            // Can't get neighbours if we're on the edge of a row.
            if(sampleX == 0)
                sampleX++;
            else if(sampleX == imageWidth-1)
                sampleX--;

            // Average each colour component with 2 neighbours.
            int colour1 = pixelData[sampleX-1];
            int colour2 = pixelData[sampleX];
            int colour3 = pixelData[sampleX+1];

            int avgR = (((colour1 >> 16) & 255) + ((colour2 >> 16) & 255) + ((colour3 >> 16) & 255))/3;
            int avgG = (((colour1 >> 8) & 255) + ((colour2 >> 8) & 255) + ((colour3 >> 8) & 255))/3;
            int avgB = ((colour1 & 255) + (colour2 & 255) + (colour3 & 255))/3;

            int colour = (avgR << 16) | (avgG << 8) | avgB;

            sample.colour = colour;
        }
    }

    private void classifyBlobs(){
        int correctBlobs = 0;
        int reds =0, blues=0, greens=0, blacks=0;

        for(int i = blobList.size()-1; i >= 0; i--){
            Blob blob = blobList.get(i);
            if(!classifyBlob(blob)) blobList.remove(i);

            correctBlobs++;
            if(blob.assignedColour== Color.RED) reds++;
            if(blob.assignedColour== Color.GREEN) greens++;
            if(blob.assignedColour== Color.BLACK) blacks++;
            if(blob.assignedColour== Color.BLUE) blues++;
        }
        // Math.max(totalred, Math.max(totalred, totalblue)) < 40)
        // s <= 25 && v <= 40
        Log.d("uct.snapvote", "Identified "+correctBlobs+" as actual votes. Red: "+reds+" Green: "+greens+" Blue: "+blues+" Black: "+blacks);
    }

    public boolean classifyBlob(Blob blob)
    {
        int numWhitesOutside = 0;
        int numInsideSamples = 0;

        int totalred = 0;
        int totalgreen = 0;
        int totalblue = 0;

        int valuered, valuegreen, valueblue;

        List<Integer> insideSquare = new ArrayList<Integer>();
        List<Integer> whiteBorder = new ArrayList<Integer>();

        for(BlobSampler.Sample sample : blob.samples)
        {
            if (sample.insideSample)
                insideSquare.add(sample.colour);
            else
                whiteBorder.add(sample.colour);
        }

        for(Integer colour : insideSquare)
        {
            totalred += (colour >> 16) & 255;
            totalgreen += (colour >> 8) & 255;
            totalblue += colour & 255;
            numInsideSamples++;
        }

        for(Integer colour : whiteBorder)
        {
            float[] hsv = new float[3];
            Color.colorToHSV(colour, hsv);

            if(hsv[2] >= 0.75 && hsv[1] <= 0.16) numWhitesOutside++;
        }

        // calculate average inside colour
        totalred = totalred/numInsideSamples;
        totalgreen = totalgreen/numInsideSamples;
        totalblue = totalblue/numInsideSamples;

        // A blob is considered a valid vote if:
        //  - number of white outside samples >= 2 (i.e. on a white page)
        // AND
        //  - to be classified as a colour, the average of all samples taken inside the blob must fall within range

        // Colours
        if(numWhitesOutside >= 2){

            int colour = Color.rgb(totalred, totalgreen, totalblue);

            float[] hsv = new float[3];
            Color.colorToHSV(colour, hsv);

            float h = hsv[0];
            float s = hsv[1]*100;
            float v = hsv[2]*100;

            if((h <= 20 || h >= 340) && s >= 50 && v >= 63){
                blob.assignedColour = Color.RED;
            }else if(h >= 100 && h <= 164 && s >= 20){
                blob.assignedColour = Color.GREEN;
            }else if(h >= 210 && h < 270 && s >= 55){
                blob.assignedColour = Color.BLUE;
            }else if(isBlack(totalred, totalgreen, totalblue)){
                blob.assignedColour = Color.BLACK;
            }else{
                return false;
            }
        }else{
            return false;
        }
        return true;
    }

    public boolean isBlack(int r, int g, int b)
    {
        int av = (r+g+b) / 3;
        if (Math.abs(r-av) > 10) return false;
        if (Math.abs(g-av) > 10) return false;
        if (Math.abs(b-av) > 10) return false;
        if (av < 100) return  true;
        return false;
    }


}
