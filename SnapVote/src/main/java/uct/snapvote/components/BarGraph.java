package uct.snapvote.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * A BarGraph View for displaying a simple bar chart below a title. The width of the bars scales
 * according to the width of the view. The graph is rendered to a hidden bitmap before being
 * rendered to the screen so that the bitmap can be easily saved to the gallery.
 */
public class BarGraph extends View {

    // size of the view in pixels
    private Rect size = null;

    // graph data
    private List<Bar> bardata;
    private String title;

    // drawing temps
    private Bitmap graphimg;
    private Paint barLabelPaint;
    private Paint barDomainLabelPaint;
    private Paint barTitlePaint;
    private Paint barBorderPaint;
    private Paint fillPaint;

    public BarGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        bardata = new ArrayList<Bar>();
        title = "Untitled";
        size = new Rect(0,0,1,1);
        setupDrawingTools();
    }

    /**
     * Called when view is resized. Stores new size so that drawing is correctly sized.
     * @param wspec Width measurespec
     * @param hspec Height measurespec
     */
    public void onMeasure(int wspec, int hspec) {
        int w = MeasureSpec.getSize(wspec);
        int h = MeasureSpec.getSize(hspec);

        size.set(0,0,w,h);

        super.onMeasure(wspec, hspec);
    }

    /**
     * Add a bar with the given value and colour
     * @param value Value of the bar
     * @param label Label displayed below the bar
     * @param colour Drawing colour for the bar
     */
    public void addBar(double value, String label, int colour) {
        bardata.add(new Bar(value, label, colour));
    }

    /**
     * Setup
     */
    private void setupDrawingTools() {
        barLabelPaint = new Paint();
        barLabelPaint.setColor(Color.WHITE);
        barLabelPaint.setTextSize(30);
        barLabelPaint.setTextAlign(Paint.Align.CENTER);

        barDomainLabelPaint = new Paint();
        barDomainLabelPaint.setColor(Color.DKGRAY);
        barDomainLabelPaint.setTextSize(30);
        barDomainLabelPaint.setTextAlign(Paint.Align.CENTER);

        barTitlePaint = new Paint();
        barTitlePaint.setColor(Color.BLACK);
        barTitlePaint.setTextSize(35);

        barBorderPaint = new Paint();
        barBorderPaint.setColor(Color.DKGRAY);
        barBorderPaint.setStyle(Paint.Style.STROKE);

        fillPaint = new Paint();
        fillPaint.setColor(Color.WHITE);
        fillPaint.setStyle(Paint.Style.FILL);



    }

    // title accessors
    public void setTitle(String s)
    {
        title = s;
    }

    public String getTitle()
    {
        return title;
    }

    /**
     * Deffered draw method. Draws first onto a bitmap
     * this is so that we can export this bitmap later.
     */
    private void drawToBitmap()
    {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        graphimg = Bitmap.createBitmap(size.width(), size.height(), conf);

        Canvas canvas = new Canvas(graphimg);

        // calculate size of graph area
        Rect innerRect = new Rect( 25, 100, size.width()-25, size.height()-75 );

        // Fill white background
        canvas.drawRect(size, fillPaint);

        // Draw background border
        barBorderPaint.setColor(Color.GRAY);
        canvas.drawRect(size, barBorderPaint);

        // draw title at the top
        canvas.drawText(title, 25, 65, barTitlePaint);

        if(bardata.size() > 0) {

            // need to know maximum value for height scaling
            int maxvalue = 0;
            for(Bar b: bardata) if(b.value > maxvalue) maxvalue = (int)b.value+1;

            // width and x holders
            int gap = 30;
            int barwidth = (innerRect.width() - (bardata.size() * gap + gap))/bardata.size();
            int cx = innerRect.left + gap;

            // Draw the bars
            for(Bar b : bardata) {

                // calculate height and drawing top value
                int height = (int)( (b.value / maxvalue) * innerRect.height());
                int top = innerRect.height() - height;

                // fill bar
                fillPaint.setColor(b.colour);
                canvas.drawRect(cx, innerRect.top + top, cx+barwidth, innerRect.bottom, fillPaint);

                // shadow
                int shadowsize = 7;
                if (height > shadowsize) {
                    fillPaint.setColor(Color.LTGRAY);
                    canvas.drawRect(cx+barwidth, innerRect.top + top + shadowsize, cx+barwidth+shadowsize, innerRect.bottom, fillPaint);
                }

                // border of bar
                canvas.drawRect(cx, innerRect.top + top, cx+barwidth, innerRect.bottom, barBorderPaint);

                // label center
                int tx = cx + barwidth/2;
                int ty = innerRect.top + top + height/2;

                // can't draw label inside bar if it is too small
                if(height > 150) {
                    barLabelPaint.setColor(Color.WHITE);
                }
                else {
                    barLabelPaint.setColor(b.colour);
                    ty = innerRect.bottom - height - 10;
                }

                // draw label
                canvas.drawText("" + (int)b.value, tx, ty, barLabelPaint);

                // draw text on axis
                canvas.drawText(b.label, tx, innerRect.bottom + 10 + barDomainLabelPaint.getTextSize(), barDomainLabelPaint);

                // move on to next bar
                cx += barwidth + gap;
            }

        }

        // Draw inner border
        barBorderPaint.setColor(Color.LTGRAY);
        canvas.drawRect(innerRect, barBorderPaint);

        // Draw bottom axis
        barBorderPaint.setColor(Color.BLACK);
        canvas.drawLine(innerRect.left, innerRect.bottom, innerRect.right, innerRect.bottom, barBorderPaint);

        fillPaint.setColor(Color.WHITE);
    }

    /* onDraw
    The actual draw method.
    Called automatically when view requires repainting, force using invalidate()
     */

    /**
     * The actual draw method.
     * Called automatically when view requires repainting, force using invalidate()
     */
    public void onDraw(Canvas canvas) {
        drawToBitmap();
        canvas.drawBitmap(graphimg, 0,0, fillPaint);
    }


    /**
     * Get the hidden bitmap that the graph has been drawn to.
     * @return bitmap
     */
    public Bitmap getBitmap()
    {
        return graphimg;
    }


    // ================================================================

    /**
     * Simple Bar structure to hold the properties of each bar.
     * Value is a double in order to accomodate any size even if its unneccessary.
     */
    public class Bar {
        public double value;
        public String label;
        public int colour;

        public Bar(double v, String l, int c) {
            value = v;
            label = l;
            colour = c;
        }
    }

}
