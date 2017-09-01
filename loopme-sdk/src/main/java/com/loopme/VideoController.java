package com.loopme;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Surface;

import com.loopme.adview.AdView;
import com.loopme.common.Logging;
import com.loopme.common.LoopMeError;
import com.loopme.common.Utils;
import com.loopme.constants.AdFormat;
import com.loopme.constants.VideoState;
import com.loopme.constants.WebviewState;
import com.loopme.debugging.ErrorLog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.loopme.common.EventManager.EVENT_VIDEO_25;
import static com.loopme.common.EventManager.EVENT_VIDEO_50;
import static com.loopme.common.EventManager.EVENT_VIDEO_75;

class VideoController implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private static final String LOG_TAG = VideoController.class.getSimpleName();
    private static final int BUFFERING_MILLIS_IN_FUTURE = 2000;
    private static final int BUFFERING_COUNTDOWN_INTERVAL = 1000;
    private final long DELAY_TIME = 200;
    private volatile MediaPlayer mMediaPlayer;
    private int mVideoDuration;
    private int mVideoPositionWhenError;
    private boolean mMuteState = false;
    private boolean mWasError;
    private boolean mIsSurfaceTextureAvailable;
    private boolean mWaitForVideo;

    private Handler mHandler;
    private Runnable mRunnable;

    private AdView mAdView;
    private Context mContext;

    private String mFileRest;

    private Callback mCallback;
    private Surface mSurface;

    private CountDownTimer mBufferingTimer;

    private String mAppKey;
    private int mFormat = AdFormat.INTERSTITIAL;

    private int mQuarter25;
    private int mQuarter50;
    private int mQuarter75;

    private boolean mIs360;

    private OnMoatEventListener mOnMoatEventListener;
    private Map<String, Integer> mQuartileEventsMap;
    private int mCurrentPosition;
    private boolean mIsFirstLaunch = true;

    public void resumeVideo() {
        if (mAdView.getCurrentVideoState() == VideoState.PAUSED) {
            playVideo(mCurrentPosition, mIs360);
        }
    }


    public interface Callback {
        void onVideoReachEnd();

        void onFail(LoopMeError error);

        void onVideoSizeChanged(int width, int height);

        void onPostponePlay(int position);

        void onPlaybackFinishedWithError();
    }

    public VideoController(AdView adView, Callback callback, String appKey, int format, OnMoatEventListener onMoatEventListener) {
        mAdView = adView;
        mCallback = callback;
        mContext = adView.getContext();
        mAppKey = appKey;
        mFormat = format;
        mHandler = new Handler(Looper.getMainLooper());
        mOnMoatEventListener = onMoatEventListener;
        initProgressRunnable();
        mQuartileEventsMap = new HashMap<>();
    }

    void contain360(boolean b) {
        mIs360 = b;
    }

    public void destroy() {
        Logging.out(LOG_TAG, "destroy");
        onStopMoatTracking();
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
        stopBuffering();
        mRunnable = null;
        releasePlayer();
        mCallback = null;
        mSurface = null;
    }

    private void initProgressRunnable() {
        mRunnable = new Runnable() {

            @Override
            public void run() {
                if (mAdView == null) {
                    return;
                }
                mCurrentPosition = getCurrentPosition();
                mAdView.setVideoCurrentTime(mCurrentPosition);
                updateCurrentVolume();
                if (mCurrentPosition < mVideoDuration) {
                    mHandler.postDelayed(mRunnable, DELAY_TIME);
                }
            }
        };
    }

    private int roundNumberToHundredth(int number) {
        return (number / 100) * 100;
    }

    public int getCurrentPosition() {
        return mMediaPlayer == null ? 0 : mMediaPlayer.getCurrentPosition();
    }

    public void releasePlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
        }
    }

    public void setSurface(Surface surface) throws IllegalStateException {
        mSurface = surface;
        if (mMediaPlayer != null) {
            mMediaPlayer.setSurface(surface);
        }
    }

    public void waitForVideo() {
        if (mWaitForVideo) {
            releasePlayer();
            initPlayer(mFileRest);
            setSurface(mSurface);

            if (mAdView.getCurrentWebViewState() == WebviewState.VISIBLE) {
                startMediaPlayer();
            }
            seekTo(mVideoPositionWhenError);
            mHandler.postDelayed(mRunnable, DELAY_TIME);

            setVideoState(VideoState.PLAYING);
        }
    }

    public void seekTo(int position) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(position);
        }
    }

    public void initPlayerFromFile(String filePath) {
        mMediaPlayer = new MediaPlayer();
        initPlayerListeners();
        mMediaPlayer.setOnPreparedListener(this);

        try {
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepareAsync();

        } catch (IllegalStateException | IOException e) {
            Logging.out(LOG_TAG, e.getMessage());
            setVideoState(VideoState.BROKEN);
        }
    }

    private void initPlayer(String filePath) {
        mMediaPlayer = MediaPlayer.create(mContext, Uri.parse(filePath));
        initPlayerListeners();
    }

    private void initPlayerListeners() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setLooping(false);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }

    public void muteVideo(boolean mute) {
        if(mMediaPlayer != null){
            if (mute) {
                mMediaPlayer.setVolume(0f, 0f);
                onVolumeChangedMoatTracking();

            } else {
                float systemVolume = Utils.getSystemVolume();
                mMediaPlayer.setVolume(systemVolume, systemVolume);
            }
        }
        mMuteState = mute;
    }

    private void applyMuteSettings() {
        if (mMediaPlayer != null) {
            Logging.out(LOG_TAG, "applyMuteSettings " + mMuteState);
            muteVideo(mMuteState);
        }
    }

    private void updateCurrentVolume() {
        if (!mMuteState && mMediaPlayer != null) {
            float systemVolume = Utils.getSystemVolume();
            mMediaPlayer.setVolume(systemVolume, systemVolume);
        }
    }

    public void setSurfaceTextureAvailable(boolean isAvailable) {
        mIsSurfaceTextureAvailable = isAvailable;
    }

    private boolean isPlayerReadyForPlay() {
        return mMediaPlayer != null && mAdView != null && !mWasError;
    }

    public void playVideo(int time, boolean is360) {
        if (isPlayerReadyForPlay()) {
            if(mIsFirstLaunch && mFormat == AdFormat.BANNER){
                onStartMoatTracking(mMediaPlayer, mAdView);
                mIsFirstLaunch = false;
            }
            if (!is360 && !mIsSurfaceTextureAvailable) {
                Logging.out(LOG_TAG, "postpone play (surface not available)");
                if(mCallback != null){
                    mCallback.onPostponePlay(time);
                }
                return;
            }

            try {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    return;
                }
                Logging.out(LOG_TAG, "Play video " + time);
                applyMuteSettings();
                if (time == 10) {
                    mMediaPlayer.seekTo(0);
                }
                startMediaPlayer();
                mAdView.setVideoState(VideoState.PLAYING);

                mHandler.postDelayed(mRunnable, DELAY_TIME);

            } catch (IllegalStateException e) {
                Logging.out(LOG_TAG, "playVideo:" + e.getMessage());
            }
        }
    }

    public void pauseVideo() {
        if (mMediaPlayer != null && mAdView != null && !mWasError) {
            try {
                if (mMediaPlayer.isPlaying()) {
                    Logging.out(LOG_TAG, "Pause video");
                    mHandler.removeCallbacks(mRunnable);
                    mMediaPlayer.pause();
                    mAdView.setVideoState(VideoState.PAUSED);
                } else {
                    mAdView.setVideoState(VideoState.IDLE);
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
                Logging.out(LOG_TAG, e.getMessage());
            }
        }
    }

    public void setFileRest(String filePath) {
        mFileRest = filePath;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mAdView.getCurrentVideoState() != VideoState.COMPLETE) {
            mHandler.removeCallbacks(mRunnable);
            mAdView.setVideoCurrentTime(mVideoDuration);
            mAdView.setVideoState(VideoState.COMPLETE);
            onVideoReachEnd();
            onStopMoatTracking();
            onCompletionMoatTracking();
        }
    }

    private void onVideoReachEnd() {
        if (mCallback != null) {
            mCallback.onVideoReachEnd();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Logging.out(LOG_TAG, "onError: " + extra);

        mHandler.removeCallbacks(mRunnable);
        destroyListeners();
        if (extra == MediaPlayer.MEDIA_ERROR_IO) {
            Logging.out(LOG_TAG, "end of preview file");
            if (!TextUtils.isEmpty(mFileRest)) {
                mVideoPositionWhenError = mp.getCurrentPosition();
                releasePlayer();
                initPlayer(mFileRest);
                setSurface(mSurface);
                startMediaPlayer();
                mMediaPlayer.seekTo(mVideoPositionWhenError);
                mHandler.postDelayed(mRunnable, DELAY_TIME);

            } else {
                mWaitForVideo = true;
                mVideoPositionWhenError = mp.getCurrentPosition();
                setVideoState(VideoState.BUFFERING);
                startBuffering();
            }
            return true;
        }
        if (mAdView.getCurrentVideoState() == VideoState.BROKEN ||
                mAdView.getCurrentVideoState() == VideoState.IDLE) {
            onFail();
        } else {
            mAdView.setWebViewState(WebviewState.HIDDEN);
            mAdView.setVideoState(VideoState.PAUSED);
            onPlaybackFinishedWithError();
            resetMediaPlayer();
            mWasError = true;
        }
        return true;
    }

    private void startMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            if(mFormat == AdFormat.INTERSTITIAL){
                onStartMoatTracking(mMediaPlayer, mAdView);
            }
        }
    }

    private void destroyListeners() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setOnErrorListener(null);
            mMediaPlayer.setOnCompletionListener(null);
        }
    }

    private void resetMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        }
    }

    private void onPlaybackFinishedWithError() {
        if (mCallback != null) {
            mCallback.onPlaybackFinishedWithError();
        }
    }

    private void startBuffering() {
        mBufferingTimer = new CountDownTimer(BUFFERING_MILLIS_IN_FUTURE, BUFFERING_COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                ErrorLog.post("Buffering 2 seconds");
            }
        };
        mBufferingTimer.start();
    }

    private void onFail() {
        if (mCallback != null) {
            mCallback.onFail(new LoopMeError("Error during video loading"));
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        setVideoState(VideoState.READY);
        extractVideoInfo(mediaPlayer);
        stopBuffering();
    }

    private void stopBuffering() {
        if (mBufferingTimer != null) {
            mBufferingTimer.cancel();
        }
    }

    private void setVideoState(int state) {
        if (mAdView != null) {
            mAdView.setVideoState(state);
        }
    }

    private void extractVideoInfo(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            if (mCallback != null) {
                mCallback.onVideoSizeChanged(
                        mediaPlayer.getVideoWidth(),
                        mediaPlayer.getVideoHeight());
            }
            mVideoDuration = mediaPlayer.getDuration();
            if (mAdView != null) {
                mAdView.setVideoDuration(mVideoDuration);
            }
        }
        mQuarter25 = roundNumberToHundredth(mVideoDuration / 4);
        mQuarter50 = roundNumberToHundredth(mVideoDuration / 2);
        mQuarter75 = mQuarter25 + mQuarter50;
        fillQuartileEventsList();
    }

    private void fillQuartileEventsList() {
        mQuartileEventsMap.clear();
        mQuartileEventsMap.put(EVENT_VIDEO_25, mQuarter25);
        mQuartileEventsMap.put(EVENT_VIDEO_50, mQuarter50);
        mQuartileEventsMap.put(EVENT_VIDEO_75, mQuarter75);
    }

    public interface OnMoatEventListener {
        void onStartMoatTracking(MediaPlayer mediaPlayer, AdView adView);

        void onStopMoatTracking();

        void onChangeViewMoatTracking(AdView adView);

        void onPreparedMoatTracking(MediaPlayer mediaPlayer, AdView adView);

        void onCompletionMoatTracking(MediaPlayer mediaPlayer);

        void onVolumeChangedMoatTracking(int playerPositionInMillis, double volume);

        void initMoatNativeTracker();
    }

    public void onStartMoatTracking(MediaPlayer mediaPlayer, AdView adView) {
        if (mOnMoatEventListener != null) {
            mOnMoatEventListener.initMoatNativeTracker();
            mOnMoatEventListener.onChangeViewMoatTracking(this.mAdView);
            mOnMoatEventListener.onStartMoatTracking(mediaPlayer, adView);
        }
    }

    public void onStopMoatTracking() {
        if (mOnMoatEventListener != null) {
            mOnMoatEventListener.onStopMoatTracking();
        }
    }

    public void onVolumeChangedMoatTracking() {
        if (mOnMoatEventListener != null) {
            mOnMoatEventListener.onVolumeChangedMoatTracking(mMediaPlayer.getCurrentPosition(), Utils.getSystemVolume());
        }
    }

    public void onCompletionMoatTracking() {
        if (mOnMoatEventListener != null) {
            mOnMoatEventListener.onCompletionMoatTracking(mMediaPlayer);
        }
    }

    private void onPreparedMoatTracking(MediaPlayer mediaPlayer) {
        if (mOnMoatEventListener != null) {
            this.mOnMoatEventListener.onPreparedMoatTracking(mediaPlayer, mAdView);
        }
    }
}