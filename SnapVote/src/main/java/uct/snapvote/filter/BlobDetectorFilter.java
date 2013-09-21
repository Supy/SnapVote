package uct.snapvote.filter;

import android.util.Log;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import uct.snapvote.util.Blob;
import uct.snapvote.util.ImageInputStream;
import uct.snapvote.util.IntQueue;

/**
 * Created by Justin on 2013/08/11.
 *
 */
public class BlobDetectorFilter extends BaseRegionFilter {

    private BitSet visitedPixels;
    private int width;
    private int height;
    private List<Blob> blobs;

    public BlobDetectorFilter(BitSet pixelBitset, ImageInputStream imageInputStream){
        super(null, null,0,0,imageInputStream.width-1, imageInputStream.height-1);
        this.visitedPixels = pixelBitset;
        this.width = imageInputStream.width;
        this.height = imageInputStream.height;
        this.blobs = new ArrayList<Blob>();
    }

    @Override
    public void run(){
        IntQueue pixelQueue = new IntQueue(42000);

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
                    
                    if(blob.valid()) blobs.add(blob);

                }
            }
        }

        Log.d("uct.snapvote", "Blobs: "+blobs.size());
    }

    public List<Blob> getBlobList(){
        return blobs;
    }
}
