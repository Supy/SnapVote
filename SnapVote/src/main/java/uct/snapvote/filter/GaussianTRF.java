package uct.snapvote.filter;

import android.util.Log;

import uct.snapvote.util.ImageByteBuffer;

/**
 * Gaussian Blur algorithm. applies a gaussian blur to the pixels in the region with the given
 * blur radius.
 */
public class GaussianTRF extends ThreadedBaseRegionFilter {

    private int radius;
    private double[][] gaussian;

    /**
     * Constructor
     */
    public GaussianTRF(ImageByteBuffer source, ImageByteBuffer destination, int left, int top, int right, int bottom, int blurradius) {
        super(source, destination, left, top, right, bottom);
        setBlurRadius(blurradius);
    }

    /**
     * Set the blur radius in preparation for the processing. The gaussian matrix is calculated
     * using a gaussian distribution approximation.
     * @param r The radius of pixels around each pixel to blur.
     */
    private void setBlurRadius(int r) {
        radius = r;

        // create Gaussian
        float sigma = radius / 2.0f;
        int size = radius*2 +1;

        gaussian = new double[size][size];

        // first build a kernel using the gaussian approximation
        double [] hkernel = new double[size];
        for (int x =0;x<size;x+=1)
            hkernel[x] = Math.exp(-(Math.pow((x-radius)/(sigma),2))/2.0);

        // build the gaussian matrix using horizantal and vertical components
        double kernelsum = 0;
        for (int y = 0;y< size;y++)
            for (int x = 0;x< size;x++) {
                gaussian[y][x] = hkernel[y] * hkernel[x];
                kernelsum += gaussian[y][x];
            }

        // normalise using total sum
        for (int y = 0;y< size;y++)
            for (int x = 0;x< size;x++)
                gaussian[y][x] /= kernelsum;
    }

    @Override
    /**
     * Run the blur on all of the pixels in the image.
     */
    public void run() {
        Log.d("uct.snapvote", "start " + this.left + " " + this.top);
        // loop through all of the pixels
        for(int y= top;y< right;y++) {
            for(int x= left;x< bottom;x++) {

                // keep track of the total
                double total = 0;
                for(int dy = -radius; dy < radius; dy++) {

                    int ry = y+dy;
                    for(int dx = -radius; dx < radius; dx++) {

                        int rx = x+dx;

                        // get the colour of the neighbouring pixels
                        int c = source.get(rx, ry);
                        // add a fraction to the total
                        total += gaussian[radius+dy][radius+dx] * (c & 0xFF);
                    }
                }
                //output
                destination.set(x,y, (byte)((int) total));
            }
        }

        Log.d("uct.snapvote", "finished " + this.left + " " + this.top);
    }
}
