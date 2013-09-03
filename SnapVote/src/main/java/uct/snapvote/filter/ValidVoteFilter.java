package uct.snapvote.filter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
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

            // TODO: we've got the sample colours, now we need to 'apply' them to their blobs and classify the blob.
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

            int avgR = ((colour1 & 255) + (colour2 & 255) + (colour3 & 255))/3;
            int avgG = (((colour1 >> 8) & 255) + ((colour2 >> 8) & 255) + ((colour3 >> 8) & 255))/3;
            int avgB = (((colour1 >> 16) & 255) + ((colour2 >> 16) & 255) + ((colour3 >> 16) & 255))/3;

            int colour = avgR | (avgG << 8) | (avgB << 16);

            sample.colour = colour;
        }
    }
}
