package uct.snapvote.filter;

import android.util.Log;

import uct.snapvote.ImageByteBuffer;

/**
 * Created by Ben on 8/7/13.
 */
public class PeakFindTRF extends ThreadedBaseRegionFilter {

    private ImageByteBuffer dirDataInput;
    private int peakLow, peakHigh;
    private final byte bytePeakMax = (byte) 255;
    private boolean[][] peakList;

    public PeakFindTRF(ImageByteBuffer source, ImageByteBuffer dirDataInput, int peakLow, int peakHigh) {
        super(source, null);
        this.dirDataInput = dirDataInput;
        this.peakLow = peakLow;
        this.peakHigh = peakHigh;
    }

    public PeakFindTRF(ImageByteBuffer source, int x1, int y1, int x2, int y2, ImageByteBuffer dirDataInput, int peakLow, int peakHigh, boolean[][] peakList) {
        super(source, null, x1, y1, x2, y2);
        this.dirDataInput = dirDataInput;
        this.peakLow = peakLow;
        this.peakHigh = peakHigh;
        this.peakList = peakList;
    }

    public void run() {
        Log.d("uct.snapvote", "Canny edge detection with low peak = "+peakLow+" and high peak = "+peakHigh);

        for(int y= y1;y< y2;y++) {
            for(int x= x1;x< x2;x++) {

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

                int gradient = source.get(x,y) & 0xFF;

                if(gradient > 0 && gradient > (source.get(x1, y1) & 0xFF) && gradient > (source.get(x2, y2) & 0xFF)) {
                    if(gradient > peakHigh){        // High threshold, indicating definite peak
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

