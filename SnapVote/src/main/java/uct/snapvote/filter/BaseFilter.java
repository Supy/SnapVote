package uct.snapvote.filter;

import uct.snapvote.ImageByteBuffer;

/**
 * Created by Ben on 8/3/13.
 A basic image filter, it simply copies all pixels from the source into the destination buffer.
 More complex filters should extend this.
 */
public class BaseFilter {

    ImageByteBuffer source, destination;

    public BaseFilter(ImageByteBuffer source, ImageByteBuffer destination) {
        this.source = source;
        this.destination = destination;
    }

    public void process() {
        int height = source.getHeight();
        int width = source.getWidth();
        for(int y=0;y<height;y++)
            for(int x=0;x<width;x++)
                destination.set(x,y, source.get(x,y));
    }

}
