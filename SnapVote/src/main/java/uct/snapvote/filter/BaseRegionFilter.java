package uct.snapvote.filter;

import uct.snapvote.util.ImageByteBuffer;

public abstract class BaseRegionFilter {

    protected ImageByteBuffer source, destination;
    protected int left, top, right, bottom;

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
