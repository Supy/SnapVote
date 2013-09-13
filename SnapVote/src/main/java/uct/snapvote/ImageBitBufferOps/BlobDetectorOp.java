package uct.snapvote.ImageBitBufferOps;

import android.util.Log;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import uct.snapvote.ImageBitBuffer;
import uct.snapvote.ImageByteBuffer;
import uct.snapvote.filter.BaseRegionFilter;
import uct.snapvote.util.Blob;
import uct.snapvote.util.BlobSampler;
import uct.snapvote.util.IntQueue;

/**
 * Created by Justin on 2013/08/11.
 */
public class BlobDetectorOp extends BaseRegionOp {

    private int width;
    private int height;
    private List<Blob> blobs;
    private ImageByteBuffer outCanvas;

    public BlobDetectorOp(ImageBitBuffer source, int width, int height, ImageByteBuffer outCanvas){
        super(source, null);
        this.width = width;
        this.height = height;
        this.blobs = new ArrayList<Blob>();
        this.outCanvas = outCanvas;
    }



    @Override
    public void run(){
        IntQueue pixelQueue = new IntQueue(42000);

        for(int y=1;y< height-1;y++)
        {
            for(int x=1;x< width-1;x++)
            {

                // if not a peak and not visited already
                if (!source.get(x,y))
                {
                    // mark as visited
                    source.set(x, y);
                    pixelQueue.add(y*width + x);

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
                        boolean north = (currentY > 0) ? source.get(currentX, currentY-1) : true;
                        boolean south = (currentY < height-1) ? source.get(currentX, currentY+1) : true;
                        boolean east = (currentX < width-1) ? source.get(currentX+1, currentY) : true;
                        boolean west = (currentX > 0) ? source.get(currentX-1, currentY) : true;
    
                        if(!north){
                            pixelQueue.add(northIndex);
                            source.set(currentX, currentY-1);
                        }
                        
                        if(!south){
                            pixelQueue.add(southIndex);
                            source.set(currentX, currentY+1);
                        }
    
                        if(!east){
                            pixelQueue.add(eastIndex);
                            source.set(currentX+1, currentY);
                        }
    
                        if(!west){
                            pixelQueue.add(westIndex);
                            source.set(currentX-1, currentY);
                        }

                        blob.addPixel(currentX, currentY);
                    }
                    
                    if(blob.valid()) blobs.add(blob);


                }
            }
        }

        Log.d("uct.snapvote", "Blobs: "+blobs.size());

        // TODO: Remove this code once the app is complete.
        // Draw borders around blobs.
        for(Blob b : blobs)
        {
            for(int y=b.yMin;y< b.yMax;y++)
            {
                outCanvas.set(b.xMin,y,(byte)255);
                outCanvas.set(b.xMax,y,(byte)255);

                if( y ==b.yMin  || y == (b.yMax-1))
                {
                    for(int x=b.xMin;x< b.xMax;x++)
                    {
                        outCanvas.set(x,y,(byte)255);
                    }
                }
            }
        }
    }

    public List<Blob> getBlobList(){
        return blobs;
    }
}
