package uct.snapvote.filter;

import uct.snapvote.ImageByteBuffer;

/**
 * Created by Ben on 8/4/13.
 */
public class InvertTRF extends ThreadedBaseRegionFilter {

    public InvertTRF(ImageByteBuffer source, ImageByteBuffer destination) {
        super(source, destination);
    }

    public InvertTRF(ImageByteBuffer source, ImageByteBuffer destination, int srcx, int srcy, int srcwidth, int srcheight) {
        super(source, destination, srcx, srcy, srcwidth, srcheight);
    }

    @Override
    public void run() {
        for(int y=srcy;y<srcheight;y++)
            for(int x=srcx;x<srcwidth;x++) {
                byte b = (byte) (255-source.get(x,y) & 0xFF);
                destination.set(x,y, b);
                progress++;
            }
    }
}
