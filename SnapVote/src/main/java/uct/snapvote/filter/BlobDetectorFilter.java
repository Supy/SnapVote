package uct.snapvote.filter;

import android.util.Log;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import uct.snapvote.util.Blob;
import uct.snapvote.util.ImageInputStream;
import uct.snapvote.util.IntQueue;

/**
 * Blob detection algorithm. Uses a one dimensional BitSet representing the two dimensional buffer
 * of peaks in order to run a breath-first-search. A custom IntQueue is used to avoid GC overheard.
 * Once the algorithm has completed, getBlobList() returns the list of detected regions.
 *
 */
public class BlobDetectorFilter {

    private BitSet visitedPixels;
    private int width;
    private int height;

    // constructor, set source and destination to null since it does not need image buffers.
    public BlobDetectorFilter(BitSet pixelBitset, int width, int height){
        this.visitedPixels = pixelBitset;
        this.width = width;
        this.height = height;
    }

    /**
     * Run the blob detection algorithm on the BitSet. Use width and height as the image dimensions
     * @return A list of blob regions.
     */
    public List<Blob> process(){

        List<Blob> blobs = new ArrayList<Blob>();

        IntQueue pixelQueue = new IntQueue(42000);

        for(int y = 1; y < height - 1; y++)
        {
            for(int x = 1; x < width - 1; x++)
            {
                int pixelIndex = y * width + x;

                // if not a peak and not visited already
                if (!visitedPixels.get(pixelIndex))
                {
                    // mark as visited
                    visitedPixels.set(pixelIndex);
                    pixelQueue.push(pixelIndex);

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

                        //expand to neighbouring pixels if possible
                        if(!north){
                            pixelQueue.push(northIndex);
                            visitedPixels.set(northIndex);
                        }
                        
                        if(!south){
                            pixelQueue.push(southIndex);
                            visitedPixels.set(southIndex);
                        }
    
                        if(!east){
                            pixelQueue.push(eastIndex);
                            visitedPixels.set(eastIndex);
                        }
    
                        if(!west){
                            pixelQueue.push(westIndex);
                            visitedPixels.set(westIndex);
                        }

                        // add the current pixel to the current blob
                        blob.addPixel(queuePixelIndex % width, queuePixelIndex / width);
                    }
                    
                    if(blob.valid()) blobs.add(blob);

                }
            }
        }

        Log.d("uct.snapvote", "Blobs: "+blobs.size());

        return blobs;
    }

}
