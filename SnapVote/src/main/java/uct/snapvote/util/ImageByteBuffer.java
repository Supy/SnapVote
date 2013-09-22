package uct.snapvote.util;

import android.graphics.Bitmap;

/**
 * A 2 dimensional byte array object. Accessors provide easy access to
 * width, height, and pixel data.
 */
public class ImageByteBuffer
{
    /** Private Variables **/
    // height of the data array (y coordinate)
    private int height;
    // width of the data array (x coordinate)
    private int width;
    // the backing byte array
    private byte[][] pixelData;

    /** [Constructor]
     * Initialise empty data array with the given size
     * @param width Width of the image.
     * @param height Heigth of the image.
     */
    public ImageByteBuffer(int width, int height) {
        pixelData = new byte[height][width];
        this.width = width;
        this.height = height;
    }

    /** Accessors **/
    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public byte get(int x, int y)
    {
        return pixelData[y][x];
    }

    /**
     * Set the specified pixel to the given value. Be careful of
     * automatic byte casting.
     * @param x X Coordinate.
     * @param y Y Coordinate.
     * @param value Byte value.
     */
    public void set(int x, int y, byte value)
    {
        pixelData[y][x] = value;
    }

    /**
     * Create a greyscale bitmap of the byte array. A value of 0 indicates a black pixel
     * while a value of 255 indicates white. Used during debugging.
     * @return A full resolution greyscale bitmap of the data.
     */
    public Bitmap createBitmap()
    {
        // Empty bitmap
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for(int y=0;y<height;y++)
        {
            for(int x=0;x<width;x++)
            {
                int level = pixelData[y][x] & 0xff;
                // full alpha + level for red green and blue
                int colour = 0xff000000 | level | level << 8 | level << 16;
                bm.setPixel(x,y, colour);
            }
        }
        return bm;
    }

}
