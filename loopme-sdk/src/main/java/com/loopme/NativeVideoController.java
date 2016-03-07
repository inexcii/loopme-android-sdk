package com.loopme;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.loopme.common.AdChecker;
import com.loopme.common.MinimizedMode;
import com.loopme.constants.DisplayMode;
import com.loopme.common.Logging;
import com.loopme.common.LoopMeError;
import com.loopme.constants.WebviewState;
import com.loopme.debugging.ErrorTracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class NativeVideoController {

    private static final String LOG_TAG = NativeVideoController.class.getSimpleName();

    private NativeVideoBinder mBinder;
    private LoopMeBanner.Listener mAdListener;

    private SparseArray<View> mViewMap = new SparseArray<>();
    private SparseArray<LoopMeBanner> mAdsMap = new SparseArray<>();

    private SparseArray<String> mAppKeysMap = new SparseArray<>();

    private boolean mHorizontalScrolling;

    private DataChangeListener mDataChangeListener;

    private int mItemCount;

    private Context mContext;
    private MinimizedMode mMinimizedMode;

    public interface DataChangeListener {
        void onDataSetChanged();
    }

    public NativeVideoController(@NonNull Context context) {
        mContext = context;
    }

    void refreshAdPlacement(int itemCount) {
        if (itemCount >= mItemCount) {
            return;
        }
        if (itemCount == 0) {
            int position = mAdsMap.keyAt(0);
            LoopMeBanner banner = mAdsMap.get(position);
            banner.destroy();
            mAdsMap.clear();
            return;
        }
        SparseArray<LoopMeBanner> currentAdsMap = mAdsMap.clone();
        mAdsMap.clear();
        for (int i = 0; i < currentAdsMap.size(); i++) {
            int key = currentAdsMap.keyAt(i);
            LoopMeBanner banner = currentAdsMap.get(key);
            if (key <= itemCount) {
                mAdsMap.put(key, banner);
            } else {
                banner.destroy();
            }
        }
    }

    void destroy() {
        Logging.out(LOG_TAG, "destroy");
        for (int i = 0; i < mAppKeysMap.size(); i++) {
            LoopMeBanner banner = LoopMeBanner.getInstance(mAppKeysMap.valueAt(i), null);
            banner.destroy();
        }
        mViewMap.clear();
        mAdsMap.clear();
        mAppKeysMap.clear();
        mAdListener = null;
        mDataChangeListener = null;
    }

    void onPause() {
        if (mAdsMap.size() != 0) {
            for (int i = 0; i < mAdsMap.size(); i++) {
                LoopMeBanner banner = mAdsMap.valueAt(i);
                if (banner != null) {
                    banner.pause();
                }
            }
        }
    }

    void onResume(AbsListView listView, AdChecker checker) {
        if (mAdsMap.size() != 0) {
            if (mAdsMap.size() == 1) {
                LoopMeBanner banner = mAdsMap.valueAt(0);
                if (banner.getViewController().getCurrentDisplayMode() == DisplayMode.MINIMIZED) {
                    banner.getViewController().setWebViewState(WebviewState.VISIBLE);
                    return;
                }
            }
            onScroll(listView, checker);
        }
    }

    void onResume(RecyclerView recyclerView, AdChecker checker) {
        if (mAdsMap.size() != 0) {
            if (mAdsMap.size() == 1) {
                LoopMeBanner banner = mAdsMap.valueAt(0);
                if (banner.getViewController().getCurrentDisplayMode() == DisplayMode.MINIMIZED) {
                    banner.getViewController().setWebViewState(WebviewState.VISIBLE);
                    return;
                }
            }
            onScroll(recyclerView, checker);
        }
    }

    void setViewBinder(NativeVideoBinder binder) {
        mBinder = binder;
    }

    void setListener(LoopMeBanner.Listener listener) {
        mAdListener = listener;
    }

    void onLoadFail(LoopMeError error) {
        if (mAdListener != null) {
            mAdListener.onLoopMeBannerLoadFail(null, error);
        }
    }

    void putAdWithAppKeyToPosition(String appKey, int position) {
        mAppKeysMap.put(position, appKey);
    }

    LoopMeBanner.Listener getListener() {
        return mAdListener;
    }

    LoopMeBanner getNativeVideoAd(int position) {
        return mAdsMap.get(position);
    }

    int getAdsCount() {
        return mAdsMap.size();
    }

    View getAdView(LayoutInflater inflater, ViewGroup parent, final LoopMeBanner banner,
                   int position) {
        Logging.out(LOG_TAG, "getAdView");
        checkFiftyPersentVisibility(mViewMap.get(position), banner);

        if (mViewMap.indexOfKey(position) >= 0) {
            return mViewMap.get(position);
        }
        View row;

        if (mBinder == null) {
            Logging.out(LOG_TAG, "Error: NativeVideoBinder is null. Init and bind it");
            row = null;

        } else {
            row = inflater.inflate(mBinder.getLayout(), parent, false);
            bindDataToView(row, mBinder, position);
        }

        mViewMap.put(position, row);
        return row;
    }

    private void bindDataToView(View row, NativeVideoBinder binder, final int position) {
        Logging.out(LOG_TAG, "bindDataToView");
        final LoopMeBannerView video = (LoopMeBannerView) row.findViewById(binder.getBannerViewId());
        int index = mAdsMap.indexOfKey(position);
        final LoopMeBanner banner = mAdsMap.valueAt(index);
        banner.bindView(video);
        banner.showNativeVideo();
    }

    void loadAds(final int itemsCount, DataChangeListener listener) {
        mDataChangeListener = listener;
        mItemCount = itemsCount;

        if (mAppKeysMap.size() == 0) {
            ErrorTracker.post("No ads for loading");
        }

        LoopMeBanner.Listener bannerListener = initBannerListener();
        for (int i = 0; i < mAppKeysMap.size(); i++) {
            String appKey = mAppKeysMap.valueAt(i);
            LoopMeBanner banner = LoopMeBanner.getInstance(appKey, mContext);
            banner.setListener(bannerListener);
            banner.load();
        }
    }

    private LoopMeBanner.Listener initBannerListener() {
        return new LoopMeBanner.Listener() {
            @Override
            public void onLoopMeBannerLoadSuccess(LoopMeBanner banner) {
                banner.setMinimizedMode(mMinimizedMode);
                addItem(banner, mItemCount);
                if (mAdListener != null) {
                    mAdListener.onLoopMeBannerLoadSuccess(banner);
                }
            }

            @Override
            public void onLoopMeBannerLoadFail(LoopMeBanner banner, LoopMeError error) {

            }

            @Override
            public void onLoopMeBannerShow(LoopMeBanner banner) {

            }

            @Override
            public void onLoopMeBannerHide(LoopMeBanner banner) {

            }

            @Override
            public void onLoopMeBannerClicked(LoopMeBanner banner) {

            }

            @Override
            public void onLoopMeBannerLeaveApp(LoopMeBanner banner) {

            }

            @Override
            public void onLoopMeBannerVideoDidReachEnd(LoopMeBanner banner) {

            }

            @Override
            public void onLoopMeBannerExpired(LoopMeBanner banner) {

            }
        };
    }

    private void addItem(LoopMeBanner ad, int itemsCount) {
        int indexOfValue = mAppKeysMap.indexOfValue(ad.getAppKey());
        int nextIndex = mAppKeysMap.keyAt(indexOfValue);
        if (nextIndex < itemsCount + getAdsCount()) {
            mAdsMap.put(nextIndex, ad);
            Logging.out(LOG_TAG, "add ad to position " + nextIndex);
            if (mDataChangeListener != null) {
                mDataChangeListener.onDataSetChanged();
            }
        }
    }

    int getInitialPosition(int position) {
        int adsBefore = 0;
        for (int i = 0; i < mAdsMap.size(); i++) {
            if (mAdsMap.keyAt(i) <= position) {
                adsBefore++;
            }
        }
        return position - adsBefore;
    }

    void onScroll(AbsListView listview, AdChecker detector) {
        if (mAdsMap.size() == 0) {
            return;
        }
        int first = listview.getFirstVisiblePosition();
        int last = listview.getLastVisiblePosition();

        if (last == -1) {
            return;
        }

        for (int i = 0; i < mAdsMap.size(); i++) {
            int adIndex = mAdsMap.keyAt(i);

            LoopMeBanner banner = mAdsMap.get(adIndex);
            if (banner.getViewController().getCurrentDisplayMode() == DisplayMode.FULLSCREEN) {
                return;
            }

            if (detector.isAd(adIndex)) {
                if (adIndex < first || adIndex > last) {
                    Logging.out(LOG_TAG, "scroll out of viewport");
                    if (mAdsMap.size() == 1) {
                        banner.switchToMinimizedMode();
                    } else {
                        banner.pause();
                    }

                } else {
                    Logging.out(LOG_TAG, "scroll in viewport");
                    int childIndex = adIndex - first;
                    View view = listview.getChildAt(childIndex);
                    banner.switchToNormalMode();
                    checkFiftyPersentVisibility(view, banner);
                }
            }
        }
    }

    void onScroll(RecyclerView recyclerView, AdChecker detector) {
        if (detector == null || recyclerView == null ||
                mAdsMap.size() == 0) {
            return;
        }

        Logging.out(LOG_TAG, "onScroll");

        int first = 0;
        int last = 0;

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        if (layoutManager instanceof LinearLayoutManager) {
            first = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
            last = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();

        } else if (layoutManager instanceof GridLayoutManager) {
            first = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
            last = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();

        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] firsts;
            int[] lasts;
            try {
                firsts = ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(null);
                lasts = ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(null);
            } catch (NullPointerException e) {
                return;
            }

            int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
            if (orientation == OrientationHelper.HORIZONTAL) {
                mHorizontalScrolling = true;
            }

            List<Integer> firstList = new ArrayList<Integer>(firsts.length);
            for (int i = 0; i < firsts.length; i++) {
                firstList.add(firsts[i]);
            }

            List<Integer> lastList = new ArrayList<Integer>(lasts.length);
            for (int i = 0; i < lasts.length; i++) {
                lastList.add(lasts[i]);
            }

            first = Collections.min(firstList);
            last = Collections.max(lastList);
        }

        for (int i = 0; i < mAdsMap.size(); i++) {
            int adIndex = mAdsMap.keyAt(i);

            LoopMeBanner banner = mAdsMap.get(adIndex);
            if (banner.getViewController().getCurrentDisplayMode() == DisplayMode.FULLSCREEN) {
                return;
            }

            if (detector.isAd(adIndex)) {
                if (adIndex < first || adIndex > last) {
                    Logging.out(LOG_TAG, "scroll out of viewport");
                    if (mAdsMap.size() == 1) {
                        banner.switchToMinimizedMode();
                    } else {
                        banner.pause();
                    }

                } else {
                    Logging.out(LOG_TAG, "scroll in viewport");
                    int childIndex = adIndex - first;
                    View view = recyclerView.getLayoutManager().getChildAt(childIndex);
                    banner.switchToNormalMode();
                    checkFiftyPersentVisibility(view, banner);
                }
            }
        }
    }

    void checkFiftyPersentVisibility(View view, final LoopMeBanner banner) {

        if (banner == null || view == null) {
            return;
        }

        Rect rect = new Rect();
        boolean b = view.getGlobalVisibleRect(rect);

        int halfOfView = mHorizontalScrolling ? view.getWidth() / 2 : view.getHeight() / 2;
        int rectHeight = mHorizontalScrolling ? rect.width() : rect.height();

        if (b) {

            if (rectHeight < halfOfView) {
                Logging.out(LOG_TAG, "invisible");
                banner.pause();

            } else {
                Logging.out(LOG_TAG, "visible");
                banner.getViewController().setWebViewState(WebviewState.VISIBLE);
            }
        }
    }

    void setMinimizedMode(MinimizedMode mode) {
        mMinimizedMode = mode;
    }
}
