package uct.snapvote;

import android.graphics.Bitmap;

/**
 * Created by Ben on 8/3/13.
 */
public class ImageByteBuffer {

    private int height;
    private int width;
    private byte[][] data;

    public ImageByteBuffer(int width, int height) {
        data = new byte[height][width];
        this.width = width;
        this.height = height;
    }

    // ACCESSORS
    public int getWidth() {return width;}
    public int getHeight() {return height;}
    public byte get(int x, int y) {return data[y][x];}
    public void set(int x, int y, byte value) {data[y][x] = value;}

    // UNSAFE ACCESSOR
    public byte[][] getRealData() {return data;}

    // BITMAAAAAAAAAAAAAAAAAAP
    //  TODO: speed this up. May require changing backing array type
    public Bitmap createBitmap() {
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);  // should attempt to keep all colour data

        for(int y = 0;y<height;y++) {
            for(int x = 0;x<width;x++) {
                bm.setPixel(x,y, 0xff000000 + (data[y][x] & 0xFF)); // currently creates a 'Bluescale' image
            }
        }

        return bm;
    }

}
