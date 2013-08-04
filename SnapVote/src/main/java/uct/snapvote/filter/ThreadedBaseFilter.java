package uct.snapvote.filter;

import uct.snapvote.ImageByteBuffer;

/**
 * Created by Ben on 8/4/13.
 */
public class ThreadedBaseFilter extends BaseFilter implements Runnable {

    public float progress = 0;
    private int maxprogress;

    public ThreadedBaseFilter(ImageByteBuffer source, ImageByteBuffer destination) {
        super(source, destination);
        int height = source.getHeight();
        int width = source.getWidth();
        maxprogress = width * height;
    }

    @Override
    public void run() {
        int height = source.getHeight();
        int width = source.getWidth();
        for(int y=0;y<height;y++)
            for(int x=0;x<width;x++) {
                destination.set(x,y, source.get(x,y));
                progress++;
            }
    }
}
