package com.loopme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.loopme.adview.AdView;
import com.loopme.adview.Bridge;
import com.loopme.common.MinimizedMode;
import com.loopme.common.VideoLoader;
import com.loopme.constants.AdFormat;
import com.loopme.constants.DisplayMode;
import com.loopme.common.Logging;
import com.loopme.common.LoopMeError;
import com.loopme.common.StaticParams;
import com.loopme.common.SwipeListener;
import com.loopme.common.Utils;
import com.loopme.constants.StretchOption;
import com.loopme.constants.VideoState;
import com.loopme.constants.WebviewState;

class ViewController implements TextureView.SurfaceTextureListener {

    private static final String LOG_TAG = ViewController.class.getSimpleName();

    private static final String EXTRA_URL = "url";

    private AdView mAdView;
    private volatile Bridge.Listener mBridgeListener;

    private boolean mIsVideoPresented;

    private BaseAd mAd;

    private TextureView mTextureView;

    private int mDisplayMode = DisplayMode.NORMAL;
    private int mPrevDisplayMode = DisplayMode.NORMAL;

    private MinimizedMode mMinimizedMode;
    private LoopMeBannerView mMinimizedView;

    private boolean mHorizontalScrollOrientation;

    //we should ignore first command
    private boolean mIsFirstFullScreenCommand = true;

    private boolean mIsBackFromExpand;

    private VideoLoader mVideoLoader;
    private StretchOption mStretch = StretchOption.NONE;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mResizeWidth;
    private int mResizeHeight;

    private boolean mPostponePlay;
    private int mPostponePlayPosition;

    private String mVideoUrl;
    private String mFileRest;

    private VideoController mVideoController;

