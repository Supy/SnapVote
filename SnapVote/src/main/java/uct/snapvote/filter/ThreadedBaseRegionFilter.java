package uct.snapvote.filter;

import uct.snapvote.ImageByteBuffer;

/**
 * Created by Ben on 8/4/13.
 */
public class ThreadedBaseRegionFilter extends BaseRegionFilter implements Runnable {

    public float progress = 0;
    private int maxprogress;

    public ThreadedBaseRegionFilter(ImageByteBuffer source, ImageByteBuffer destination, int srcx, int srcy, int srcwidth, int srcheight) {
        super(source, destination, srcx, srcy, srcwidth, srcheight);
        maxprogress = srcwidth * srcheight;
    }

    @Override
    public void run() {
        for(int y=srcy;y<srcheight;y++)
            for(int x=srcx;x<srcwidth;x++) {
                destination.set(x,y, source.get(x,y));
                progress++;
            }
    }
}
