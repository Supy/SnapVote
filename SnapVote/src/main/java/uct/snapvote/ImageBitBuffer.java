package uct.snapvote;

import android.graphics.Bitmap;

import java.util.BitSet;

public class ImageBitBuffer {

    private int height;
    private int width;
    private BitSet data;

    public ImageBitBuffer(int width, int height) {
        data = new BitSet(height*width);
        this.width = width;
        this.height = height;
    }

    // ACCESSORS
    public int getWidth() {return width;}
    public int getHeight() {return height;}

    public boolean get(int x, int y) {return data.get(y * width + x);}

    public void set(int x, int y) {data.set(y*width +x);}
    public void set(int x, int y, boolean value) {data.set(y*width +x, value);}

    public Bitmap createBitmap() {
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);  // should attempt to keep all colour data

        for(int y = 0;y<height;y++) {
            int xoffset = y*width;
            for(int x = 0;x<width;x++) {
                int c = 0xff000000;
                if (data.get(xoffset + x)) c = 0xffffffff;
                bm.setPixel(x,y, c);
            }
        }
        return bm;
    }

}
