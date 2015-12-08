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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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

import com.loopme.Logging.LogLevel;

import java.io.IOException;

class ViewController implements TextureView.SurfaceTextureListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener{

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


    private Handler mHandler;
    private Runnable mRunnable;

    private static volatile MediaPlayer mPlayer;
    private int mVideoDuration;
    private VideoLoader mVideoLoader;
    private boolean mMuteState = false;
    private StretchOption mStretch = StretchOption.NONE;
    private boolean mWasError;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mResizeWidth;
    private int mResizeHeight;

    private CountDownTimer mBufferingTimer;
    private int mBufferingValue = -1;

    private boolean mIsSurfaceTextureAvailable;
    private boolean mPlayerReady;
    private boolean mPostponePlay;
    private int mPostponePlayPosition;

    public enum StretchOption {
        NONE,
        STRECH,
        NO_STRETCH
    }

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

        mHandler = new Handler(Looper.getMainLooper());
        initRunnable();
    }

    private void initRunnable() {
        mRunnable = new Runnable() {

            @Override
            public void run() {
                if (mPlayer == null || mAdView == null) {
                    return;
                }
                int position = mPlayer.getCurrentPosition();
                mAdView.setVideoCurrentTime(position);

                if (position < mVideoDuration) {
                    mHandler.postDelayed(mRunnable, 200);
                }
            }
        };
    }

    void resetFullScreenCommandCounter() {
        mIsFirstFullScreenCommand = true;
    }

    void destroy(boolean interruptFile) {
        mBridgeListener = null;
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
        mRunnable = null;
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        if (mVideoLoader != null) {
            mVideoLoader.stop(interruptFile);
        }

        if (mAdView != null) {
            mAdView.stopLoading();
            mAdView.clearCache(true);
            mAdView = null;
            Logging.out(LOG_TAG, "AdView destroyed", LogLevel.DEBUG);
        }
        mMinimizedMode = null;
    }

    void setWebViewState(int state) {
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

    int getCurrentDisplayMode() {
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

    void setHorizontalScrollingOrientation() {
        mHorizontalScrollOrientation = true;
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
        Logging.out(LOG_TAG, "switch to minimized mode", LogLevel.DEBUG);
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

        Logging.out(LOG_TAG, "switch to normal mode", LogLevel.DEBUG);
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

        mAdView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
    }

    void setMinimizedMode(MinimizedMode mode) {
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
            Logging.out(LOG_TAG, "loadDataWithBaseURL", LogLevel.DEBUG);
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
        Logging.out(LOG_TAG, "JS command: load success", LogLevel.DEBUG);
        mAd.startExpirationTimer();
        mAd.onAdLoadSuccess();
    }

    private void handleLoadFail(String mess) {
        Logging.out(LOG_TAG, "JS command: load fail", LogLevel.DEBUG);
        if (mAd != null) {
            mAd.onAdLoadFail(new LoopMeError("Failed to process ad"));
        }
    }

    private void handleVideoLoad(String videoUrl) {
        Logging.out(LOG_TAG, "JS command: load video " + videoUrl, LogLevel.DEBUG);

        mIsVideoPresented = true;
        boolean preload = mAd.getAdParams().getPartPreload();
        loadVideoFile(videoUrl, mAd.getContext(), preload);
    }

    private void loadVideoFile(String videoUrl, Context context, boolean preload) {

        mVideoLoader = new VideoLoader(videoUrl, preload, context, new VideoLoader.Callback() {

            @Override
            public void onError(LoopMeError error) {
                sendLoadFail(error);
            }

            @Override
            public void onLoadFromUrl(final String url) {
                ExecutorHelper.getExecutor().submit(new Runnable() {
                    @Override
                    public void run() {
                        Logging.out(LOG_TAG, "onLoadFromUrl", LogLevel.DEBUG);
                        preparePlayerFromUrl(url);
                    }
                });

            }

            @Override
            public void onLoadFromFile(String filePath) {
                Logging.out(LOG_TAG, "onLoadFromFile", LogLevel.DEBUG);
                preparePlayerFromFile(filePath);
            }
        });
        mVideoLoader.start();
    }

    private void sendLoadFail(LoopMeError error) {
        if (mAd != null) {
            mAd.onAdLoadFail(error);
        }
    }

    private void preparePlayerFromFile(String filePath) {
        mPlayer = new MediaPlayer();
        initPlayerListeners();
        mPlayer.setOnPreparedListener(this);

        try {
            mPlayer.setDataSource(filePath);
            mPlayer.prepareAsync();

        } catch (IllegalStateException e) {
            Logging.out(LOG_TAG, e.getMessage(), LogLevel.ERROR);
            setVideoState(VideoState.BROKEN);

        } catch (IOException e) {
            Logging.out(LOG_TAG, e.getMessage(), LogLevel.ERROR);
            setVideoState(VideoState.BROKEN);
        }
    }

    private void preparePlayerFromUrl(String videoUrl) {
        if (TextUtils.isEmpty(videoUrl)) {
            return;
        }
        mPlayer = MediaPlayer.create(mAdView.getContext(), Uri.parse(videoUrl));
        initPlayerListeners();
        mPlayer.setOnBufferingUpdateListener(this);
        mPlayer.setVolume(0, 0);
        mPlayer.start();
    }

    private void setVideoState(int state) {
        if (mAdView != null) {
            mAdView.setVideoState(state);
        }
    }

    private void initPlayerListeners() {
        mPlayer.setLooping(false);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnCompletionListener(this);

        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    private void handleVideoMute(boolean mute) {
        Logging.out(LOG_TAG, "JS command: video mute " + mute, LogLevel.DEBUG);

        if (mPlayer != null) {
            mAdView.setVideoMute(mute);

            if (mAdView.getCurrentVideoState() == VideoState.PLAYING) {
                if (mute) {
                    mPlayer.setVolume(0f, 0f);
                } else {
                    float systemVolume = Utils.getSystemVolume();
                    mPlayer.setVolume(systemVolume, systemVolume);
                }
            }
            mMuteState = mute;
        }
    }

    private void handleVideoPlay(final int time) {
        Logging.out(LOG_TAG, "JS command: play video " + time, LogLevel.DEBUG);

        playVideo(time);

        if (mDisplayMode == DisplayMode.MINIMIZED) {
            Utils.animateAppear(mMinimizedView);
        }
    }

    private void handleVideoPause(int time) {
        Logging.out(LOG_TAG, "JS command: pause video " + time, LogLevel.DEBUG);
        pauseVideo(time);
    }

    private void handleClose() {
        Logging.out(LOG_TAG, "JS command: close", LogLevel.DEBUG);
        mAd.dismiss();
    }

    private void handleVideoStretch(boolean b) {
        Logging.out(LOG_TAG, "JS command: stretch video ", LogLevel.DEBUG);
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

    void pauseVideo(int time) {
        if (mPlayer != null && mAdView != null && !mWasError) {
            try {
                if (mPlayer.isPlaying()) {
                    Logging.out(LOG_TAG, "Pause video", LogLevel.DEBUG);
                    mHandler.removeCallbacks(mRunnable);
                    mPlayer.pause();
                    mAdView.setVideoState(VideoState.PAUSED);
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
                Logging.out(LOG_TAG, e.getMessage(), LogLevel.ERROR);
            }
        }
    }

    void playVideo(int time) {
        if (mPlayer != null && mAdView != null && !mWasError) {
            try {
                if (!mIsSurfaceTextureAvailable) {
                    Logging.out(LOG_TAG, "postpone play (surface not available)", LogLevel.DEBUG);
                    mPostponePlay = true;
                    mPostponePlayPosition = time;
                    return;
                }
                if (mPlayer.isPlaying()) {
                    return;
                }

                Logging.out(LOG_TAG, "Play video", LogLevel.DEBUG);
                applyMuteSettings();
                if (time > 0) {
                    mPlayer.seekTo(time);
                }

                mPlayer.start();
                mAdView.setVideoState(VideoState.PLAYING);

                mHandler.postDelayed(mRunnable, 200);

            } catch (IllegalStateException e) {
                e.printStackTrace();
                Logging.out(LOG_TAG, e.getMessage(), LogLevel.ERROR);
            }
        }
    }

    private void switchToFullScreenMode() {
        if (mDisplayMode != DisplayMode.FULLSCREEN) {
            Logging.out(LOG_TAG, "switch to fullscreen mode", LogLevel.DEBUG);
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
        Logging.out(LOG_TAG, "Non Js command", LogLevel.DEBUG);
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
            Logging.out(LOG_TAG, "No internet connection", LogLevel.DEBUG);
        }
    }

    private void broadcastAdClickedIntent() {
        Intent intent = new Intent();
        intent.setAction(StaticParams.CLICK_INTENT);
        mAd.getContext().sendBroadcast(intent);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
                                          int height) {

        Logging.out(LOG_TAG, "onSurfaceTextureAvailable", LogLevel.DEBUG);

        mIsSurfaceTextureAvailable = true;
        if (mPostponePlay) {
            Logging.out(LOG_TAG, "play video after postpone", LogLevel.DEBUG);
            playVideo(mPostponePlayPosition);
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
                    Logging.out(LOG_TAG, "WARNING: MinimizedMode is null", LogLevel.ERROR);
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
                Logging.out(LOG_TAG, "Unknown display mode", LogLevel.ERROR);
                break;
        }

        Surface s = new Surface(surface);
        mPlayer.setSurface(s);
        resizeVideo(mTextureView, viewWidth, viewHeight);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
                                            int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Logging.out(LOG_TAG, "onSurfaceTextureDestroyed", LogLevel.DEBUG);
        mIsSurfaceTextureAvailable = false;

        if (mPlayer != null) {
            mPlayer.setSurface(null);
        }
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
        Logging.out(LOG_TAG, "updateLayoutParams()", LogLevel.DEBUG);

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
    public void onCompletion(MediaPlayer mp) {
        if (mAdView.getCurrentVideoState() != VideoState.COMPLETE) {
            mHandler.removeCallbacks(mRunnable);
            mAdView.setVideoCurrentTime(mVideoDuration);
            mAdView.setVideoState(VideoState.COMPLETE);
            mAd.onAdVideoDidReachEnd();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Logging.out(LOG_TAG, "onError: " + extra, LogLevel.ERROR);

        mHandler.removeCallbacks(mRunnable);

        if (mBufferingTimer != null) {
            mBufferingTimer.cancel();
        }

        mp.setOnErrorListener(null);
        mp.setOnCompletionListener(null);

        if (mAdView.getCurrentVideoState() == VideoState.BROKEN ||
                mAdView.getCurrentVideoState() == VideoState.IDLE) {
            sendLoadFail(new LoopMeError("Error during video loading"));
        } else {

            mAdView.setWebViewState(WebviewState.HIDDEN);
            mAdView.setVideoState(VideoState.PAUSED);

            if (mAd.getAdFormat() == AdFormat.BANNER) {
                ((LoopMeBanner) mAd).playbackFinishedWithError();
            }

            mPlayer.reset();
            mWasError = true;
        }
        return true;
    }

    @Override
    public void onBufferingUpdate(final MediaPlayer mp, int percent) {
        Logging.out(LOG_TAG, "onBufferingUpdate " + percent, LogLevel.DEBUG);
        if (mp.isPlaying()) {
            mPlayerReady = true;
        }

        if (percent >= StaticParams.BUFFERING_LEVEL) {

            if (mPlayerReady && mAdView.getCurrentVideoState() == VideoState.IDLE) {
                mp.pause();
                mp.seekTo(10);
                setVideoState(VideoState.READY);
                configMediaPlayer(mp);
            }

            if (mBufferingValue != percent) {
                if (mAd.isShowing()) {
                    restartBufferingTimer();
                }
            }

            if (percent == 100 && mVideoLoader != null) {
                stopBufferingTimer();
                mVideoLoader.downloadVideo();
                mp.setOnBufferingUpdateListener(null);
            }
        }
        mBufferingValue = percent;
    }

    private void restartBufferingTimer() {
        if (mBufferingTimer == null) {
            mBufferingTimer = new CountDownTimer(StaticParams.BUFFERING_TIMEOUT, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    Logging.out(LOG_TAG, "Buffering timeout", LogLevel.DEBUG);
                    if (mAdView != null) {
                        mAdView.setWebViewState(WebviewState.HIDDEN);
                        if (mPlayer != null) {
                            mPlayer.reset();
                        }
                    }
                }
            };
            mBufferingTimer.start();
        } else {
            mBufferingTimer.cancel();
            mBufferingTimer.start();
        }
    }

    private void stopBufferingTimer() {
        if (mBufferingTimer != null) {
            mBufferingTimer.cancel();
            mBufferingTimer = null;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Logging.out(LOG_TAG, "onPrepared", LogLevel.DEBUG);
        setVideoState(VideoState.READY);
        configMediaPlayer(mp);
    }

    private void configMediaPlayer(MediaPlayer mp) {
        mVideoWidth = mp.getVideoWidth();
        mVideoHeight = mp.getVideoHeight();
        configPlayerDuration();
    }

    private void configPlayerDuration() {
        if (mPlayer != null) {
            mVideoDuration = mPlayer.getDuration();
            mAdView.setVideoDuration(mVideoDuration);
        }
    }

    private void applyMuteSettings() {
        if (mPlayer != null) {
            Logging.out(LOG_TAG, "applyMuteSettings " + mMuteState, LogLevel.DEBUG);
            if (mMuteState) {
                mPlayer.setVolume(0f, 0f);
            } else {
                float systemVolume = Utils.getSystemVolume();
                mPlayer.setVolume(systemVolume, systemVolume);
            }
        }
    }
}
