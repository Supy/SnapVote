package uct.snapvote.filter;

import android.util.Log;

import java.util.BitSet;

import uct.snapvote.ImageBitBuffer;
import uct.snapvote.ImageByteBuffer;

/**
 * Created by Ben on 8/7/13.
 *
 */
public class ConvertToBitImageFilter extends ThreadedBaseRegionFilter {


    private ImageBitBuffer target;


    public ConvertToBitImageFilter(ImageByteBuffer source, ImageBitBuffer target) {
        super(source, null);
        this.target = target;
    }

    public ConvertToBitImageFilter(ImageByteBuffer source, ImageBitBuffer target, int x1, int y1, int x2, int y2) {
        super(source, null, x1, y1, x2, y2);
        this.target = target;
    }

    @Override
    public void run() {
        Log.d("uct.snapvote", "start " + this.x1 + " " + this.y1);
        for(int y= y1;y< y2;y++) {
            for(int x= x1;x< x2;x++) {
                if(source.get(x, y) == (byte)255) {
                    target.set(x, y);
                }
            }
        }
        Log.d("uct.snapvote", "finished " + this.x1 + " " + this.y1);
    }
}
