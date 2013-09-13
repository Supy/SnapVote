package uct.snapvote.ImageBitBufferOps;

import uct.snapvote.ImageBitBuffer;

public class ErosionOp extends BaseRegionOpThreaded{

    private StructuringElement structuringElement;

    public ErosionOp(ImageBitBuffer source, ImageBitBuffer destination, StructuringElement se) {
        super(source, destination);
        this.structuringElement = se;
    }

    public ErosionOp(ImageBitBuffer source, ImageBitBuffer destination, StructuringElement se, int x1, int y1, int x2, int y2) {
        super(source, destination, x1, y1, x2, y2);
        this.structuringElement = se;
    }

    @Override
    public void run() {

        int w = structuringElement.GetWidth();
        int h = structuringElement.GetHeight();
        int ws = w/2;
        int hs = h/2;

        for(int y= y1;y< y2;y++) {
            for(int x= x1;x< x2;x++) {

                boolean val = false;

                if(source.get(x,y))
                {
                    for(int dy = -hs; dy < h; dy++) {
                        int ry = y+dy;
                        for(int dx = -ws; dx < w; dx++) {
                            int rx = x+dx;
                            destination.set(rx,ry);
                        }
                    }
                }
            }
        }
    }
}
