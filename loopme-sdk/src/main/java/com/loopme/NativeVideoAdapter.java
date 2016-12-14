package com.loopme;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.loopme.common.AdChecker;
import com.loopme.common.Logging;
import com.loopme.common.LoopMeError;
import com.loopme.common.MinimizedMode;
import com.loopme.common.Utils;

public class NativeVideoAdapter extends BaseAdapter implements AdChecker,
        NativeVideoController.DataChangeListener {

    private static final String LOG_TAG = NativeVideoAdapter.class.getSimpleName();

    private BaseAdapter mOriginAdapter;
    private Context mContext;
    private NativeVideoController mNativeVideoController;
    private LayoutInflater mInflater;
    private AbsListView.OnScrollListener mOriginScrollListener;

    private AbsListView mListView;

    public NativeVideoAdapter(@NonNull BaseAdapter originAdapter,
                              @NonNull Context context,
                              @NonNull AbsListView listView) {

        mOriginAdapter = originAdapter;
        mContext = context.getApplicationContext();
        mListView = listView;

        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mNativeVideoController = new NativeVideoController(mContext);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (mOriginScrollListener != null) {
                    mOriginScrollListener.onScrollStateChanged(view, scrollState);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mOriginScrollListener != null) {
                    mOriginScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                }
                mNativeVideoController.onScroll(view, NativeVideoAdapter.this);
            }
        });

        mOriginAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                triggerUpdateProcessor();
            }

            @Override
            public void onInvalidated() {
                notifyDataSetInvalidated();
            }
        });
    }

    private void triggerUpdateProcessor() {
        notifyDataSetChanged();
        mNativeVideoController.refreshAdPlacement(mOriginAdapter.getCount());
    }

    /**
     * Clean resources.
     */
    public void destroy() {
        mNativeVideoController.destroy();
    }

    /**
     * Pauses ads (video)
     * NOTE: trigger in Activity onPause()
     */
    public void onPause() {
        Logging.out(LOG_TAG, "onPause");
        mNativeVideoController.onPause();
    }

    /**
     * Resumes ads (video)
     * NOTE: trigger in Activity onResume()
     */
    public void onResume() {
        Logging.out(LOG_TAG, "onResume");
        mNativeVideoController.onResume(mListView, this);
    }

    /**
     * Adds banner ad to defined position.
     * @param appKey - app key
     * @param position - position in list
     */
    public void putAdWithAppKeyToPosition(String appKey, int position) {
        if (position < mOriginAdapter.getCount()) {
            mNativeVideoController.putAdWithAppKeyToPosition(appKey, position);
        } else {
            Logging.out(LOG_TAG, "Wrong position " + position);
        }
    }

    /**
     * Starts loading all ads which were added with 'putAdWithAppKeyToPosition' method
     */
    public void loadAds() {
        if (Build.VERSION.SDK_INT < 14) {
            LoopMeError error = new LoopMeError("Not supported Android version. Expected Android 4.0+");
            mNativeVideoController.onLoadFail(error);
            return;
        }
        if (Utils.isOnline(mContext)) {
            mNativeVideoController.loadAds(mOriginAdapter.getCount(), this);
        } else {
            LoopMeError error = new LoopMeError("No connection");
            mNativeVideoController.onLoadFail(error);
        }
    }

    public void setMinimizedMode(MinimizedMode mode) {
        if (mode != null) {
            Logging.out(LOG_TAG, "Set minimized mode");
            mNativeVideoController.setMinimizedMode(mode);
        }
    }

    /**
     * Used in case if `ListView` handles scrolling in custom way
     * @param scrollListener - origin scroll listener
     */
    public void setOriginScrollListener(AbsListView.OnScrollListener scrollListener) {
        mOriginScrollListener = scrollListener;
    }

    /**
     * Set listener to receive notifications during loading/showing process
     * @param listener - listener
     */
    public void setAdListener(LoopMeBanner.Listener listener) {
        mNativeVideoController.setListener(listener);
    }

    public void setViewBinder(NativeVideoBinder binder) {
        mNativeVideoController.setViewBinder(binder);
    }

    @Override
    public boolean hasStableIds() {
        return mOriginAdapter.hasStableIds();
    }

    @Override
    public int getItemViewType(int position) {
        if (isAd(position)) {
            return mOriginAdapter.getViewTypeCount();
        } else {
            return mOriginAdapter.getItemViewType(position);
        }
    }

    @Override
    public int getViewTypeCount() {
        return mOriginAdapter.getViewTypeCount() + 1;
    }

    @Override
    public boolean isEmpty() {
        return mOriginAdapter.isEmpty();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mOriginAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mOriginAdapter.unregisterDataSetObserver(observer);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return mOriginAdapter.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int position) {
        return mOriginAdapter.isEnabled(position);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return mOriginAdapter.getDropDownView(position, convertView, parent);
    }

    @Override
    public int getCount() {
        return mOriginAdapter.getCount() + mNativeVideoController.getAdsCount();
    }

    @Override
    public Object getItem(int position) {
        if (isAd(position)) {
            return mNativeVideoController.getNativeVideoAd(position);
        } else {
            int initPosition = mNativeVideoController.getInitialPosition(position);
            return mOriginAdapter.getItem(initPosition);
        }
    }

    @Override
    public long getItemId(int position) {
        if (isAd(position)) {
            return -System.identityHashCode(mNativeVideoController.getNativeVideoAd(position));
        } else {
            int initPosition = mNativeVideoController.getInitialPosition(position);
            return mOriginAdapter.getItemId(initPosition);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (isAd(position)) {
            View row;
            final LoopMeBanner item = mNativeVideoController.getNativeVideoAd(position);
            row = mNativeVideoController.getAdView(mInflater, parent, item, position);
            return row;

        } else {
            int initPosition = mNativeVideoController.getInitialPosition(position);
            return mOriginAdapter.getView(initPosition, convertView, parent);
        }
    }

    @Override
    public boolean isAd(int i) {
        return mNativeVideoController.getNativeVideoAd(i) != null;
    }

    @Override
    public void onDataSetChanged() {
        mOriginAdapter.notifyDataSetChanged();
    }
}