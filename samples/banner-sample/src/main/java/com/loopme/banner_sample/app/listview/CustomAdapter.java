package com.loopme.banner_sample.app.listview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopme.banner_sample.app.CustomListItem;
import com.loopme.banner_sample.app.R;

import java.util.List;

public class CustomAdapter extends BaseAdapter {
    private List<CustomListItem> mData;
    private LayoutInflater mInflater;

    public CustomAdapter(Context context, List<CustomListItem> list) {
        mData = list;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;

        if (v == null || v.getTag() == null) {
            holder = new ViewHolder();
            v = mInflater.inflate(R.layout.list_data_layout, null);
            holder.title = (TextView) v.findViewById(R.id.title);
            holder.subtitle = (TextView) v.findViewById(R.id.subtitle);
            holder.icon = (ImageView) v.findViewById(R.id.icon);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        if (holder.title != null) {
            holder.title.setText((mData.get(position)).getTitle());
        }
        if (holder.subtitle != null) {
            holder.subtitle.setText((mData.get(position)).getSubtitle());
        }
        if (holder.icon != null) {
            holder.icon.setImageResource((mData.get(position)).getIconId());
        }
        return v;
    }

    private static class ViewHolder {
        public TextView title;
        public TextView subtitle;
        public ImageView icon;
    }
}
