package uct.snapvote.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

/**
 * Created by Justin on 2013/09/15.
 */
public class PollManager {

    private final static String DATA_FILE = "polldata.json";

    public static void saveResult(String title, Map<Integer, Integer> result, Activity a){
        JSONObject entry = new JSONObject();

        try{
            entry.put("title", title);

            // Create an array of JSONObjects, each one a colour-count combination.
            JSONArray results = new JSONArray();
            for(Map.Entry<Integer, Integer> e : result.entrySet()){
                JSONObject r = new JSONObject();
                r.put(e.getKey().toString(), e.getValue());
                results.put(r);
            }
            entry.put("results", results);

            writeData(entry.toString(), a);

            Log.d("uct.snapvote", "New poll result saved.");
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    private static void writeData(String data, Activity a){
        try {
            FileOutputStream fOut = a.openFileOutput(DATA_FILE, Context.MODE_APPEND) ;
            OutputStreamWriter osw = new OutputStreamWriter(fOut) ;
            osw.write(data) ;
            osw.flush() ;
            osw.close() ;
        } catch (Exception e) {
            e.printStackTrace () ;
        }
    }

    private static JSONArray readData(Activity a){
        JSONArray entries = new JSONArray();

        try {
            FileInputStream fIn = a.openFileInput(DATA_FILE) ;
            InputStreamReader isr = new InputStreamReader(fIn) ;
            BufferedReader br = new BufferedReader(isr) ;

            String str;
            while ( (str = br.readLine()) != null ){
                // We rebuild each row so that we can simply append during writing
                // instead of having to rebuild the entire array to add one new result.
                entries.put(new JSONObject(str));
            }
            isr.close () ;

            Log.d("uct.snapvote", "Read "+entries.length()+" poll results from file.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entries;
    }
}
