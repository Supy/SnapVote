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
}
