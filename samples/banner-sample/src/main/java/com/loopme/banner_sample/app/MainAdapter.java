package com.loopme.banner_sample.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.loopme.banner_sample.app.listview.ListViewActivity;
import com.loopme.banner_sample.app.listview.ListViewShrinkModeActivity;
import com.loopme.banner_sample.app.recyclerview.RecyclerViewActivity;
import com.loopme.banner_sample.app.recyclerview.RecyclerViewShrinkActivity;
import com.loopme.banner_sample.app.scrollview.ScrollViewActivity;
import com.loopme.banner_sample.app.scrollview.ScrollViewShrinkModeActivity;

import java.util.List;

public class MainAdapter  extends RecyclerView.Adapter<MainAdapter.CustomViewHolder> {

    private List<String> mData;
    private Context mContext;

    public MainAdapter(Context context, List list) {
        mData = list;
        mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.main_row, null);

        CustomViewHolder viewHolder = new CustomViewHolder(view,
                new ViewHolderClicks() {
                    @Override
                    public void onClick(String str) {
                        handleClick(str);
                    }
                });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        String item = mData.get(i);
        customViewHolder.textView.setText(item);
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        } else {
            return 0;
        }
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        protected TextView textView;
        private ViewHolderClicks mListaner;

        public CustomViewHolder(View view, ViewHolderClicks listener) {
            super(view);
            mListaner = listener;
            this.textView = (TextView) view.findViewById(R.id.title);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListaner.onClick(this.textView.getText().toString());
        }
    }

    public interface ViewHolderClicks {
        void onClick(String str);
    }

    private void handleClick(String str) {
        switch (str) {
            case Constants.SIMPLE:
                mContext.startActivity(new Intent(mContext, SimpleBannerActivity.class));
                break;

            case Constants.LISTVIEW:
                mContext.startActivity(new Intent(mContext, ListViewActivity.class));
                break;

            case Constants.LISTVIEW_SHRINK:
                mContext.startActivity(new Intent(mContext, ListViewShrinkModeActivity.class));
                break;

            case Constants.SCROLLVIEW:
                mContext.startActivity(new Intent(mContext, ScrollViewActivity.class));
                break;

            case Constants.SCROLLVIEW_SHRINK:
                mContext.startActivity(new Intent(mContext, ScrollViewShrinkModeActivity.class));
                break;

            case Constants.RECYCLERVIEW:
                mContext.startActivity(new Intent(mContext, RecyclerViewActivity.class));
                break;

            case Constants.RECYCLERVIEW_SHRINK:
                mContext.startActivity(new Intent(mContext, RecyclerViewShrinkActivity.class));
                break;

            default:
                break;
        }
    }
}
