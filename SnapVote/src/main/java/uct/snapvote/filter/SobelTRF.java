package uct.snapvote.filter;

import android.util.Log;

import uct.snapvote.ImageByteBuffer;

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
        for(int y= y1;y< y2;y++) {
            for(int x= x1;x< x2;x++) {

                int Gx = -(source.get(x-1, y-1) & 0xFF) - 2*(source.get(x, y-1) & 0xFF) -(source.get(x+1, y-1) & 0xFF) +
                        (source.get(x-1, y+1) & 0xFF) + 2*(source.get(x, y+1) & 0xFF) +(source.get(x+1, y+1) & 0xFF);

                int Gy = -(source.get(x-1, y-1) & 0xFF) - 2*(source.get(x-1, y) & 0xFF) -(source.get(x-1, y+1) & 0xFF) +
                        (source.get(x+1, y-1) & 0xFF) + 2*(source.get(x+1, y) & 0xFF) +(source.get(x+1, y+1) & 0xFF);

                int Gm = ((int) Math.sqrt(Gx*Gx + Gy*Gy));

                double angle = (Math.atan2(Gy,Gx) * 180) / Math.PI;
                angle = (angle + 180) % 180 + 22.5;

                int acat = 0;
                if (angle < 180) acat = 135;
                if (angle < 135) acat = 90;
                if (angle < 90) acat = 45;
                if (angle < 45) acat = 0;

                dirDataOutput.set(x, y, (byte)acat);

                if (Gm > 255) Gm = 255;

                destination.set(x,y, (byte)Gm);
            }
        }

        Log.d("uct.snapvote", "finished " + this.x1 + " " + this.y1);

    }

}
