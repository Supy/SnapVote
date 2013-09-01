package uct.snapvote.filter;

import android.util.Log;

import uct.snapvote.ImageByteBuffer;
import uct.snapvote.util.SobelAngleClassifier;

/**
 * Created by Ben on 8/7/13.
 */
public class SobelTRF extends ThreadedBaseRegionFilter {

    private ImageByteBuffer dirDataOutput;

    public SobelTRF(ImageByteBuffer source, ImageByteBuffer destination, ImageByteBuffer dirDataOutput) {
        super(source, destination);
        this.dirDataOutput = dirDataOutput;
    }

    public SobelTRF(ImageByteBuffer source, ImageByteBuffer destination, int x1, int y1, int x2, int y2, ImageByteBuffer dirDataOutput) {
        super(source, destination, x1, y1, x2, y2);
        this.dirDataOutput = dirDataOutput;
    }

    public void run() {
        Log.d("uct.snapvote", "start " + this.x1 + " " + this.y1);

        // Loop through all pixels in the given region

        for(int y= y1;y< y2;y++) {
            for(int x= x1;x< x2;x++) {

                int g00 = source.get(x-1, y-1) & 0xFF; int g01 = source.get(x, y-1) & 0xFF; int g02 = source.get(x+1, y-1) & 0xFF;
                int g10 = source.get(x-1, y) & 0xFF; int g11 = source.get(x, y) & 0xFF; int g12 = source.get(x+1, y) & 0xFF;
                int g20 = source.get(x-1, y+1) & 0xFF; int g21 = source.get(x, y+1) & 0xFF; int g22 = source.get(x+1, y+1) & 0xFF;

                int Gx = -g00 -2*g01 -g02 +g20 +2*g21 +g22;
                int Gy = -g00 -2*g10 -g20 +g02 +2*g12 +g22;

                // Store gradient direction
                dirDataOutput.set(x, y, SobelAngleClassifier.classify(Gx/10,Gy/10));

                // Store gradient value
                destination.set(x,y, SobelAngleClassifier.magnify(Gx/10, Gy/10));
            }
        }

        Log.d("uct.snapvote", "finished " + this.x1 + " " + this.y1);

    }

}
