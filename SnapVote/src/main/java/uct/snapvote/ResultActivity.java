package uct.snapvote;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        for(Map.Entry<Integer, List<DetectedSquare>> group: colourGroups.entrySet()) {
            barGraph.addBar(group.getValue().size(), Integer.toHexString(group.getKey()), group.getKey());
        }

        ImageView mImage = (ImageView) findViewById(R.id.results_imageview);

        loadThumbnail(mImage, imageUri);

    }


    private void loadThumbnail(ImageView iv, String uri) {

        Uri contentURI = Uri.parse(uri);
        ContentResolver cr = getContentResolver();

        InputStream in = null;
        try {
            in = cr.openInputStream(contentURI);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // read image attributes
        BitmapFactory.Options iOptions = new BitmapFactory.Options();
        iOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in,null,iOptions);
        int imageHeight = iOptions.outHeight;
        int imageWidth = iOptions.outWidth;

        try {
            in = cr.openInputStream(contentURI);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        iOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        int inSampleSize = 1;

        if (imageHeight > iv.getHeight()) {
            inSampleSize = Math.round((float)imageHeight / (float)200);
        }

        int expectedWidth = imageWidth / inSampleSize;

        if (expectedWidth > iv.getWidth()) {
            //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
            inSampleSize = Math.round((float)imageWidth / (float)400);
        }


        iOptions.inSampleSize = inSampleSize;

        // Decode bitmap with inSampleSize set
        iOptions.inJustDecodeBounds = false;

        Bitmap bm = BitmapFactory.decodeStream(in, null, iOptions);

        android.graphics.Bitmap.Config bitmapConfig = bm.getConfig();
        Bitmap bmcpy = bm.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bmcpy);

        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);

        float divx = (float)bm.getWidth() / imageWidth;
        float divy = (float)bm.getHeight() / imageHeight;

        for(List<DetectedSquare> group : colourGroups.values()) {
            for(DetectedSquare square : group) {

                p.setColor(square.Colour());

                int x = (int)(divx * square.Left());
                int y = (int)(divy * square.Top());

                int x2 = (int)(divx * square.Right())+1;
                int y2 = (int)(divy * square.Bottom())+1;

                canvas.drawRect(x,y,x2,y2,p);

            }
        }


        iv.setImageBitmap(bmcpy);

    }





}