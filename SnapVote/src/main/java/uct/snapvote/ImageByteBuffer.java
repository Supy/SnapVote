package uct.snapvote;

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
}
