package uct.snapvote.filter;

import android.util.Log;

import uct.snapvote.ImageByteBuffer;

/**
 * Created by Justin on 2013/08/11.
 */
public class BlobDetectorFilter extends BaseRegionFilter {

    final char MAX_CHAR_VAL = 65535;

    public BlobDetectorFilter(ImageByteBuffer source){
        super(source, null);
    }

    @Override
    public void run(){
        char labelBuffer[] = new char[x2*y2];
        char labelTable[] = new char[MAX_CHAR_VAL];

        char currentLabel = 1;

        for(int y = 1; y < y2-1; y++){
            for(int x = 1; x < x2-1; x++){
                byte pixel = source.get(x,y);

                if(pixel == 0){
                    int topRow = (x2 * (y-1)) + x;

                    // Max char value;
                    char min = MAX_CHAR_VAL;

                    // Get neighbour labels
                    char top = labelBuffer[topRow];
                    char left = labelBuffer[topRow+x2-1];

                    if(top != 0 && top < min)
                        min = top;

                    if(left != 0 && left < min)
                        min = left;

                    int currentIndex = topRow + x2;

                    if(min == MAX_CHAR_VAL){
                        labelBuffer[currentIndex] = currentLabel;
                        labelTable[currentLabel] = currentLabel;
                        currentLabel++;
                    }else{
                        labelBuffer[currentIndex] = min;

                        if(top != 0)
                            labelTable[top] = min;

                        if(left != 0)
                            labelTable[left] = min;
                    }
                }
            }
        }

        Log.d("uct.snapvote", "Labels: "+(int)currentLabel+".");
    }
}
