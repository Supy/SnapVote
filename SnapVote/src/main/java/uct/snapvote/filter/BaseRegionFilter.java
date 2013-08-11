package uct.snapvote.filter;

import uct.snapvote.ImageByteBuffer;

/**
 * Created by Ben on 8/3/13.
 */
public abstract class BaseRegionFilter {

    ImageByteBuffer source, destination;
    int x1, y1, y2, x2;

    public BaseRegionFilter(ImageByteBuffer source, ImageByteBuffer destination) {
        this.source = source;
        this.destination = destination;
        this.y2 = source.getHeight();
        this.x2 = source.getWidth();
        this.x1 = 0;
        this.y1 = 0;
    }

    public BaseRegionFilter(ImageByteBuffer source, ImageByteBuffer destination, int x1, int y1, int x2, int y2) {
        this.source = source;
        this.destination = destination;
        this.y2 = y2;
        this.x2 = x2;
        this.x1 = x1;
        this.y1 = y1;
    }

    public abstract void run();
}
