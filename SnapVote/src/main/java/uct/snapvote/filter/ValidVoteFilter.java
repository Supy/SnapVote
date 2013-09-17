package uct.snapvote.filter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;

import java.io.IOException;
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

            int numRed = 0;
            int numGreen = 0;
            int numBlue = 0;
            int numBlack = 0;
            int numWhitesOutside = 0;
            int numInsideSamples = 0;

            float[] totalHsv = new float[]{0,0,0};

            for(BlobSampler.Sample sample : blob.samples){

                // Easier to classify colours in Hue,Saturation,Value (HSV) format, so convert it.
                float[] hsv = new float[3];
                Color.colorToHSV(sample.colour, hsv);

                if(sample.insideSample){
                    // Account for the red hue wrap-around which makes a bad average.
                    totalHsv[0] += (hsv[0] >= 340) ? hsv[0] - 340 : hsv[0];
                    totalHsv[1] += hsv[1];
                    totalHsv[2] += hsv[2];
                    numInsideSamples++;
                }else if(hsv[2] >= 0.75 && hsv[1] <= 0.16){
                    numWhitesOutside++;
                }
            }

            float h = totalHsv[0]/numInsideSamples;
            float s = (totalHsv[1]/numInsideSamples)*100;
            float v = (totalHsv[2]/numInsideSamples)*100;

            // A blob is considered a valid vote if:
            //  - number of white outside samples >= 2 (i.e. on a white page)
            // AND
            //  - to be classified as a colour, the average of all samples taken inside the blob must fall within range

            // Colours
            if(numWhitesOutside >= 2){
                if(h <= 20 && s >= 50 && v >= 63){
                    blob.assignedColour = 1;
                    reds++;
                }else if(h >= 100 && h <= 164 && s >= 20){
                    blob.assignedColour = 2;
                    greens++;
                }else if(h >= 210 && h < 270 && s >= 55){
                    blob.assignedColour = 3;
                    blues++;
                }else if(s <= 25 && v <= 40){
                    blob.assignedColour = 0;
                    blacks++;
                }else{
                    blobList.remove(i);
                }
            }else{
                blobList.remove(i);
            }
        }

        Log.d("uct.snapvote", "Identified "+correctBlobs+" as actual votes. Red: "+reds+" Green: "+greens+" Blue: "+blues+" Black: "+blacks);
    }
}
