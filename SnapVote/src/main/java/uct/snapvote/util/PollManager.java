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
 * The PollManager provides an interface to load and save a list of polls from the polldata
 * json file. We chose to use JSON because of the built-in JSON support and to avoid
 * the complications for complex string parsing.
 */
public class PollManager {

    private final static String DATA_FILE = "polldata.json";

    /**
     * Save a poll to the end of the file
     * @param title The title of the poll
     * @param entry The JSONObject containing the properties of the poll
     * @param a The source activity, needed in order to write to disk.
     */
    public static void saveResult(String title, JSONObject entry, Activity a){
        try{
            entry.put("title", title);
            writeData(entry.toString()+'\n', a);
            Log.d("uct.snapvote", "New poll result saved.");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Write a string line to the end of the polldata file.
     * @throws IOException
     */
    private static void writeData(String data, Activity a) throws IOException{
        FileOutputStream fOut = a.openFileOutput(DATA_FILE, Context.MODE_APPEND) ;
        OutputStreamWriter osw = new OutputStreamWriter(fOut) ;
        osw.write(data) ;
        osw.flush() ;
        osw.close() ;
    }

    // overwrite the file with nothing. This is easier than deleting the file.
    public static void clearFile(Activity a) throws IOException
    {
        FileOutputStream fOut = a.openFileOutput(DATA_FILE, 0) ;
        OutputStreamWriter osw = new OutputStreamWriter(fOut) ;
        osw.flush() ;
        osw.close() ;
    }

    /**
     * Get an Array of all polls from the polldata file.
     * @return A JSONArray as it is read from the file.
     */
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
