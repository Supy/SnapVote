package uct.snapvote.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import java.util.List;

/**
 * Created by Ben on 2013/09/18.
 */
public class PollListAdapter extends BaseAdapter {

    private Context context;
    private List<Pair<String, String>> contents;

    public PollListAdapter(Context c, List<Pair<String, String>> titledates)
    {
        this.context = c;
        this.contents = titledates;
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
        return 0;
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

        text1.setText(contents.get(i).left);
        text2.setText("" + contents.get(i).right);

        return twoLineListItem;
    }
}
