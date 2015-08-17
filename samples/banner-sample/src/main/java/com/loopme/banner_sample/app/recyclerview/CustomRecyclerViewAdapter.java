package com.loopme.banner_sample.app.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.loopme.banner_sample.app.CustomListItem;
import com.loopme.banner_sample.app.R;
import com.loopme.LoopMeAdapter;
import com.loopme.LoopMeBanner;
import com.loopme.LoopMeBannerView;

import java.util.List;

public class CustomRecyclerViewAdapter extends
        RecyclerView.Adapter<CustomRecyclerViewAdapter.CustomViewHolder>
        implements LoopMeAdapter {

    static final int TYPE_DATA = 0;
    static final int TYPE_BANNER = 1;

    private List<Object> mData;

    private int mBannerPosition = -1;

    private LoopMeBanner mBanner;
    private BindViewListener mBindViewListener;

    public interface BindViewListener {
        public void onViewBinded();
    }

    public CustomRecyclerViewAdapter(List list, BindViewListener listener) {
        mData = list;
        mBindViewListener = listener;
    }

    @Override
    public boolean isAd(int position) {
        return (mBannerPosition == position) ? true : false;
    }

    public void addBannerToPosition(int position, LoopMeBanner banner) {
        if (position >= 0 && position < mData.size() && banner != null) {
            mData.add(position, banner);
            mBanner = banner;
            mBannerPosition = position;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (mBannerPosition == position) ? TYPE_BANNER : TYPE_DATA;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        int layoutId = (viewType == TYPE_BANNER) ? R.layout.ad_row : R.layout.list_row;
        View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(layoutId, null);

        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        if (isAd(i)) {
            if (mBanner != null && !mBanner.isViewBinded()) {
                mBanner.bindView(customViewHolder.banner);
                if (mBindViewListener != null) {
                    mBindViewListener.onViewBinded();
                }
            }
        } else {
            CustomListItem item = (CustomListItem) mData.get(i);

            customViewHolder.textView.setText(item.getTitle());
            customViewHolder.imageView.setImageResource(item.getIconId());
        }
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        } else {
            return 0;
        }
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected ImageView imageView;
        protected TextView textView;
        protected LoopMeBannerView banner;

        public CustomViewHolder(View view) {
            super(view);
            this.imageView = (ImageView) view.findViewById(R.id.thumbnail);
            this.textView = (TextView) view.findViewById(R.id.title);
            this.banner = (LoopMeBannerView) view.findViewById(R.id.ad_image);
        }
    }
}
