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

import uct.snapvote.util.BlobSampler;
import uct.snapvote.util.ImageInputStream;

/**
 * Created by Justin on 2013/09/03.
 */
public class ValidVoteFilter{

    private List<BlobDetectorFilter.Blob> blobList;
    private ImageInputStream imageInputStream;
    private int imageWidth;
    private int imageHeight;

    private PriorityQueue<BlobSampler.Sample> pixelSamples;

    public ValidVoteFilter(List<BlobDetectorFilter.Blob> blobList, ImageInputStream imageInputStream){
        this.blobList = blobList;
        this.imageInputStream = imageInputStream;
        this.imageWidth = imageInputStream.width;
        this.imageHeight = imageInputStream.height;

        try{
            getPixelSamples();
            colourizeSamples();
            classifyBlobs();
        }catch(IOException e){
            Log.d("uct.snapvote", "Could not colourize pixel samples: "+e.getMessage());
        }
    }

    private void getPixelSamples(){
        pixelSamples = BlobSampler.createSamples(blobList, imageWidth, imageHeight);
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

        for(int i=blobList.size()-1; i >=0; i--){
            BlobDetectorFilter.Blob blob = blobList.get(i);

            int numRed = 0;
            int numGreen = 0;
            int numBlue = 0;
            int numBlack = 0;
            int numWhitesOutside = 0;

            for(BlobSampler.Sample sample : blob.samples){

                // Easier to classify colours in Hue,Saturation,Lightness (HSL) format, so convert it.
                float[] hsv = new float[3];
                Color.colorToHSV(sample.colour, hsv);

                float h = hsv[0];
                float s = hsv[1];
                float v = hsv[2];

                // TODO: Need to adjust these values to get the best combinations.
                if(sample.insideSample){
                    if(v < 0.18 && s < 0.1){
                        numBlack++;
                    }else if(s >= 0.2){     // Saturation < 0.2 is grey. We don't want grey.
                        // Colours
                        if(h >= 330 || h < 30){
                            numRed++;
                        }else if(h >= 90 && h < 150){
                            numGreen++;
                        }else if(h >= 210 && h < 270){
                            numBlue++;
                        }
                    }
                }else if(v > 0.90 && s < 0.15){
                    numWhitesOutside++;
                }

                int maxColour = Math.max(Math.max(numBlack, numBlue), Math.max(numRed, numGreen));

                // A blob is considered a valid vote if:
                //  - number of white outside samples >= 3 (i.e. on a white page)
                // AND
                //  - to be classified as a colour, the number of samples with that colour as their maximum component must be >= 3. (i.e. majority rules)

                if(numWhitesOutside >= 2 && maxColour >= 3){
                    if(maxColour == numBlack){
                        blob.assignedColour = 0;
                        blacks++;
                    }else if(maxColour == numRed){
                        blob.assignedColour = 1;
                        reds++;
                    }else if(maxColour == numGreen){
                        blob.assignedColour = 2;
                        greens++;
                    }else{
                        blob.assignedColour = 3;
                        blues++;
                    }

                    correctBlobs++;
                }
            }
        }

        Log.d("uct.snapvote", "Identified "+correctBlobs+" as actual votes. Red: "+reds+" Green: "+greens+" Blue: "+blues+" Black: "+blacks);
    }
}
