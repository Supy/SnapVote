package uct.snapvote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uct.snapvote.components.BarGraph;
import uct.snapvote.util.DetectedSquare;
import uct.snapvote.util.DetectedSquareListSerialiser;
import uct.snapvote.util.ImageInputStream;

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

        barGraph = (BarGraph) findViewById(R.id.results_bargraph);
        Button btnSave = (Button) findViewById(R.id.btnSave);

        // == Extract Extras
        imageUri = getIntent().getStringExtra("ImageUri");
        colourArray = getIntent().getIntArrayExtra("ColourArray");

        int[] serialisedSquares = getIntent().getIntArrayExtra("SquareList");

        List<DetectedSquare> detectedSquareList = DetectedSquareListSerialiser.Deserialise(serialisedSquares);

        // == Group into colour groups
        // TODO: once we're finished, we can just keep colour tallies. No need for these objects for use in the thumbnail.
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

        // TODO: this can be removed after we're done
        saveOverlayImage();

        // Setup prompt for poll title
        final EditText inPollTitle = new EditText(ResultActivity.this);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(ResultActivity.this)
                        .setTitle("Save poll results")
                        .setMessage("Please enter a title for this poll")
                        .setView(inPollTitle)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String title = inPollTitle.getText().toString();
                                savePoll(title);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
            }
        });
        // --- End prompt setup
    }

    private void savePoll(String pollTitle){
        // TODO: Convert colourGroup map to a map of colour-count and save.
    }

    private void saveOverlayImage() {
        try {
            ImageInputStream imageInputStream = new ImageInputStream(imageUri, this.getContentResolver());

            int imageHeight = imageInputStream.height;
            int imageWidth = imageInputStream.width;

            // read image attributes
            BitmapFactory.Options iOptions = new BitmapFactory.Options();
            iOptions.inJustDecodeBounds = true;
            iOptions.inPreferredConfig = Bitmap.Config.RGB_565;
            iOptions.inSampleSize = 4;

            Bitmap bm = BitmapFactory.decodeStream(imageInputStream.getInputStream(), null, iOptions);

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

            // 7. == Save to sdcard0/Pictures
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bmcpy.compress(Bitmap.CompressFormat.JPEG, 80, bytes);
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File f = new File(path, "test-results.jpg");

            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();

        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}