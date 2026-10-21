package com.fast.radio;

import android.content.Context;
import android.view.*;
import android.widget.*;
import java.util.*;

public class StationAdapter extends BaseAdapter {
    private final Context context;
    private final List<RadioStation> data;
    public StationAdapter(Context c, List<RadioStation> d) { context = c; data = d; }
    @Override public int getCount() { return data.size(); }
    @Override public Object getItem(int p) { return data.get(p); }
    @Override public long getItemId(int p) { return p; }
    @Override public View getView(int p, View convert, ViewGroup parent) {
        TextView t = new TextView(context);
        t.setText(data.get(p).toString());
        return t;
    }
}
