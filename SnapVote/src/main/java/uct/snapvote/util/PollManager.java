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

/**
 * Created by Justin on 2013/09/15.
 */
public class PollManager {

    private final static String DATA_FILE = "polldata.json";

    public static void saveResult(String title, JSONObject entry, Activity a){
        try{
            entry.put("title", title);
            writeData(entry.toString()+'\n', a);
            Log.d("uct.snapvote", "New poll result saved.");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void writeData(String data, Activity a) throws IOException{
        FileOutputStream fOut = a.openFileOutput(DATA_FILE, Context.MODE_APPEND) ;
        OutputStreamWriter osw = new OutputStreamWriter(fOut) ;
        osw.write(data) ;
        osw.flush() ;
        osw.close() ;
    }

    public static JSONArray getAllPolls(Activity a){
        JSONArray entries = new JSONArray();

        try {
            FileInputStream fIn = a.openFileInput(DATA_FILE) ;
            InputStreamReader isr = new InputStreamReader(fIn) ;
            BufferedReader br = new BufferedReader(isr) ;

            String str;
            while ( (str = br.readLine()) != null ){
                // We rebuild each row so that we can simply append during writing
                // instead of having to rebuild the entire array to add one new result.
                try{
                    entries.put(new JSONObject(str));
                }catch (JSONException e){}
            }
            isr.close () ;

            Log.d("uct.snapvote", "Read "+entries.length()+" poll results from file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entries;
    }
}
