package uct.snapvote.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ben on 2013/09/03.
 */
public class BarGraph extends View {

    private Rect size = null;

    private List<Bar> bardata;

    Paint barLabelPaint;
    Paint barDomainLabelPaint;

    public BarGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        bardata = new ArrayList<Bar>();
        setupDrawingTools();
    }

    /* onMeasure
    Called when view is resized.
    Stores new size so that drawing is correctly sized.
     */
    public void onMeasure(int wspec, int hspec) {
        int w = MeasureSpec.getSize(wspec);
        int h = MeasureSpec.getSize(hspec);

        size = new Rect(0,0,w,h);

        super.onMeasure(wspec, hspec);
    }


    public void addBar(double v, String l, int c) {
        bardata.add(new Bar(v, l, c));
    }

    public void clear() {
        bardata.clear();
    }

    public List<Bar> getData() {
        return bardata;
    }

    private void setupDrawingTools() {
        barLabelPaint = new Paint();
        barLabelPaint.setColor(Color.WHITE);
        barLabelPaint.setTextSize(30);
        barLabelPaint.setTextAlign(Paint.Align.CENTER);

        barDomainLabelPaint = new Paint();
        barDomainLabelPaint.setColor(Color.DKGRAY);
        barDomainLabelPaint.setTextSize(30);
        barDomainLabelPaint.setTextAlign(Paint.Align.CENTER);
    }

    /* onDraw
    The actual draw method.
    Called automatically when view requires repainting, force using invalidate()
     */
    public void onDraw(Canvas canvas) {

        // calculate size of graph area
        Rect innerRect = new Rect( 25, 25, size.width()-25, size.height()-75 );

        // mutable paint object
        Paint p = new Paint();

        // Fill white background
        p.setColor(Color.WHITE);
        canvas.drawRect(size, p);

        // Draw background border
        p.setColor(Color.GRAY);
        p.setStrokeWidth(2);
        p.setStyle(Paint.Style.STROKE);
        canvas.drawRect(size, p);


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
            p.setColor(b.colour);
            p.setStyle(Paint.Style.FILL);
            canvas.drawRect(cx, innerRect.top + top, cx+barwidth, innerRect.bottom, p);

            // shadow
            int shadowsize = 7;
            if (height > shadowsize) {
                p.setColor(Color.LTGRAY);
                p.setStyle(Paint.Style.FILL);
                canvas.drawRect(cx+barwidth, innerRect.top + top + shadowsize, cx+barwidth+shadowsize, innerRect.bottom, p);
            }

            // border of bar
            p.setColor(Color.DKGRAY);
            p.setStyle(Paint.Style.STROKE);
            canvas.drawRect(cx, innerRect.top + top, cx+barwidth, innerRect.bottom, p);

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

        // Draw inner border
        p.setColor(Color.LTGRAY);
        p.setStyle(Paint.Style.STROKE);
        canvas.drawRect(innerRect, p);

        // Draw bottom axis
        p.setColor(Color.BLACK);
        p.setStyle(Paint.Style.STROKE);
        canvas.drawLine(innerRect.left, innerRect.bottom, innerRect.right, innerRect.bottom, p);


    }




    // ================================================================
    public class Bar {
        public double value;
        public String label;
        public int colour;

        public Bar(double v, String l) {
            value = v;
            label = l;
            colour = Color.GRAY;
        }

        public Bar(double v, String l, int c) {
            value = v;
            label = l;
            colour = c;
        }
    }

}
