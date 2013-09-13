package uct.snapvote.ImageBitBufferOps;

import uct.snapvote.ImageBitBuffer;

public abstract class BaseRegionOp {

    ImageBitBuffer source, destination;
    int x1, y1, y2, x2;

    public BaseRegionOp(ImageBitBuffer source, ImageBitBuffer destination) {
        this.source = source;
        this.destination = destination;
        this.y2 = source.getHeight();
        this.x2 = source.getWidth();
        this.x1 = 0;
        this.y1 = 0;
    }

    public BaseRegionOp(ImageBitBuffer source, ImageBitBuffer destination, int x1, int y1, int x2, int y2) {
        this.source = source;
        this.destination = destination;
        this.y2 = y2;
        this.x2 = x2;
        this.x1 = x1;
        this.y1 = y1;
    }

    public abstract void run();




}
