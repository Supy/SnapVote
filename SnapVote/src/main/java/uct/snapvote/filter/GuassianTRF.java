package uct.snapvote.filter;

import android.util.Log;

import java.security.CodeSigner;

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

    public GuassianTRF(ImageByteBuffer source, ImageByteBuffer destination, int srcx, int srcy, int srcwidth, int srcheight, int blurradius) {
        super(source, destination, srcx, srcy, srcwidth, srcheight);
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


        for(int y=srcy;y<srcheight;y++) {
            for(int x=srcx;x<srcwidth;x++) {

                double total = 0;

                for(int dy = -radius; dy < radius; dy++) {

                    int ry = y+dy;
                    if (ry < 0) ry = -ry;
                    else if (ry >= source.getHeight()) ry = (source.getHeight() << 1) - ry -1;

                    for(int dx = -radius; dx < radius; dx++) {

                        int rx = x+dx;
                        if (rx < 0) rx = -rx;
                        else if (rx >= source.getWidth()) rx = (source.getWidth() << 1) - rx -1;

                        int c = source.get(rx, ry);
                        total += guassian[radius+dy][radius+dx] * (c & 0xFF);
                    }
                }

                destination.set(x,y, (byte)((int) total));
                progress++;
            }


        }

    }

}
