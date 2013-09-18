package uct.snapvote.util;

import android.content.Context;
import android.util.Pair;
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
 * Created by Ben on 2013/09/18.
 */
public class PollListAdapter extends BaseAdapter {

    private Context context;
    private List<JSONObject> contents;

    public PollListAdapter(Context c, JSONArray polls)
    {
        this.context = c;
        this.contents =new ArrayList<JSONObject>();

        for(int i=0;i<polls.length();i++)
        {
            try {
                this.contents.add(polls.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


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

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        TwoLineListItem twoLineListItem;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            twoLineListItem = (TwoLineListItem) inflater.inflate(
                    android.R.layout.simple_list_item_2, null);
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
