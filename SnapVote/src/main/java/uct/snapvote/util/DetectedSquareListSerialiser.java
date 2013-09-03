package uct.snapvote.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ben on 2013/09/03.
 */
public class DetectedSquareListSerialiser {

    public static int[] Serialise(List<DetectedSquare> squareList) {
        int[] out = new int[squareList.size()*5];
        int index = 0;

        for(DetectedSquare square : squareList) {
            out[index++] = square.Left();
            out[index++] = square.Top();
            out[index++] = square.Right();
            out[index++] = square.Bottom();
            out[index++] = square.Colour();
        }

        return out;
    }

    public static List<DetectedSquare> Deserialise(int[] input) {

        List<DetectedSquare> out = new ArrayList<DetectedSquare>();

        int numsquares = input.length / 5;

        for(int n=0;n<numsquares;n++) {
            out.add(new DetectedSquare(
                    input[n*5],             // x1
                    input[n*5+1],           // y1
                    input[n*5+2],           // x2
                    input[n*5+3],           // y2
                    input[n*5+4]            // colour
            ));
        }
        return out;
    }



}
