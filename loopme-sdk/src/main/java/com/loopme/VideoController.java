package com.loopme;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import com.loopme.adview.AdView;
import com.loopme.common.Logging;
import com.loopme.common.LoopMeError;
import com.loopme.common.Utils;
import com.loopme.constants.VideoState;
import com.loopme.constants.WebviewState;
import com.loopme.debugging.ErrorLog;

import java.io.IOException;

class VideoController implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private static final String LOG_TAG = VideoController.class.getSimpleName();

    private volatile MediaPlayer mPlayer;
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
    private int mFormat;

    private int mQuarter25;
    private int mQuarter50;
    private int mQuarter75;

    private boolean mIs360;

    public interface Callback {
        void onVideoReachEnd();

        void onFail(LoopMeError error);

        void onVideoSizeChanged(int width, int height);

        void postponePlay(int position);

        void playbackFinishedWithError();
    }

    public VideoController(AdView adView, Callback callback, String appKey, int format) {
        mAdView = adView;
        mCallback = callback;
        mContext = adView.getContext();
        mAppKey = appKey;
        mFormat = format;
        mHandler = new Handler(Looper.getMainLooper());
        initProgressRunnable();
    }

    void contain360(boolean b) {
        mIs360 = b;
    }

    public void destroy() {
        Logging.out(LOG_TAG, "destroy");
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
        if (mBufferingTimer != null) {
            mBufferingTimer.cancel();
        }
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
                int position = getCurrentPosition();
                mAdView.setVideoCurrentTime(position);
                updateCurrentVolume();

                if (position < mVideoDuration) {
                    mHandler.postDelayed(mRunnable, 100);
                }
            }
        };
    }

    public int getCurrentPosition() {
        return mPlayer == null ? 0 : mPlayer.getCurrentPosition();
    }

    public void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.reset();
            mPlayer.release();
        }
    }

    public void setSurface(Surface surface) throws IllegalStateException {
        Log.d(LOG_TAG, "setSurface " + surface);
        mSurface = surface;
        if (mPlayer != null) {
            mPlayer.setSurface(surface);
        }
    }

    public void waitForVideo() {
        if (mWaitForVideo) {
            releasePlayer();

            initPlayer(mFileRest);

            mPlayer.setSurface(mSurface);

            if (mAdView.getCurrentWebViewState() == WebviewState.VISIBLE) {
                mPlayer.start();
            }
            seekTo(mVideoPositionWhenError);
            Logging.out(LOG_TAG, "waitForVideo mHandler.postDelayed");
            mHandler.postDelayed(mRunnable, 200);

            setVideoState(VideoState.PLAYING);
        }
    }

    public void seekTo(int position) {
        if (mPlayer != null) {
            mPlayer.seekTo(position);
        }
    }

    public void initPlayerFromFile(String filePath) {
        mPlayer = new MediaPlayer();
        initPlayerListeners();
        mPlayer.setOnPreparedListener(this);

        try {
            mPlayer.setDataSource(filePath);
            mPlayer.prepareAsync();

        } catch (IllegalStateException e) {
            Logging.out(LOG_TAG, e.getMessage());
            setVideoState(VideoState.BROKEN);

        } catch (IOException e) {
            Logging.out(LOG_TAG, e.getMessage());
            setVideoState(VideoState.BROKEN);
        }
    }

    private void initPlayer(String filePath) {
        mPlayer = MediaPlayer.create(mContext, Uri.parse(filePath));
        initPlayerListeners();
    }

    private void initPlayerListeners() {
        mPlayer.setLooping(false);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnCompletionListener(this);

        mPlayer.setAudioStreamType(AudioManager.STREAM_RING);
    }

    public void muteVideo(boolean mute) {
        if (mute) {
            mPlayer.setVolume(0f, 0f);
        } else {
            float systemVolume = Utils.getSystemVolume();
            mPlayer.setVolume(systemVolume, systemVolume);
        }
        mMuteState = mute;
    }

    private void applyMuteSettings() {
        if (mPlayer != null) {
            Logging.out(LOG_TAG, "applyMuteSettings " + mMuteState);
            muteVideo(mMuteState);
        }
    }

    private void updateCurrentVolume() {
        if (!mMuteState) {
            float systemVolume = Utils.getSystemVolume();
            mPlayer.setVolume(systemVolume, systemVolume);
        }
    }

    public void setSurfaceTextureAvailable(boolean b) {
        mIsSurfaceTextureAvailable = b;
    }

    private boolean isPlayerReadyForPlay() {
        return mPlayer != null && mAdView != null && !mWasError;
    }

    public void playVideo(int time, boolean is360) {
        if (isPlayerReadyForPlay()) {
            if (!is360 && !mIsSurfaceTextureAvailable) {
                Logging.out(LOG_TAG, "postpone play (surface not available)");
                mCallback.postponePlay(time);
                return;
            }

            try {
                if (mPlayer.isPlaying()) {
                    return;
                }

                Logging.out(LOG_TAG, "Play video " + time);
                applyMuteSettings();
                if (time == 10) {
                    mPlayer.seekTo(0);
                }

                mPlayer.start();
                mAdView.setVideoState(VideoState.PLAYING);

                mHandler.postDelayed(mRunnable, 200);

            } catch (IllegalStateException e) {
                Logging.out(LOG_TAG, "playVideo:" + e.getMessage());
            }
        }
    }

    public void pauseVideo() {
        if (mPlayer != null && mAdView != null && !mWasError) {
            try {
                if (mPlayer.isPlaying()) {
                    Logging.out(LOG_TAG, "Pause video");
                    mHandler.removeCallbacks(mRunnable);
                    mPlayer.pause();
                    mAdView.setVideoState(VideoState.PAUSED);
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
            if (mCallback != null) {
                mCallback.onVideoReachEnd();
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Logging.out(LOG_TAG, "onError: " + extra);

        mHandler.removeCallbacks(mRunnable);

        if (extra == MediaPlayer.MEDIA_ERROR_IO) {
            Logging.out(LOG_TAG, "end of preview file");
            mPlayer.setOnErrorListener(null);
            mPlayer.setOnCompletionListener(null);
            if (!TextUtils.isEmpty(mFileRest)) {
                mVideoPositionWhenError = mp.getCurrentPosition();
                mPlayer.reset();
                mPlayer.release();

                mPlayer = MediaPlayer.create(mContext, Uri.parse(mFileRest));
                initPlayerListeners();
                mPlayer.setSurface(mSurface);

                mPlayer.start();
                mPlayer.seekTo(mVideoPositionWhenError);
                Logging.out(LOG_TAG, "mHandler.postDelayed");
                mHandler.postDelayed(mRunnable, 200);

            } else {
                mWaitForVideo = true;
                mVideoPositionWhenError = mp.getCurrentPosition();
                setVideoState(VideoState.BUFFERING);

                mBufferingTimer = new CountDownTimer(2000, 1000) {
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
            return true;
        }

        if (mPlayer != null) {
            mPlayer.setOnErrorListener(null);
            mPlayer.setOnCompletionListener(null);
        }

        if (mAdView.getCurrentVideoState() == VideoState.BROKEN ||
                mAdView.getCurrentVideoState() == VideoState.IDLE) {
            if (mCallback != null) {
                mCallback.onFail(new LoopMeError("Error during video loading"));
            }
        } else {

            mAdView.setWebViewState(WebviewState.HIDDEN);
            mAdView.setVideoState(VideoState.PAUSED);

            mCallback.playbackFinishedWithError();

            if (mPlayer != null) {
                mPlayer.reset();
            }
            mWasError = true;
        }
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Logging.out(LOG_TAG, "onPrepared");
        setVideoState(VideoState.READY);
        extractVideoInfo(mp);
        if (mBufferingTimer != null) {
            mBufferingTimer.cancel();
        }
    }

    private void setVideoState(int state) {
        if (mAdView != null) {
            mAdView.setVideoState(state);
        }
    }

    private void extractVideoInfo(MediaPlayer mp) {
        if (mp != null) {
            int width = mp.getVideoWidth();
            int height = mp.getVideoHeight();
            if (mCallback != null) {
                mCallback.onVideoSizeChanged(width, height);
            }
            mVideoDuration = mp.getDuration();
            if (mAdView != null) {
                mAdView.setVideoDuration(mVideoDuration);
            }
            mQuarter25 = mVideoDuration / 4;
            mQuarter50 = mVideoDuration / 2;
            mQuarter75 = mQuarter25 + mQuarter50;
        }
    }
}
