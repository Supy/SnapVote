package uct.snapvote.filter;

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

                int value = source.get(x,y) & 0xFF;


                if(value > 0 && value > (source.get(x1, y1) & 0xFF) && value > (source.get(x2, y2) & 0xFF)) {
                    //peak!
                    destination.set(x,y, (byte) value);
                }
                else
                {
                    destination.set(x,y, (byte) 0);
                }

            }
        }

    }

}

