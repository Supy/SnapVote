package uct.snapvote.util;

import java.util.ArrayList;
import java.util.List;

public class Blob {
    public static final int MIN_MASS = 44;
    public static final double MAX_RATIO = 1.65;
    public static final double MIN_AREA = 0.35;
    public int xMin = Integer.MAX_VALUE;
    public int xMax;
    public int yMin = Integer.MAX_VALUE;
    public int yMax;
    public int mass;
    public List<BlobSampler.Sample> samples;
    public int assignedColour;

    public void addPixel(int pixelX, int pixelY) {
        if (pixelX < xMin)
            xMin = pixelX;
        else if (pixelX > xMax)
            xMax = pixelX;

        if (pixelY < yMin)
            yMin = pixelY;
        else if (pixelY > yMax)
            yMax = pixelY;

        mass++;
    }

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

    public void attachSample(BlobSampler.Sample sample) {
        if (samples == null)
            samples = new ArrayList<BlobSampler.Sample>();

        samples.add(sample);
    }

    public String toString() {
        return String.format("X: %4d -> %4d, Y: %4d -> %4d, mass: %6d", xMin, xMax, yMin, yMax, mass);
    }
}