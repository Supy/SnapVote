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

    private PriorityQueue<BlobSampler.Sample> pixelSamples;

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

    private void removeInvalidSizedBlobs()
    {

        List<Blob> before = blobList;
        List<Blob> after = new ArrayList<Blob>();
        for(int i=0;i<before.size();i++)
        {
            Blob blob = before.get(i);
            int height = blob.yMax-blob.yMin;
            int yCenter = (blob.yMax+blob.yMin)/2;

            int minH = (int)(((float)yCenter / imageHeight) * 30);

            if (height > minH) after.add(blob);
        }

        blobList = after;
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
//            if(sampleX == 0)
//                sampleX++;
//            else if(sampleX == imageWidth-1)
//                sampleX--;
//
//            // Average each colour component with 2 neighbours.
//            int colour1 = pixelData[sampleX-1];
//            int colour2 = pixelData[sampleX];
//            int colour3 = pixelData[sampleX+1];
//
//            int avgR = (((colour1 >> 16) & 255) + ((colour2 >> 16) & 255) + ((colour3 >> 16) & 255))/3;
//            int avgG = (((colour1 >> 8) & 255) + ((colour2 >> 8) & 255) + ((colour3 >> 8) & 255))/3;
//            int avgB = ((colour1 & 255) + (colour2 & 255) + (colour3 & 255))/3;
//
//            int colour = (avgR << 16) | (avgG << 8) | avgB;

            sample.colour = pixelData[sampleX];
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

        int totalred = 0;
        int totalgreen = 0;
        int totalblue = 0;

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


        int numWhitesOutside = 0;
        for(Integer colour : whiteBorder)
        {
            float[] hsv = new float[3];
            Color.colorToHSV(colour, hsv);

            if(hsv[2] >= 0.65 && hsv[1] <= 0.13) numWhitesOutside++;
        }


        // A blob is considered a valid vote if:
        //  - number of white outside samples >= 2 (i.e. on a white page)
        // AND
        //  - to be classified as a colour, the average of all samples taken inside the blob must fall within range

        // Colours
        if(numWhitesOutside >= 4){

            if (tdistance < 35)
            {
                float[] colourDistances = new float[4];
                colourDistances[0] = colourDistance(totalred, totalgreen, totalblue, 255,60,60);
                colourDistances[1] = colourDistance(totalred, totalgreen, totalblue, 35,129,63);
                colourDistances[2] = colourDistance(totalred, totalgreen, totalblue, 50,70,200);
                colourDistances[3] = colourDistance(totalred, totalgreen, totalblue, 37,37,37) + monochromeDistance(totalred, totalgreen, totalblue);

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
                    case 4:
                        return false;
                }

                return true;
            }
        }
        return false;
    }

    public float colourDistance(int r1, int g1, int b1, int r2, int g2, int b2)
    {

        int d1 = r1-r2;
        int d2 = g1-g2;
        int d3 = b1-b2;

        float r = (float)Math.cbrt(Math.abs(d1*d1*d1) + Math.abs(d2*d2*d2) + Math.abs(d3*d3*d3));

        //Log.d("uct.snapvote", String.format("R: %d %d, G: %d, %d B: %d %d = %f", r1, r2, g1, g2, b1, b2, r ));
        return r;
    }

    public float monochromeDistance(int r, int g, int b)
    {
        int av = (r+g+b) / 3;
        if (Math.abs(r-av) > 15) return 255;
        if (Math.abs(g-av) > 12) return 255;
        if (Math.abs(b-av) > 15) return 255;
        return av-170;
    }

    public List<Blob> getBlobList() {
        return blobList;
    }
}
