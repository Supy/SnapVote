package uct.snapvote.filter;

import android.util.Log;

import java.util.concurrent.Callable;

import uct.snapvote.ImageByteBuffer;

/**
 * Created by Justin on 2013/08/11.
 */
public class BlobDetectorFilter extends BaseRegionFilter {

    final char MAX_CHAR_VAL = 65535;

    public BlobDetectorFilter(ImageByteBuffer source){
        super(source, null);
    }

    public class Blob
    {
        public int minx, miny, maxx, maxy;
        public char label;
    }

    @Override
    public void run(){

        // this contains the labels for the pixels. 0 = no label
        char[] pixelLabels = new char[source.getHeight() * source.getWidth()];

        char[] labelTable = new char[Character.MAX_VALUE];

        char currentlabel = 1;

        // loop through all pixels
        for(int y=1;y<source.getHeight()-1;y++)
        {
            for(int x=1;x<source.getWidth()-1;x++)
            {
                int px = source.get(x,y) & 0xFF;

                // can this pixel be labeled?
                if (px == 0)
                {
                    // check top and left pixel labels
                    char north = pixelLabels[(y-1) * source.getWidth() + x];
                    char west = pixelLabels[y * source.getWidth() + (x-1)];
                    //char northwest = pixelLabels[(y-1) * source.getWidth() + (x-1)];
                    //char northeast = pixelLabels[(y-1) * source.getWidth() + (x+1)];

                    char min = Character.MAX_VALUE;
                    if(north > 0 && north < min) min = north;
                    if(west > 0 && west < min) min = west;
                    //if(northwest > 0 && northwest < min) min = northwest;
                   // if(northeast > 0 && northeast < min) min = northeast;

                    // if neither were labeled
                    if (min == Character.MAX_VALUE)
                    {
                        // label the pixel with currentlabel
                        pixelLabels[y * source.getWidth() + x] = currentlabel;
                        labelTable[currentlabel] = currentlabel;
                        // increment current label
                        currentlabel++;
                    }
                    else
                    {
                        pixelLabels[y * source.getWidth() + x] = min;

                        if (north > 0) labelTable[north] = min;
                        if (west > 0) labelTable[west] = min;
                    }
                }
            }
        }

        char maxlabelcount = currentlabel;


        // for each label, decay it
        for (int i = maxlabelcount-1; i >0;i--)
        {
            if (labelTable[i] != i)
            {
                int l = i;
                while (l != labelTable[l]) l = labelTable[l];
                labelTable[i] = (char)l;
            }
        }

        char newlabel = 0;
        for(int i=0;i< maxlabelcount-1;i++)
        {
            if ( labelTable[i] == i) labelTable[i] = newlabel++;
            else labelTable[i] = labelTable[labelTable[i]];
        }

        byte [] colours = new byte[]{
                (byte) 31,
                (byte) 63,
                (byte) 95,
                (byte) 127,
                (byte) 159,
                (byte) 191,
                (byte) 223
        };

        for(int y=1;y<source.getHeight()-1;y++)
        {
            for(int x=1;x<source.getWidth()-1;x++)
            {
                char l = pixelLabels[y * source.getWidth() + x];
                char rootlabel = labelTable[l];

                source.set(x,y, colours[rootlabel % 7]);
            }
        }


        //Log.d("uct.snapvote", "Labels: "+(int)count+".");
    }
}
