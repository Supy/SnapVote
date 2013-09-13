package uct.snapvote.ImageBitBufferOps;


import uct.snapvote.ImageBitBuffer;

public abstract class BaseRegionOpThreaded extends BaseRegionOp implements Runnable {

    public BaseRegionOpThreaded(ImageBitBuffer source, ImageBitBuffer destination) {
        super(source, destination);
    }

    public BaseRegionOpThreaded(ImageBitBuffer source, ImageBitBuffer destination, int x1, int y1, int x2, int y2) {
        super(source, destination, x1, y1, x2, y2);
    }

}
