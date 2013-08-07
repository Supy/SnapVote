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

        for(int y= y1;y< y2;y++) {
            for(int x= x1;x< x2;x++) {

                int angle = dirDataInput.get(x,y);
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
                int peak = 0;

                if(gradient > 0 && gradient > (source.get(x1, y1) & 0xFF) && gradient > (source.get(x2, y2) & 0xFF)) {
                    peak = gradient;

                    // High threshold, indicating definite peak
                    if(peak > HIGH){
                        destination.set(x,y, (byte) 1);
                        peak = 0;
                    }else if(peak < LOW){
                        peak = 0; 	// Definitely not a peak
                    }

                    // Every other pixel is a "potential peak", which we check through later.
                }

                if(peak == 0)
                    source.set(x,y, (byte) 0);
            }
        }

        //TODO: Need to fix the infinite loop here. Commented out so that it doesnt break build.
/*        boolean more;
        int iterations = 0;
        do{
            iterations++;
            int neighbours = 0;
            more = false;
            for(int y = y1; y < y2; y++){
                for(int x = x1; x < x2; x++){
                    int pixel = destination.get(x, y) & 0xFF;
                    // If we are a peak, any neighbouring potential peaks are made into peaks.
                    if(pixel != 0){
                        for(int p = -1; p < 1; p++){
                            for(int q = -1; q < 1; q++){
                                int neighbour = source.get(x+p, y+q) & 0xFF;

                                if(neighbour != 0){
                                    destination.set(x+p, y+q, (byte) 1);
                                    neighbours++;
                                    more = true;
                                }
                            }
                        }
                    }
                }
            }
        }while(more && iterations < 100);

        if(iterations >= 100)
            Log.d("uct.snapvote", "Quit after 2000 iterations.");*/


        // Clear out remaining potential peaks that have lost their potential ;(
        for(int y = y1; y < y2; y++){
            for(int x = x1; x < x2; x++){

                int pixel = destination.get(x, y) & 0xFF;

                // Expand any peaks out by 1px to fill small gaps.
                if(pixel != 0){
                    for(int p = -1; p < 1; p++)
                        for(int q = -1; q < 1; q++)
                            destination.set(x+p, y+q, (byte) 1);
                }
            }
        }

    }

}

