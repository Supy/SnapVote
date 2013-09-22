package uct.snapvote.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A continuous region in the edge detected image.
 * A blob is defined by its X and Y limits as well as
 * the number of pixels inside it.
 *
 * It also contains a list of pixel sample positions and results for use in the
 * validvotefilter.
 */
public class Blob {
    // constants
    public static final int MIN_MASS = 44;
    public static final double MAX_RATIO = 1.65;
    public static final double MIN_AREA = 0.35;

    // X and Y limits
    public int xMin = Integer.MAX_VALUE;
    public int xMax;
    public int yMin = Integer.MAX_VALUE;
    public int yMax;
    // number of pixels
    public int mass;

    // attached pixel samples
    public List<BlobSampler.Sample> samples;
    public int assignedColour;

    /**
     * Adds a position to this blob. If it expands the blob, modify the X or Y limits. Add 1 to the mass
     * @param pixelX X coordinate
     * @param pixelY Y coordinate
     */
    public void addPixel(int pixelX, int pixelY) {
        if (pixelX < xMin)
            xMin = pixelX;
        else if (pixelX > xMax)
            xMax = pixelX;

        if (pixelY < yMin)
            yMin = pixelY;
        else if (pixelY > yMax)
            yMax = pixelY;

        //increase the max
        mass++;
    }

    /**
     * Check the blob for basic validity: Minimum Mass, Aspect ratio, and density.
     * @return True if the blob is valid.
     */
    public boolean valid() {

        if (mass < MIN_MASS)
            return false;

        int width = xMax - xMin;
        int height = yMax - yMin;

        double ratio;

        if (width >= height) {
            ratio = width * 1.0 / height;
        } else {
            ratio = height * 1.0 / width;
        }

        if (ratio > MAX_RATIO) return false;

        double areaFilled = mass * 1.0 / (width * height);

        return areaFilled >= MIN_AREA;

    }

    /**
     * Attach a sample to the given blob
     * @param sample The sample instance to attach
     */
    public void attachSample(BlobSampler.Sample sample) {
        if (samples == null)
            samples = new ArrayList<BlobSampler.Sample>();

        samples.add(sample);
    }

    /**
     * To string : debug info
     */
    public String toString() {
        return String.format("X: %4d -> %4d, Y: %4d -> %4d, mass: %6d", xMin, xMax, yMin, yMax, mass);
    }
}