package uct.snapvote.util;

import android.graphics.Point;

/** DetectedSquare
 *
 * This class is used to pass a detected square to the results screen.
 * It represents a square that has been detected successfully (or a false positive)
 *
 * Colour data is stored as an actual colour value, NOT as a index. This allows more
 * flexibility in the analysis stage of things.
 *
 * Position is stored in a topleft, bottomright scheme.
 *
 * Designed to be IMMUTABLE. Can't change values once initialised.
 *
 * Created by Ben on 2013/09/03.
 */
public class DetectedSquare {

    private Point topleft;
    private Point bottomright;

    private int colour;

    // CONSTRUCTORS
    public DetectedSquare(int x1, int y1, int x2, int y2, int c) {
        topleft = new Point(x1, y1);
        bottomright = new Point(x2, y2);
        colour = c;
    }

    // ACCESSORS
    // - Colour
    public int Colour() {
        return colour;
    }

    // - Position
    public int Left() {
        return topleft.x;
    }

    public int Right() {
        return bottomright.x;
    }

    public int Top() {
        return topleft.y;
    }

    public int Bottom() {
        return bottomright.y;
    }
}
