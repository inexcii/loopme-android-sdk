package com.loopme;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.loopme.adview.AdView;
import com.loopme.common.Logging;
import com.loopme.common.Utils;
import com.loopme.constants.StretchOption;


public class ViewController implements TextureView.SurfaceTextureListener, IViewController {

    private static final String LOG_TAG = ViewController.class.getSimpleName();

    private TextureView mTextureView;

    private int mVideoWidth;
    private int mVideoHeight;
    private int mResizeWidth;
    private int mResizeHeight;

    private Callback mCallback;
    private StretchOption mStretch = StretchOption.NONE;

    public interface Callback {
        void onSurfaceTextureAvailable(SurfaceTexture surface);
        void onSurfaceTextureDestroyed();
    }

    public ViewController(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void buildVideoAdView(Context context, ViewGroup bannerView, AdView adView) {
        mTextureView = new TextureView(context);
        if (Build.VERSION.SDK_INT < 23) {
            mTextureView.setBackgroundColor(Color.TRANSPARENT);
        }
        mTextureView.setSurfaceTextureListener(this);

        adView.setBackgroundColor(Color.TRANSPARENT);
        adView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        bannerView.removeAllViews();
        bannerView.setBackgroundColor(Color.BLACK);
        bannerView.addView(mTextureView, 0);
        if (adView.getParent() != null) {
            ((ViewGroup) adView.getParent()).removeView(adView);
        }
        bannerView.addView(adView, 1);
    }

    @Override
    public void rebuildView(ViewGroup bannerView, AdView adView) {
        Logging.out(LOG_TAG, "rebuildView");
        if (bannerView == null || adView == null || mTextureView == null) {
            return;
        }
        bannerView.setBackgroundColor(Color.BLACK);
        if (mTextureView.getParent() != null) {
            ((ViewGroup) mTextureView.getParent()).removeView(mTextureView);
        }
        if (adView.getParent() != null) {
            ((ViewGroup) adView.getParent()).removeView(adView);
        }

        bannerView.addView(mTextureView, 0);
        bannerView.addView(adView, 1);
    }

    private void resizeVideo() {
        Logging.out(LOG_TAG, "resizeVideo()");

        if (mTextureView == null || mResizeWidth == 0 || mResizeHeight == 0
                || mVideoWidth == 0 || mVideoHeight == 0) {
            return;
        }

        FrameLayout.LayoutParams oldParams = (FrameLayout.LayoutParams) mTextureView.getLayoutParams();
        FrameLayout.LayoutParams params = Utils.calculateNewLayoutParams(oldParams,
                mVideoWidth, mVideoHeight,
                mResizeWidth, mResizeHeight, mStretch);
        mTextureView.setLayoutParams(params);
    }

    @Override
    public void setViewSize(int width, int height) {
        Logging.out(LOG_TAG, "setViewSize " + width + " : " + height);
        mResizeWidth = width;
        mResizeHeight = height;
    }

    @Override
    public void setVideoSize(int width, int height) {
        Logging.out(LOG_TAG, "setVideoSize " + width + " : " + height);
        mVideoWidth = width;
        mVideoHeight = height;
    }

    @Override
    public void setStretchParam(StretchOption stretchParam) {
        Logging.out(LOG_TAG, "setStretchParam");
        mStretch = stretchParam;
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public boolean handleTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public void initVRLibrary(Context context) {

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mCallback != null) {
            mCallback.onSurfaceTextureAvailable(surface);
        }
        resizeVideo();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mCallback != null) {
            mCallback.onSurfaceTextureDestroyed();
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
