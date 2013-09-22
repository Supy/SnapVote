package uct.snapvote.filter;

import uct.snapvote.util.ImageByteBuffer;

/**
 * The base abstract class from which the image manipulation filters are descended.
 * This class is designed to perform a function on a subregion of an image, and to output
 * the result into a destination image.
 */
public abstract class BaseRegionFilter {

    // input and output buffers
    protected ImageByteBuffer source, destination;

    // region limits
    protected int left, top, right, bottom;

    // constructor
    public BaseRegionFilter(ImageByteBuffer source, ImageByteBuffer destination, int left, int top, int bottom, int right) {
        this.source = source;
        this.destination = destination;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
        this.top = top;
    }

    public abstract void run();
}
