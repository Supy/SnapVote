package uct.snapvote.ImageBitBufferOps;

/**
 * Created by Ben on 2013/09/13.
 */
public class StructuringElement {

    boolean[][] data;
    private int width;
    private int height;

    public StructuringElement(boolean[][] array)
    {
        data = array.clone();
        height = array.length;
        width = array[0].length;
    }

    public int GetWidth() { return width; }
    public int GetHeight() { return height; }


    // ==== STATICS

    public static StructuringElement Generate3x3()
    {
        return new StructuringElement(new boolean[][]{
                {true, true, true},
                {true, true, true},
                {true, true, true}});
    }

    public static StructuringElement Generate5x5()
    {
        return new StructuringElement(new boolean[][]{
                {true, true, true, true, true},
                {true, true, true, true, true},
                {true, true, true, true, true},
                {true, true, true, true, true},
                {true, true, true, true, true}});
    }




}
