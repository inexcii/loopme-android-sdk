package com.loopme.common;

import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

public class MinimizedMode {

    private static final String LOG_TAG = MinimizedMode.class.getSimpleName();

    private int mWidth = 100;
    private int mHeight = 100;
    private int mMarginRight = 10;
    private int mMarginBottom = 10;
    private ViewGroup mRoot;
    private RecyclerView mRecyclerView;
    private int mPosition;

    public MinimizedMode(ViewGroup root, RecyclerView recyclerView) {
        if (root == null) {
            Logging.out(LOG_TAG, "Error: Root view or recyclerView should be not null. Minimized mode will not work");
            return;
        }
        mRoot = root;
        mRecyclerView = recyclerView;

        DisplayMetrics dm = Utils.getDisplayMetrics();
        // portrait mode
        if (dm.heightPixels > dm.widthPixels) {
            mWidth = dm.widthPixels / 2;
        } else { //landscape mode
            mWidth = dm.widthPixels / 3;
        }
        mHeight = mWidth * 2 / 3;
        mWidth = mWidth - 6;
    }

    public void setViewSize(int width, int height) {
        mWidth = Utils.convertDpToPixel(width);
        mHeight = Utils.convertDpToPixel(height);
    }

    public void setMarginRight(int margin) {
        mMarginRight = Utils.convertDpToPixel(margin);
    }

    public void setMarginBottom(int margin) {
        mMarginBottom = Utils.convertDpToPixel(margin);
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public ViewGroup getRootView() {
        return mRoot;
    }

    public int getMarginRight() {
        return mMarginRight;
    }

    public int getMarginBottom() {
        return mMarginBottom;
    }

    public void onViewClicked() {
        if (mRecyclerView != null) {
            mRecyclerView.smoothScrollToPosition(mPosition);
        }
    }

    public void setPosition(int position) {
        mPosition = position;
    }
}
