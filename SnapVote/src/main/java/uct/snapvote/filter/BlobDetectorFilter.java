package uct.snapvote.filter;

import android.util.Log;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import uct.snapvote.ImageByteBuffer;
import uct.snapvote.util.IntQueue;

/**
 * Created by Justin on 2013/08/11.
 */
public class BlobDetectorFilter extends BaseRegionFilter {

    private BitSet visitedPixels;
    private int width;
    private int height;

    public BlobDetectorFilter(ImageByteBuffer source, BitSet pixelBitset, int width, int height){
        super(source, null);
        this.visitedPixels = pixelBitset;
        this.width = width;
        this.height = height;
    }

    static class Blob
    {
        public static final int MIN_MASS = 35;
        public static final double MAX_RATIO = 2.0;
        public static final double MIN_AREA = 0.35;
        public int xMin = Integer.MAX_VALUE;
        public int xMax;
        public int yMin = Integer.MAX_VALUE;
        public int yMax;
        public int mass;

        public void addPixel(int pixelX, int pixelY)
        {
            if(pixelX < xMin)
                xMin = pixelX;
            else if(pixelX > xMax)
                xMax = pixelX;

            if(pixelY < yMin)
                yMin = pixelY;
            else if(pixelY > yMax)
                yMax = pixelY;

            mass++;
        }

        public boolean valid(){

            if(mass < MIN_MASS)
                return false;

            int width = xMax - xMin;
            int height = yMax - yMin;

            double ratio;

            if(width >= height){
                ratio = width * 1.0 / height;
            }else{
                ratio = height * 1.0 / width;
            }

            if(ratio > MAX_RATIO)
                return false;

            double areaFilled = mass * 1.0 / (width * height);

            if(areaFilled < MIN_AREA)
                return false;

            return true;
        }

        public String toString()
        {
            return String.format("X: %4d -> %4d, Y: %4d -> %4d, mass: %6d", xMin, xMax, yMin, yMax, mass);
        }
    }

    @Override
    public void run(){

        List<Blob> blobs = new ArrayList<Blob>();
        IntQueue pixelQueue = new IntQueue(42000);

        int maxPixelIndex = width * height - 1;

        for(int y=1;y< height-1;y++)
        {
            for(int x=1;x< width-1;x++)
            {
                int pixelIndex = y * width + x;

                // if not a peak and not visited already
                if (!visitedPixels.get(pixelIndex))
                {
                    // mark as visited
                    visitedPixels.set(pixelIndex);
                    pixelQueue.add(pixelIndex);

                    int queuePixelIndex;
                    Blob blob = new Blob();

                    while(!pixelQueue.isEmpty())
                    {
                        queuePixelIndex = pixelQueue.pop();
                        int currentX = queuePixelIndex % width;
                        int currentY = queuePixelIndex / width;

                        // start flood fill
                        // check adjacent pixels
                        int northIndex = queuePixelIndex - width;
                        int southIndex = queuePixelIndex + width;
                        int eastIndex = queuePixelIndex + 1;
                        int westIndex = queuePixelIndex - 1;
                        boolean north = (currentY > 0) ? visitedPixels.get(northIndex) : true;
                        boolean south = (currentY < height-1) ? visitedPixels.get(southIndex) : true;
                        boolean east = (currentX < width-1) ? visitedPixels.get(eastIndex) : true;
                        boolean west = (currentX > 0) ? visitedPixels.get(westIndex) : true;
    
                        if(!north){
                            pixelQueue.add(northIndex);
                            visitedPixels.set(northIndex);
                        }
                        
                        if(!south){
                            pixelQueue.add(southIndex);
                            visitedPixels.set(southIndex);
                        }
    
                        if(!east){
                            pixelQueue.add(eastIndex);
                            visitedPixels.set(eastIndex);
                        }
    
                        if(!west){
                            pixelQueue.add(westIndex);
                            visitedPixels.set(westIndex);
                        }

                        blob.addPixel(queuePixelIndex % width, queuePixelIndex / width);
                    }
                    
                    if(blob.valid())
                        blobs.add(blob);


                }
            }
        }

        Log.d("uct.snapvote", "Blobs: "+blobs.size());

        // Draw borders around blobs.
        for(Blob b : blobs)
        {
            for(int y=b.yMin;y< b.yMax;y++)
            {
                source.set(b.xMin,y,(byte)255);
                source.set(b.xMax,y,(byte)255);

                if( y ==b.yMin  || y == (b.yMax-1))
                {
                    for(int x=b.xMin;x< b.xMax;x++)
                    {
                        source.set(x,y,(byte)255);
                    }
                }
            }
        }
    }
}
