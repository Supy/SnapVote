package uct.snapvote.ImageBitBufferOps;

import uct.snapvote.ImageBitBuffer;

public class DilationOp extends BaseRegionOpThreaded{

    private int radius;

    public DilationOp(ImageBitBuffer source, ImageBitBuffer destination, int radius) {
        super(source, destination);
        this.radius = radius;
    }

    public DilationOp(ImageBitBuffer source, ImageBitBuffer destination, int radius, int x1, int y1, int x2, int y2) {
        super(source, destination, x1, y1, x2, y2);
        this.radius = radius;
    }

    @Override
    public void run() {

        // Loop through entire range
        for(int y= y1;y< y2;y++) {
            for(int x= x1;x< x2;x++) {
                if(source.get(x,y))
                {
                    for(int dy = -radius; dy <= radius; dy++) {
                        int ry = y+dy;
                        for(int dx = -radius; dx <= radius; dx++) {
                            int rx = x+dx;
                            destination.set(rx,ry);
                        }
                    }
                }
            }
        }
    }
}
