package uct.snapvote.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom ListAdapter for Poll objects. It stores the polls as a list of JSONObject's
 * pulled from a JSONArray. ListAdapter makes it easy to display a 2 line list item
 * in the list on the MainActivity.
 */
public class PollListAdapter extends BaseAdapter {

    private Context context;
    private List<JSONObject> contents;

    // constructor
    public PollListAdapter(Context c, JSONArray polls)
    {
        this.context = c;
        rebuild(polls);
    }

    /**
     * Construct a list of JSONobjects from a JSONArray.
     * @param polls A list of polls
     */
    public void rebuild(JSONArray polls)
    {
        this.contents =new ArrayList<JSONObject>();
        for(int i=0;i<polls.length();i++)
        {
            // print the exception but dont notify since this shouldn't ever happen
            try {
                this.contents.add(polls.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // == Accessors
    @Override
    public int getCount() {
        return contents.size();
    }

    @Override
    public Object getItem(int i) {
        return contents.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
    // ===========

    /**
     * Construct the view for the given item id. Use a layour inflator to build the layout
     * from the specified layout file.
     * @param i Item index
     * @param view Possibly existing view
     * @param viewGroup (unused)
     * @return the constructed View
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        TwoLineListItem twoLineListItem;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            twoLineListItem = (TwoLineListItem) inflater.inflate( android.R.layout.simple_list_item_2, null);
        } else {
            twoLineListItem = (TwoLineListItem) view;
        }

        TextView text1 = twoLineListItem.getText1();
        TextView text2 = twoLineListItem.getText2();

        text1.setText(contents.get(i).optString("title", "<title>"));
        text2.setText("" + contents.get(i).optString("date","<date>"));

        return twoLineListItem;
    }
}
