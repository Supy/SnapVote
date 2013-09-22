package uct.snapvote.filter;

import android.util.Log;

import uct.snapvote.util.ImageByteBuffer;

/**
 * Peak finder, can be run multithreaded.
 */
public class PeakFindTRF extends ThreadedBaseRegionFilter {

    private ImageByteBuffer dirDataInput;
    private int peakLow, peakHigh;
    private boolean[][] peakList;

    /**
     * Constructor
     * @param dirDataInput Peak direction angle from the Sobel filter
     * @param peakLow Low peak threshold parameter, Below this value, peaks are discarded.
     * @param peakHigh High peak threshold parameter. Above this value, peaks are definite.
     * @param peakList Output object
     */
    public PeakFindTRF(ImageByteBuffer source, int left, int top, int right, int bottom, ImageByteBuffer dirDataInput, int peakLow, int peakHigh, boolean[][] peakList) {
        super(source, null, left, top, right, bottom);
        this.dirDataInput = dirDataInput;
        this.peakLow = peakLow;
        this.peakHigh = peakHigh;
        this.peakList = peakList;
    }

    /**
     * Run the algorithm on the sobel buffers
     */
    public void run() {
        Log.d("uct.snapvote", "Canny edge detection with low peak = "+peakLow+" and high peak = "+peakHigh);

        // loop through all pixels
        for(int y= top;y< right;y++) {
            for(int x= left;x< bottom;x++) {

                int angle = dirDataInput.get(x,y) & 0xFF;
                int x1 = x;
                int x2 = x;
                int y1 = y;
                int y2 = y;

                // Which two neighbouring pixels must we compare to?
                if(angle == 90){
                    x1--;
                    x2++;
                }else if(angle == 45){
                    x1--;
                    x2++;
                    y1--;
                    y2++;
                }else if(angle == 0){
                    y1--;
                    y2++;
                }else{
                    x1--;
                    x2++;
                    y1++;
                    y2--;
                }

                // get peak strength
                int gradient = source.get(x,y) & 0xFF;

                // must be greater than neighbours
                if(gradient > 0 && gradient > (source.get(x1, y1) & 0xFF) && gradient > (source.get(x2, y2) & 0xFF)) {
                    if(gradient > peakHigh){        // High threshold, indicating definite peak
                        byte bytePeakMax = (byte) 255;
                        source.set(x, y, bytePeakMax);
                        peakList[y][x] = true;
                    }else if(gradient < peakLow){  // Potential peak
                        source.set(x,y, (byte) 0);
                    }
                }else{
                    source.set(x, y, (byte) 0);
                }
            }
        }
    }
}

