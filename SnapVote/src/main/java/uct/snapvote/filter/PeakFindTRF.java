package uct.snapvote.filter;

import android.util.Log;

import uct.snapvote.ImageByteBuffer;

/**
 * Created by Ben on 8/7/13.
 */
public class PeakFindTRF extends ThreadedBaseRegionFilter {

    private ImageByteBuffer dirDataInput;

    public PeakFindTRF(ImageByteBuffer source, ImageByteBuffer destination, ImageByteBuffer dirDataInput) {
        super(source, destination);
        this.dirDataInput = dirDataInput;
    }

    public PeakFindTRF(ImageByteBuffer source, ImageByteBuffer destination, int x1, int y1, int x2, int y2, ImageByteBuffer dirDataInput) {
        super(source, destination, x1, y1, x2, y2);
        this.dirDataInput = dirDataInput;
    }

    public void run() {
        // The thresholds for the edge tracing.
        // TODO: needs to be a parameter
        final int HIGH = 150;
        final int LOW = 70;
        final int peakMax = 255;
        final byte peakMaxByte = (byte) peakMax;
        final byte zero = (byte) 0;

        int peaks = 0;
        int potentialPeaks = 0;
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
                byte peak = 0;
                byte finalPeak = 0;

                if(gradient > 0 && gradient > (source.get(x1, y1) & 0xFF) && gradient > (source.get(x2, y2) & 0xFF)) {
                    if(gradient > HIGH){        // High threshold, indicating definite peak
                        finalPeak = peakMaxByte;
                        peak = peakMaxByte;
                        peaks++;
                    }else if(gradient >= LOW){  // Potential peak
                        peak = (byte) gradient;
                        potentialPeaks++;
                    }
                }

                destination.set(x,y, finalPeak);
                source.set(x,y, peak);
            }
        }

        Log.d("uct.snapvote", "Definite peaks: "+peaks+" Potential peaks: "+potentialPeaks);
        //TODO: Need to fix the infinite loop here. Commented out so that it doesnt break build.
        boolean more;
        int iterations = 0;
        int convertedPeaks = 0;
        do{
            more = false;
            for(int y = y1; y < y2; y++){
                for(int x = x1; x < x2; x++){
                    byte pixel = destination.get(x, y);
                    // If we are a peak, any neighbouring potential peaks are made into peaks.
                    if(pixel == peakMaxByte){
                        for(int p = -1; p < 1; p++){
                            for(int q = -1; q < 1; q++){

                                // Don't need to adjust the center pixel in the filter.
                                if(q == 0 && p == 0)
                                    continue;

                                byte neighbour = source.get(x+p, y+q);

                                if(neighbour != 0 && neighbour != peakMaxByte){
                                    source.set(x+p, y+q, zero);
                                    destination.set(x+p, y+q, peakMaxByte);
                                    more = true;
                                    convertedPeaks++;
                                }
                            }
                        }
                    }
                }
            }
        }while(more && iterations < 200);

        if(iterations >= 200)
            Log.d("uct.snapvote", "Quit after 200 iterations.");

        Log.d("uct.snapvote", "Potential peaks converted: "+convertedPeaks);

        // Clear out remaining potential peaks that have lost their potential ;(
        for(int y = y1; y < y2; y++){
            for(int x = x1; x < x2; x++){

                byte pixel = destination.get(x, y);

                // Expand any peaks out by 1px to fill small gaps.
                if(pixel != 0){
                    for(int p = -1; p < 1; p++)
                        for(int q = -1; q < 1; q++){
                            if(q == 0 && p == 0)
                                continue;

                            destination.setWhite(x+p, y+q);
                        }
                }
            }
        }
    }
}

