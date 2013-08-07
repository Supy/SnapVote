package uct.snapvote.filter;

import android.util.Log;

import uct.snapvote.ImageByteBuffer;

/**
 * Created by Ben on 8/7/13.
 */
public class GuassianTRF extends ThreadedBaseRegionFilter {

    private int radius;
    private double[][] guassian;


    public GuassianTRF(ImageByteBuffer source, ImageByteBuffer destination, int blurradius) {
        super(source, destination);
        setBlurRadius(blurradius);
    }

    public GuassianTRF(ImageByteBuffer source, ImageByteBuffer destination, int x1, int y1, int x2, int y2, int blurradius) {
        super(source, destination, x1, y1, x2, y2);
        setBlurRadius(blurradius);
    }

    private void setBlurRadius(int r) {
        radius = r;

        // create guassian
        float sigma = radius / 2.0f;
        int size = radius*2 +1;

        guassian = new double[size][size];

        double [] hkernel = new double[size];
        for (int x =0;x<size;x+=1)
            hkernel[x] = Math.exp(-(Math.pow((x-radius)/(sigma),2))/2.0);

        double kernelsum = 0;
        for (int y = 0;y< size;y++)
            for (int x = 0;x< size;x++) {
                guassian[y][x] = hkernel[y] * hkernel[x];
                kernelsum += guassian[y][x];
            }

        // normalise
        for (int y = 0;y< size;y++)
            for (int x = 0;x< size;x++)
                guassian[y][x] /= kernelsum;
    }

    @Override
    public void run() {
        Log.d("uct.snapvote", "start " + this.x1 + " " + this.y1);
        for(int y= y1;y< y2;y++) {
            for(int x= x1;x< x2;x++) {

                double total = 0;

                for(int dy = -radius; dy < radius; dy++) {

                    int ry = y+dy;

                    for(int dx = -radius; dx < radius; dx++) {

                        int rx = x+dx;

                        int c = source.get(rx, ry);
                        total += guassian[radius+dy][radius+dx] * (c & 0xFF);
                    }
                }

                destination.set(x,y, (byte)((int) total));
                progress++;
            }


        }

        Log.d("uct.snapvote", "finished " + this.x1 + " " + this.y1);

    }

}
