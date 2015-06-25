package com.example.mopub_nativead;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.loopme.LoopMeAdapter;
import com.loopme.LoopMeBanner;
import com.loopme.LoopMeBannerView;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class CustomBaseAdapter extends BaseAdapter implements LoopMeAdapter {

    static final int TYPE_DATA = 0;
    static final int TYPE_BANNER = 1;

    static final int TYPE_COUNT = 2;

    private LayoutInflater mInflater;
    private List<Object> mData = new ArrayList<Object>();

    private TreeSet mBannerSet = new TreeSet();

    private LoopMeBanner mBanner;

    public CustomBaseAdapter(Context context, List<Object> list) {
        mData = list;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addBannerToPosition(int position, LoopMeBanner banner) {
        if (position >= 0 && position < mData.size()) {
            mBanner = banner;
            mData.add(position, mBanner);
            mBannerSet.add(position);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mBannerSet.contains(position)) {
            return TYPE_BANNER;
        }
        return TYPE_DATA;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
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
        ViewHolder holder = null;
        int type = getItemViewType(position);

        if (v == null || v.getTag() == null) {
            holder = new ViewHolder();
            switch (type) {
                case TYPE_DATA:
                    v = mInflater.inflate(R.layout.list_data_layout, null);
                    holder.title = (TextView) v.findViewById(R.id.title);
                    holder.subtitle = (TextView) v.findViewById(R.id.subtitle);
                    holder.icon = (ImageView) v.findViewById(R.id.icon);
                    break;

                case TYPE_BANNER:
                    //inflate xml layout for LoopMe banner
                    v = mInflater.inflate(R.layout.list_ad_layout, null);
                    holder.banner_ad = (LoopMeBannerView) v.findViewById(R.id.banner_ad_spot);
                    break;
            }
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        if (holder.banner_ad != null) {
            if (mBanner != null) {
                mBanner.bindView(holder.banner_ad);
            }
        }
        if (holder.title != null) {
            holder.title.setText(((CustomListItem)mData.get(position)).getTitle());
        }
        if (holder.subtitle != null) {
            holder.subtitle.setText(((CustomListItem)mData.get(position)).getSubtitle());
        }
        if (holder.icon != null) {
            holder.icon.setImageResource(((CustomListItem)mData.get(position)).getIconId());
        }
        return v;
    }

    @Override
    public boolean isAd(int i) {
        return mBannerSet.contains(i);
    }

    private static class ViewHolder {
        public TextView title;
        public TextView subtitle;
        public ImageView icon;

        public LoopMeBannerView banner_ad;
    }
}