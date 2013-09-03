package uct.snapvote;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uct.snapvote.components.BarGraph;
import uct.snapvote.util.DetectedSquare;
import uct.snapvote.util.DetectedSquareListSerialiser;

/**
 * Created by Ben on 2013/09/03.
 */
public class ResultActivity extends Activity {

    // Extra's data
    String imageUri;
    int[] colourArray;
    HashMap<Integer, List<DetectedSquare>> colourGroups;

    // Components
    BarGraph barGraph;

    // Touch control
    float lastX;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // == Assign components
        barGraph = (BarGraph) findViewById(R.id.results_bargraph);

        // == Extract Extras
        imageUri = getIntent().getStringExtra("ImageUri");
        colourArray = getIntent().getIntArrayExtra("ColourArray");

        int[] serialisedSquares = getIntent().getIntArrayExtra("SquareList");

        List<DetectedSquare> detectedSquareList = DetectedSquareListSerialiser.Deserialise(serialisedSquares);

        // == Group into colour groups
        colourGroups = new HashMap<Integer, List<DetectedSquare>>();
        for(DetectedSquare s : detectedSquareList) {
            int c = s.Colour();

            if(!colourGroups.containsKey(c)) {
                colourGroups.put(c, new ArrayList<DetectedSquare>());
            }

            colourGroups.get(c).add(s);
        }

        // == Render bars

        // FAKE
        barGraph.addBar(10,"Red", Color.RED);
        barGraph.addBar(0,"Magenta", Color.MAGENTA);
        barGraph.addBar(5,"Blue", Color.BLUE);
        barGraph.addBar(1,"Green", Color.GREEN);


    }


    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction())
        {
            // when user first touches the screen to swap
            case MotionEvent.ACTION_DOWN:
            {
                float fingerx = e.getX();
                break;
            }
            case MotionEvent.ACTION_UP:
            {
                float fingerx = e.getX();
                break;
            }
        }
        return false;
    }




}