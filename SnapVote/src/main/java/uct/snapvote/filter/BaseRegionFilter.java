package uct.snapvote.filter;

import uct.snapvote.ImageByteBuffer;

/**
 * Created by Ben on 8/3/13.
 */
public class BaseRegionFilter {

    ImageByteBuffer source, destination;
    int srcx, srcy, srcheight, srcwidth;

    public BaseRegionFilter(ImageByteBuffer source, ImageByteBuffer destination) {
        this.source = source;
        this.destination = destination;
        this.srcheight = source.getHeight();
        this.srcwidth = source.getWidth();
        this.srcx = 0;
        this.srcy = 0;
    }

    public BaseRegionFilter(ImageByteBuffer source, ImageByteBuffer destination, int srcx, int srcy, int srcwidth, int srcheight) {
        this.source = source;
        this.destination = destination;
        this.srcheight = srcheight;
        this.srcwidth = srcwidth;
        this.srcx = srcx;
        this.srcy = srcy;
    }

    public void process() {
        for(int y=srcy;y<srcheight;y++)
            for(int x=srcx;x<srcwidth;x++)
                destination.set(x,y, source.get(x,y));
    }
}
