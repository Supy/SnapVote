package uct.snapvote.filter;

import uct.snapvote.util.ImageByteBuffer;

public abstract class ThreadedBaseRegionFilter extends BaseRegionFilter implements Runnable {

    public ThreadedBaseRegionFilter(ImageByteBuffer source, ImageByteBuffer destination, int x1, int y1, int x2, int y2) {
        super(source, destination, x1, y1, x2, y2);
    }
}
