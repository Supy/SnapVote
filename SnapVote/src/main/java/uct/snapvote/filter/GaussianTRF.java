package uct.snapvote.filter;

import android.util.Log;

import uct.snapvote.ImageByteBuffer;

/**
 * Created by Ben on 8/7/13.
 */
public class GaussianTRF extends ThreadedBaseRegionFilter {

    private int radius;
    private double[][] gaussian;


    public GaussianTRF(ImageByteBuffer source, ImageByteBuffer destination, int blurradius) {
        super(source, destination);
        setBlurRadius(blurradius);
    }

    public GaussianTRF(ImageByteBuffer source, ImageByteBuffer destination, int x1, int y1, int x2, int y2, int blurradius) {
        super(source, destination, x1, y1, x2, y2);
        setBlurRadius(blurradius);
    }

    private void setBlurRadius(int r) {
        radius = r;

        // create Gaussian
        float sigma = radius / 2.0f;
        int size = radius*2 +1;

        gaussian = new double[size][size];

        double [] hkernel = new double[size];
        for (int x =0;x<size;x+=1)
            hkernel[x] = Math.exp(-(Math.pow((x-radius)/(sigma),2))/2.0);

        double kernelsum = 0;
        for (int y = 0;y< size;y++)
            for (int x = 0;x< size;x++) {
                gaussian[y][x] = hkernel[y] * hkernel[x];
                kernelsum += gaussian[y][x];
            }

        // normalise
        for (int y = 0;y< size;y++)
            for (int x = 0;x< size;x++)
                gaussian[y][x] /= kernelsum;
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
                        total += gaussian[radius+dy][radius+dx] * (c & 0xFF);
                    }
                }

                destination.set(x,y, (byte)((int) total));
            }
        }

        Log.d("uct.snapvote", "finished " + this.x1 + " " + this.y1);
    }
}