    public ViewController(BaseAd ad) {
        mAd = ad;
        mAdView = new AdView(mAd.getContext());
        mBridgeListener = initBridgeListener();
        mAdView.addBridgeListener(mBridgeListener);
        mAdView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });

        VideoController.Callback callback = initVideoControllerCallback();
        mVideoController = new VideoController(mAdView, callback);
    }

    private VideoController.Callback initVideoControllerCallback() {
        return new VideoController.Callback() {
            @Override
            public void onVideoReachEnd() {
                mAd.onAdVideoDidReachEnd();
            }

            @Override
            public void onFail(LoopMeError error) {
                mAd.onAdLoadFail(error);
            }

            @Override
            public void onVideoSizeChanged(int width, int height) {
                mVideoWidth = width;
                mVideoHeight = height;
            }

            @Override
            public void postponePlay(int position) {
                mPostponePlay = true;
                mPostponePlayPosition = position;
            }

            @Override
            public void playbackFinishedWithError() {
                if (mAd.getAdFormat() == AdFormat.BANNER) {
                    ((LoopMeBanner) mAd).playbackFinishedWithError();
                }
            }
        };
    }

    void resetFullScreenCommandCounter() {
        mIsFirstFullScreenCommand = true;
    }

    void destroy() {
        mBridgeListener = null;
        mVideoController.destroy();
        if (mVideoLoader != null) {
            mVideoLoader.stop();
        }

        if (mAdView != null) {
            mAdView.stopLoading();
            mAdView.clearCache(true);
            mAdView = null;
            Logging.out(LOG_TAG, "AdView destroyed");
        }
        mMinimizedMode = null;
    }

    public void setWebViewState(int state) {
        if (mAdView != null) {
            mAdView.setWebViewState(state);
        }
    }

    void onAdShake() {
        if (mAdView != null) {
            mAdView.shake();
        }
    }

    int getCurrentVideoState() {
        if (mAdView != null) {
            return mAdView.getCurrentVideoState();
        }
        return -1;
    }

    public int getCurrentDisplayMode() {
        return mDisplayMode;
    }

    void buildStaticAdView(ViewGroup bannerView) {
        if (bannerView == null || mAdView == null) {
            return;
        }
        mAdView.setBackgroundColor(Color.BLACK);
        bannerView.addView(mAdView);
    }

    void buildVideoAdView(ViewGroup bannerView) {
        mTextureView = new TextureView(mAd.getContext());
        mTextureView.setBackgroundColor(Color.TRANSPARENT);
        mTextureView.setSurfaceTextureListener(this);

        mAdView.setBackgroundColor(Color.TRANSPARENT);
        mAdView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        bannerView.setBackgroundColor(Color.BLACK);
        bannerView.addView(mTextureView, 0);
        if (mAdView.getParent() != null) {
            ((ViewGroup) mAdView.getParent()).removeView(mAdView);
        }
        bannerView.addView(mAdView, 1);
    }

    void rebuildView(ViewGroup bannerView) {
        if (bannerView == null || mAdView == null || mTextureView == null) {
            return;
        }
        bannerView.setBackgroundColor(Color.BLACK);
        if (mTextureView.getParent() != null) {
            ((ViewGroup) mTextureView.getParent()).removeView(mTextureView);
        }
        if (mAdView.getParent() != null) {
            ((ViewGroup) mAdView.getParent()).removeView(mAdView);
        }

        bannerView.addView(mTextureView, 0);
        bannerView.addView(mAdView, 1);
    }

    void ensureAdIsVisible(View view) {
        if (mAdView == null || view == null) {
            return;
        }

        Rect rect = new Rect();
        boolean b = view.getGlobalVisibleRect(rect);

        int halfOfView = mHorizontalScrollOrientation ? view.getWidth() / 2 : view.getHeight() / 2;
        int rectHeight = mHorizontalScrollOrientation ? rect.width() : rect.height();

        if (b) {
            if (rectHeight < halfOfView) {
                setWebViewState(WebviewState.HIDDEN);
                mIsBackFromExpand = false;
            } else if (rectHeight >= halfOfView) {
                setWebViewState(WebviewState.VISIBLE);
            }
        } else {
            setWebViewState(WebviewState.HIDDEN);
        }
    }

    private void storePreviousMode(int displayMode) {
        if (displayMode == DisplayMode.FULLSCREEN) {
            mPrevDisplayMode = DisplayMode.FULLSCREEN;

        } else if (displayMode == DisplayMode.MINIMIZED) {
            mPrevDisplayMode = DisplayMode.MINIMIZED;

        } else {
            mPrevDisplayMode = DisplayMode.NORMAL;
        }
    }

    boolean isBackFromExpand() {
        return mIsBackFromExpand;
    }

    void switchToMinimizedMode() {
        if (mDisplayMode == DisplayMode.MINIMIZED) {
            if (getCurrentVideoState() == VideoState.PAUSED) {
                setWebViewState(WebviewState.VISIBLE);
            }
            return;
        }
        Logging.out(LOG_TAG, "switch to minimized mode");
        storePreviousMode(mDisplayMode);

        mDisplayMode = DisplayMode.MINIMIZED;

        int width = mMinimizedMode.getWidth();
        int height = mMinimizedMode.getHeight();
        mMinimizedView = new LoopMeBannerView(mAdView.getContext(), width, height);

        rebuildView(mMinimizedView);
        addBordersToView(mMinimizedView);

        if (mAdView.getCurrentWebViewState() == WebviewState.HIDDEN) {
            mMinimizedView.setAlpha(0);
        }

        mMinimizedMode.getRootView().addView(mMinimizedView);
        configMinimizedViewLayoutParams(mMinimizedView);

        setWebViewState(WebviewState.VISIBLE);

        mAdView.setOnTouchListener(new SwipeListener(width,
            new SwipeListener.Listener() {
                @Override
                public void onSwipe(boolean toRight) {
                    mAdView.setWebViewState(WebviewState.HIDDEN);

                    Animation anim = AnimationUtils.makeOutAnimation(mAd.getContext(),
                        toRight);
                    anim.setDuration(200);
                    mMinimizedView.startAnimation(anim);

                    switchToNormalMode();
                    mMinimizedMode = null;
                }
            }));
    }

    private void configMinimizedViewLayoutParams(LoopMeBannerView bannerView) {
        LayoutParams lp = (LayoutParams) bannerView.getLayoutParams();
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.bottomMargin = mMinimizedMode.getMarginBottom();
        lp.rightMargin = mMinimizedMode.getMarginRight();
        bannerView.setLayoutParams(lp);
    }

    @SuppressLint("NewApi")
    private void addBordersToView(LoopMeBannerView bannerView) {
        ShapeDrawable drawable = new ShapeDrawable(new RectShape());
        drawable.getPaint().setColor(Color.BLACK);
        drawable.getPaint().setStyle(Style.FILL_AND_STROKE);
        drawable.getPaint().setAntiAlias(true);

        bannerView.setPadding(2, 2, 2, 2);
        if (Build.VERSION.SDK_INT < 16) {
            bannerView.setBackgroundDrawable(drawable);
        } else {
            bannerView.setBackground(drawable);
        }
    }

    void switchToNormalMode() {
        if (mDisplayMode == DisplayMode.NORMAL) {
            return;
        }

        Logging.out(LOG_TAG, "switch to normal mode");
        if (mDisplayMode == DisplayMode.FULLSCREEN) {
            mIsBackFromExpand = true;
        }
        storePreviousMode(mDisplayMode);
        mDisplayMode = DisplayMode.NORMAL;

        LoopMeBannerView initialView = ((LoopMeBanner) mAd).getBannerView();
        initialView.setVisibility(View.VISIBLE);

        rebuildView(initialView);

        if (mMinimizedView != null && mMinimizedView.getParent() != null) {
            ((ViewGroup) mMinimizedView.getParent()).removeView(mMinimizedView);
            mMinimizedView.removeAllViews();
        }

        if (mAdView != null) {
            mAdView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return (event.getAction() == MotionEvent.ACTION_MOVE);
                }
            });
        }
    }

    public void setMinimizedMode(MinimizedMode mode) {
        mMinimizedMode = mode;
    }

    boolean isMinimizedModeEnable() {
        return mMinimizedMode != null && mMinimizedMode.getRootView() != null;
    }

    void destroyMinimizedView() {
        if (mMinimizedView != null) {
            if (mMinimizedView.getParent() != null) {
                ((ViewGroup) mMinimizedView.getParent()).removeView(mMinimizedView);
            }
            mMinimizedView.removeAllViews();
            mMinimizedView = null;
        }
    }

    void preloadHtml(String html) {
        if (mAdView != null) {
            Logging.out(LOG_TAG, "loadDataWithBaseURL");
            mAdView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        } else {
            mAd.onAdLoadFail(new LoopMeError("Html loading error"));
        }
    }

    public boolean isVideoPresented() {
        return mIsVideoPresented;
    }

    private Bridge.Listener initBridgeListener() {
        return new Bridge.Listener() {

            @Override
            public void onJsVideoPlay(int time) {
                handleVideoPlay(time);
            }

            @Override
            public void onJsVideoPause(final int time) {
                handleVideoPause(time);
            }

            @Override
            public void onJsVideoMute(boolean mute) {
                handleVideoMute(mute);
            }

            @Override
            public void onJsVideoLoad(final String videoUrl) {
                handleVideoLoad(videoUrl);
            }

            @Override
            public void onJsLoadSuccess() {
                handleLoadSuccess();
            }

            @Override
            public void onJsClose() {
                handleClose();
            }

            @Override
            public void onJsLoadFail(String mess) {
                handleLoadFail(mess);
            }

            @Override
            public void onJsFullscreenMode(boolean b) {
                handleFullscreenMode(b);
            }

            @Override
            public void onNonLoopMe(String url) {
                handleNonLoopMe(url);
            }

            @Override
            public void onJsVideoStretch(boolean b) {
                handleVideoStretch(b);
            }
        };
    }

    private void handleLoadSuccess() {
        Logging.out(LOG_TAG, "JS command: load success");
        mAd.startExpirationTimer();
        mAd.onAdLoadSuccess();
    }

    private void handleLoadFail(String mess) {
        Logging.out(LOG_TAG, "JS command: load fail");
        if (mAd != null) {
            mAd.onAdLoadFail(new LoopMeError("Failed to process ad"));
        }
    }

    private void handleVideoLoad(String videoUrl) {
        Logging.out(LOG_TAG, "JS command: load video " + videoUrl);
        mVideoUrl = videoUrl;

        mIsVideoPresented = true;
        boolean preload = mAd.getAdParams().getPartPreload();
        loadVideoFile(videoUrl, mAd.getContext(), preload);
    }

    private void loadVideoFile(final String videoUrl, Context context, final boolean preload) {

        mVideoLoader = new VideoLoader(videoUrl, preload, context, new VideoLoader.Callback() {

            @Override
            public void onError(LoopMeError error) {
                sendLoadFail(error);
            }

            @Override
            public void onPreviewLoaded(String filePath) {
                Logging.out(LOG_TAG, "onPreviewLoaded");
                mVideoController.initPlayerFromFile(filePath);
            }

            @Override
            public void onFullVideoLoaded(String filePath) {
                Log.d("debug2", "onFullVideoLoaded: " + filePath);
                if (preload) {
                    if (mAd.isShowing()) {
                        mFileRest = filePath;
                        mVideoController.setFileRest(mFileRest);
                        mVideoController.waitForVideo();
                    } else {
                        mVideoController.releasePlayer();
                        mVideoController.initPlayerFromFile(filePath);
                    }
                } else {
                    mVideoController.initPlayerFromFile(filePath);
                }
            }
        });
        mVideoLoader.start();
    }

    private void sendLoadFail(LoopMeError error) {
        if (mAd != null) {
            mAd.onAdLoadFail(error);
        }
    }

    private void handleVideoMute(boolean mute) {
        Logging.out(LOG_TAG, "JS command: video mute " + mute);

        mAdView.setVideoMute(mute);
        mVideoController.muteVideo(mute);
    }

    private void handleVideoPlay(final int time) {
        Logging.out(LOG_TAG, "JS command: play video " + time);

        mVideoController.playVideo(time);

        if (mDisplayMode == DisplayMode.MINIMIZED) {
            Utils.animateAppear(mMinimizedView);
        }
    }

    private void handleVideoPause(int time) {
        Logging.out(LOG_TAG, "JS command: pause video " + time);
        mVideoController.pauseVideo();
    }

    private void handleClose() {
        Logging.out(LOG_TAG, "JS command: close");
        mAd.dismiss();
    }

    private void handleVideoStretch(boolean b) {
        Logging.out(LOG_TAG, "JS command: stretch video ");
        mStretch = b ? StretchOption.STRECH : StretchOption.NO_STRETCH;
    }

    private void handleFullscreenMode(boolean b) {
        if (mIsFirstFullScreenCommand) {
            mIsFirstFullScreenCommand = false;
            mAdView.setFullscreenMode(false);
            return;
        }
        if (b) {
            switchToFullScreenMode();
        } else {
            broadcastDestroyIntent();
        }
        mAdView.setFullscreenMode(b);
    }

    private void broadcastDestroyIntent() {
        Intent intent = new Intent();
        intent.setAction(StaticParams.DESTROY_INTENT);
        mAd.getContext().sendBroadcast(intent);
    }

    private void switchToFullScreenMode() {
        if (mDisplayMode != DisplayMode.FULLSCREEN) {
            Logging.out(LOG_TAG, "switch to fullscreen mode");
            storePreviousMode(mDisplayMode);
            mDisplayMode = DisplayMode.FULLSCREEN;

            if (mPrevDisplayMode == DisplayMode.MINIMIZED) {
                if (mMinimizedView != null && mMinimizedView.getParent() != null) {
                    ((ViewGroup) mMinimizedView.getParent()).removeView(mMinimizedView);
                }
            }
            startAdActivity();
        }
    }

    void switchToPreviousMode() {
        if (mPrevDisplayMode == DisplayMode.MINIMIZED) {
            switchToMinimizedMode();
        } else if (mPrevDisplayMode == DisplayMode.NORMAL) {
            switchToNormalMode();
        }
    }

    private void startAdActivity() {
        LoopMeAdHolder.putAd(mAd);

        Context context = mAd.getContext();
        Intent intent = new Intent(context, AdActivity.class);
        intent.putExtra(StaticParams.APPKEY_TAG, mAd.getAppKey());
        intent.putExtra(StaticParams.FORMAT_TAG, mAd.getAdFormat());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void handleNonLoopMe(String url) {
        Logging.out(LOG_TAG, "Non Js command");
        Context context = mAd.getContext();
        if (Utils.isOnline(context)) {
            Intent intent = new Intent(context, AdBrowserActivity.class);
            intent.putExtra(EXTRA_URL, url);
            intent.putExtra(StaticParams.APPKEY_TAG, mAd.getAppKey());
            intent.putExtra(StaticParams.FORMAT_TAG, mAd.getAdFormat());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            mAd.onAdClicked();
            setWebViewState(WebviewState.HIDDEN);
            broadcastAdClickedIntent();

            context.startActivity(intent);
        } else {
            Logging.out(LOG_TAG, "No internet connection");
        }
    }

    private void broadcastAdClickedIntent() {
        Intent intent = new Intent();
        intent.setAction(StaticParams.CLICK_INTENT);
        mAd.getContext().sendBroadcast(intent);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Logging.out(LOG_TAG, "onSurfaceTextureAvailable");

        mVideoController.setSurfaceTextureAvailable(true);
        if (mPostponePlay) {
            mVideoController.playVideo(mPostponePlayPosition);
            mPostponePlay = false;
        }

        int viewWidth = 0;
        int viewHeight = 0;

        switch (mDisplayMode) {
            case DisplayMode.MINIMIZED:
                if (mMinimizedMode != null) {
                    viewWidth = mMinimizedMode.getWidth();
                    viewHeight = mMinimizedMode.getHeight();
                } else {
                    Logging.out(LOG_TAG, "WARNING: MinimizedMode is null");
                }
                break;

            case DisplayMode.NORMAL:
                viewWidth = mAd.detectWidth();
                viewHeight = mAd.detectHeight();
                break;

            case DisplayMode.FULLSCREEN:
                viewWidth = Utils.getScreenWidth();
                viewHeight = Utils.getScreenHeight();
                break;

            default:
                Logging.out(LOG_TAG, "Unknown display mode");
                break;
        }

        Surface s = new Surface(surface);
        mVideoController.setSurface(s);
        resizeVideo(mTextureView, viewWidth, viewHeight);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
                                            int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Logging.out(LOG_TAG, "onSurfaceTextureDestroyed");
        mVideoController.setSurfaceTextureAvailable(false);
        mVideoController.setSurface(null);
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private void resizeVideo(final TextureView texture, int viewWidth, int viewHeight) {
        mTextureView = texture;
        mResizeWidth = viewWidth;
        mResizeHeight = viewHeight;

        updateLayoutParams();
    }

    private void updateLayoutParams() {
        Logging.out(LOG_TAG, "updateLayoutParams()");

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
}
