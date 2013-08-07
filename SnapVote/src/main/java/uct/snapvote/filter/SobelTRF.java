package uct.snapvote.filter;

import android.util.Log;

import uct.snapvote.ImageByteBuffer;

/**
 * Created by Ben on 8/7/13.
 */
public class SobelTRF extends ThreadedBaseRegionFilter {


    public SobelTRF(ImageByteBuffer source, ImageByteBuffer destination) {
        super(source, destination);
    }

    public SobelTRF(ImageByteBuffer source, ImageByteBuffer destination, int x1, int y1, int x2, int y2) {
        super(source, destination, x1, y1, x2, y2);
    }

    public void run() {
        Log.d("uct.snapvote", "start " + this.x1 + " " + this.y1);
        for(int y= y1;y< y2;y++) {
            for(int x= x1;x< x2;x++) {

                int Gx = -(source.get(x-1, y-1) & 0xFF) - 2*(source.get(x, y-1) & 0xFF) -(source.get(x+1, y-1) & 0xFF) +
                        (source.get(x-1, y+1) & 0xFF) + 2*(source.get(x, y+1) & 0xFF) +(source.get(x+1, y+1) & 0xFF);

                int Gy = -(source.get(x-1, y-1) & 0xFF) - 2*(source.get(x-1, y) & 0xFF) -(source.get(x-1, y+1) & 0xFF) +
                        (source.get(x+1, y-1) & 0xFF) + 2*(source.get(x+1, y) & 0xFF) +(source.get(x+1, y+1) & 0xFF);

                int Gm = ((int) Math.sqrt(Gx*Gx + Gy*Gy));
                if (Gm > 255) Gm = 255;

                destination.set(x,y, (byte)Gm);
                progress++;
            }
        }

        Log.d("uct.snapvote", "finished " + this.x1 + " " + this.y1);

    }

}
