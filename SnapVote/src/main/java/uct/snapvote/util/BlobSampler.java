package uct.snapvote.util;

import java.util.List;
import java.util.PriorityQueue;

import uct.snapvote.filter.BlobDetectorFilter;

/**
 * Created by Justin on 2013/09/03.
 */
public class BlobSampler {

    // % and hard value offsets for determining where to take colour samples.
    private static final short PIXEL_OFFSET = 3;
    private static final double PERCENTAGE_OFFSET = 0.1;

    public static class Sample implements Comparable<Sample>{
        public BlobDetectorFilter.Blob parent;
        public int pixelIndex;
        public int colour;
        public boolean insideSample;

        public Sample(int pi, BlobDetectorFilter.Blob p, boolean iS){
            parent = p;
            pixelIndex = pi;
            insideSample = iS;
        }

        @Override
        public int compareTo(Sample sample2){
            return this.pixelIndex - sample2.pixelIndex;
        }
    }

    public static PriorityQueue<Sample> createSamples(List<BlobDetectorFilter.Blob> blobList, int imageWidth, int imageHeight){
        PriorityQueue<Sample> samples = new PriorityQueue<Sample>(blobList.size() * 8);

        int maxPixelIndex = (imageWidth * imageHeight)-1;

        for(BlobDetectorFilter.Blob blob : blobList){

            // Calculate these once per blob and reuse.
            int centerX = (blob.xMax + blob.xMin)/2;
            int centerY = (blob.yMax + blob.yMin)/2;
            int blobWidth = (blob.xMax - blob.xMin)/2;
            int blobHeight = (blob.yMax - blob.yMin)/2;
            int widthPercentage = (int)(blobWidth * PERCENTAGE_OFFSET);
            int heightPercentage = (int)(blobHeight * PERCENTAGE_OFFSET);

            // -- Outside samples
            int oTopIndex = ((centerY - blobHeight - heightPercentage - PIXEL_OFFSET) * imageWidth) + centerX;
            if(oTopIndex >= 0){
                Sample oTop = new Sample(oTopIndex, blob, false);
                samples.add(oTop);
            }

            int oBottomIndex = ((centerY + blobHeight + heightPercentage + PIXEL_OFFSET) * imageWidth) + centerX;
            if(oBottomIndex <= maxPixelIndex){
                Sample oBottom = new Sample(oBottomIndex, blob, false);
                samples.add(oBottom);
            }

            int oLeftOffset = centerX - blobWidth - widthPercentage - PIXEL_OFFSET;
            if(oLeftOffset >= 0){
                int oLeftIndex = (centerY * imageWidth) + oLeftOffset;
                Sample oLeft = new Sample(oLeftIndex, blob, false);
                samples.add(oLeft);
            }

            int oRightOffset = centerX + blobWidth + widthPercentage + PIXEL_OFFSET;
            if(oRightOffset < imageWidth){
                int oRightIndex = (centerY * imageWidth) + oRightOffset;
                Sample oRight = new Sample(oRightIndex, blob, false);
                samples.add(oRight);
            }
            // --

            // -- Inside samples
            int iTopIndex = ((centerY - heightPercentage - PIXEL_OFFSET) * imageWidth) + centerX;
            Sample iTop = new Sample(iTopIndex, blob, true);
            samples.add(iTop);

            int iBottomIndex = ((centerY + heightPercentage + PIXEL_OFFSET) * imageWidth) + centerX;
            Sample iBottom = new Sample(iBottomIndex, blob, true);
            samples.add(iBottom);

            int iLeftIndex = (centerY * imageWidth) + centerX - widthPercentage - PIXEL_OFFSET;
            Sample iLeft = new Sample(iLeftIndex, blob, true);
            samples.add(iLeft);

            int iRightIndex = (centerY * imageWidth) + centerX + widthPercentage + PIXEL_OFFSET;
            Sample iRight = new Sample(iRightIndex, blob, true);
            samples.add(iRight);
            // --
        }

        return samples;
    }
}
