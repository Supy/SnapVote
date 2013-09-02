package uct.snapvote.util;

import java.util.List;
import java.util.PriorityQueue;

import uct.snapvote.filter.BlobDetectorFilter;

/**
 * Created by Justin on 2013/09/03.
 */
public class BlobSampler {

    // % and hard value offsets for determining where to take colour samples.
    private static final short PIXEL_OFFSET = 5;
    private static final double PERCENTAGE_OFFSET = 0.1;

    public static class Sample implements Comparable<Sample>{
        public BlobDetectorFilter.Blob parent;
        public int pixelIndex;
        public int colour;

        public Sample(int pi, BlobDetectorFilter.Blob p){
            parent = p;
            pixelIndex = pi;
        }

        @Override
        public int compareTo(Sample sample2){
            return this.pixelIndex - sample2.pixelIndex;
        }
    }

    public static PriorityQueue<Sample> createSamples(List<BlobDetectorFilter.Blob> blobList, int imageWidth){
        PriorityQueue<Sample> samples = new PriorityQueue<Sample>(blobList.size() * 8);

        for(BlobDetectorFilter.Blob blob : blobList){
            int centerX = (blob.xMax + blob.xMin)/2;
            int centerY = (blob.yMax + blob.yMin)/2;
            int blobWidth = (blob.xMax - blob.xMin)/2;
            int blobHeight = (blob.yMax - blob.yMin)/2;

            int widthPercentage = (int)(blobWidth * PERCENTAGE_OFFSET);
            int heightPercentage = (int)(blobHeight * PERCENTAGE_OFFSET);

            // -- Outside samples
            int oTopIndex = ((centerY - blobHeight - heightPercentage - PIXEL_OFFSET) * imageWidth) + centerX;
            Sample oTop = new Sample(oTopIndex, blob);

            int oBottomIndex = ((centerY + blobHeight + heightPercentage + PIXEL_OFFSET) * imageWidth) + centerX;
            Sample oBottom = new Sample(oBottomIndex, blob);

            int oLeftIndex = (centerY * imageWidth) + centerX - blobWidth - widthPercentage - PIXEL_OFFSET;
            Sample oLeft = new Sample(oLeftIndex, blob);

            int oRightIndex = (centerY * imageWidth) + centerX + blobWidth + widthPercentage + PIXEL_OFFSET;
            Sample oRight = new Sample(oRightIndex, blob);
            // --

            // -- Inside samples
            int iTopIndex = ((centerY - heightPercentage - PIXEL_OFFSET) * imageWidth) + centerX;
            Sample iTop = new Sample(iTopIndex, blob);

            int iBottomIndex = ((centerY + heightPercentage + PIXEL_OFFSET) * imageWidth) + centerX;
            Sample iBottom = new Sample(iBottomIndex, blob);

            int iLeftIndex = (centerY * imageWidth) + centerX - widthPercentage - PIXEL_OFFSET;
            Sample iLeft = new Sample(iLeftIndex, blob);

            int iRightIndex = (centerY * imageWidth) + centerX + widthPercentage + PIXEL_OFFSET;
            Sample iRight = new Sample(iRightIndex, blob);
            // --

            // Push our samples into the queue
            samples.add(oTop);
            samples.add(oBottom);
            samples.add(oLeft);
            samples.add(oRight);
            samples.add(iTop);
            samples.add(iBottom);
            samples.add(iLeft);
            samples.add(iRight);
        }

        return samples;
    }
}
