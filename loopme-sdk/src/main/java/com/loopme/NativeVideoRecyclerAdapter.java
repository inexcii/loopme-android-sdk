package com.loopme;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.loopme.common.AdChecker;
import com.loopme.common.Logging;
import com.loopme.common.LoopMeError;
import com.loopme.common.MinimizedMode;
import com.loopme.common.Utils;

public class NativeVideoRecyclerAdapter extends RecyclerView.Adapter
implements AdChecker, NativeVideoController.DataChangeListener {

    private static final String LOG_TAG = NativeVideoRecyclerAdapter.class.getSimpleName();

    static final int TYPE_AD = 1000;

    private RecyclerView.Adapter mOriginAdapter;
    private NativeVideoController mNativeVideoController;
    private Context mContext;
    private LayoutInflater mInflater;

    private RecyclerView mRecyclerView;

    public NativeVideoRecyclerAdapter(@NonNull RecyclerView.Adapter originAdapter,
                                      @NonNull Context context,
                                      @NonNull RecyclerView recyclerView) {

        mContext = context;
        mOriginAdapter = originAdapter;
        mRecyclerView = recyclerView;

        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mNativeVideoController = new NativeVideoController(mContext);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mNativeVideoController.onScroll(recyclerView, NativeVideoRecyclerAdapter.this);
            }
        });

        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Logging.out(LOG_TAG, "onLayoutChange!!!!!");
                mNativeVideoController.onScroll(mRecyclerView, NativeVideoRecyclerAdapter.this);
            }
        });

        mOriginAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                triggerUpdateProcessor();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                triggerUpdateProcessor();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                triggerUpdateProcessor();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                triggerUpdateProcessor();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                triggerUpdateProcessor();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                triggerUpdateProcessor();
            }
        });
    }

    private void triggerUpdateProcessor() {
        notifyDataSetChanged();
        mNativeVideoController.refreshAdPlacement(mOriginAdapter.getItemCount());
    }

    /**
     * Clean resources.
     */
    public void destroy() {
        mNativeVideoController.destroy();
    }

    /**
     * Pauses ads (video).
     * NOTE: trigger in Activity onPause().
     */
    public void onPause() {
        Logging.out(LOG_TAG, "onPause");
        mNativeVideoController.onPause();
    }

    /**
     * Resumes ads (video).
     * NOTE: trigger in Activity onResume().
     */
    public void onResume() {
        Logging.out(LOG_TAG, "onResume");
        mNativeVideoController.onResume(mRecyclerView, this);
    }

    public void setMinimizedMode(MinimizedMode mode) {
        if (mode != null) {
            Logging.out(LOG_TAG, "Set minimized mode");
            mNativeVideoController.setMinimizedMode(mode);
        }
    }

    /**
     * Adds banner ad to defined position.
     * @param appKey - app key
     * @param position - position in list
     */
    public void putAdWithAppKeyToPosition(String appKey, int position) {
        if (position < mOriginAdapter.getItemCount()) {
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
            mNativeVideoController.loadAds(mOriginAdapter.getItemCount(), this);
        } else {
            LoopMeError error = new LoopMeError("No connection");
            mNativeVideoController.onLoadFail(error);
        }
    }

    /**
     * Define custome design for TileText ads
     * @param binder - ViewBinder
     */
    public void setViewBinder(NativeVideoBinder binder) {
        if (mNativeVideoController != null) {
            mNativeVideoController.setViewBinder(binder);
        }
    }

    /**
     * Set listener to receive notifications during loading/showing process
     * @param listener - listener
     */
    public void setListener(LoopMeBanner.Listener listener) {
        mNativeVideoController.setListener(listener);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_AD) {
            RelativeLayout v = new RelativeLayout(viewGroup.getContext());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            v.setLayoutParams(params);
            return new NativeVideoVH(v);
        } else {
            return mOriginAdapter.onCreateViewHolder(viewGroup, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (isAd(position)) {

            Logging.out(LOG_TAG, "onBindViewHolder");
            final LoopMeBanner banner = mNativeVideoController.getNativeVideoAd(position);
            View row = mNativeVideoController.getAdView(mInflater, null, banner, position);

            ((NativeVideoVH) viewHolder).adView.removeAllViews();
            if (row.getParent() != null) {
                ((ViewGroup) row.getParent()).removeView(row);
            }
            ((NativeVideoVH) viewHolder).adView.addView(row);

            RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) row.getLayoutParams();
            p.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            row.setLayoutParams(p);

        } else {
            int initPosition = mNativeVideoController.getInitialPosition(position);
            mOriginAdapter.onBindViewHolder(viewHolder, initPosition);
        }

    }

    @Override
    public boolean isAd(int i) {
        return mNativeVideoController.getNativeVideoAd(i) != null ? true : false;
    }

    @Override
    public int getItemViewType(int position) {
        int initPosition = mNativeVideoController.getInitialPosition(position);
        return isAd(position) ? TYPE_AD : mOriginAdapter.getItemViewType(initPosition);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        mOriginAdapter.setHasStableIds(hasStableIds);
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
    public int getItemCount() {
        return mOriginAdapter.getItemCount() + mNativeVideoController.getAdsCount();
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        mOriginAdapter.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
        return mOriginAdapter.onFailedToRecycleView(holder);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        mOriginAdapter.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        mOriginAdapter.onViewDetachedFromWindow(holder);
    }

    @Override
    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        mOriginAdapter.registerAdapterDataObserver(observer);
    }

    @Override
    public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        mOriginAdapter.unregisterAdapterDataObserver(observer);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mOriginAdapter.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mOriginAdapter.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onDataSetChanged() {
        mOriginAdapter.notifyDataSetChanged();
    }

    public class NativeVideoVH extends RecyclerView.ViewHolder {
        private RelativeLayout adView;

        public NativeVideoVH(View view) {
            super(view);
            adView = (RelativeLayout) view;
        }
    }
}
