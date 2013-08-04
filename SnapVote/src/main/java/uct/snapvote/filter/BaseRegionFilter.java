package uct.snapvote.filter;

import uct.snapvote.ImageByteBuffer;

/**
 * Created by Ben on 8/3/13.
 */
public class BaseRegionFilter extends BaseFilter {

    int srcx, srcy, srcheight, srcwidth;

    public BaseRegionFilter(ImageByteBuffer source, ImageByteBuffer destination, int srcx, int srcy, int srcwidth, int srcheight) {
        super(source, destination);
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
