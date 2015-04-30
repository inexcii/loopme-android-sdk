package com.loopme;

import java.io.IOException;
import java.util.concurrent.Future;

import android.graphics.SurfaceTexture;
import com.loopme.Logging.LogLevel;
import com.loopme.tasks.VideoTask;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

class VideoController implements OnBufferingUpdateListener,
OnPreparedListener, OnErrorListener, OnCompletionListener, MediaPlayer.OnInfoListener, MediaPlayer.OnVideoSizeChangedListener {

	private static final String LOG_TAG = VideoController.class.getSimpleName();
	
	private volatile MediaPlayer mPlayer;
	private AdView mAdView;
	private StretchOption mStretch = StretchOption.NONE;
	
	private Handler mHandler;
	private Runnable mRunnable;
	
	private String mAppKey;
	
	private int mVideoDuration;
	private Future mFuture;
	private VideoTask mVideoTask;
	
	private AdFormat mFormat;

	private boolean mWasError;
	private boolean mIsPlayerPrepared;

	private int mVideoWidth;
	private int mVideoHeight;

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		Logging.out(LOG_TAG, "onInfo: " + what + " " + extra, LogLevel.DEBUG);
		return false;
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		mVideoWidth = width;
		mVideoHeight = height;
	}

	public enum StretchOption {
		NONE,
		STRECH,
		NO_STRETCH
	}
	
	public VideoController(String appKey, AdView adview, AdFormat format) {
		mAppKey = appKey;
		mAdView = adview;
		mFormat = format;
		mHandler = new Handler(Looper.getMainLooper());
		
		initRunnable();
	}
	
	public void loadVideoFile(String videoUrl, Context context) {
		mVideoTask = new VideoTask(videoUrl, context, 
				new VideoTask.Listener() {
					
					@Override
					public void onComplete(String filePath) {
						if (filePath != null) {
							preparePlayer(filePath);
						} else {
                            BaseAd ad;
                            if (mFormat == AdFormat.INTERSTITIAL) {
                                ad = LoopMeAdHolder.getInterstitial(mAppKey, null);
                            } else {
                                ad = LoopMeAdHolder.getBanner(mAppKey, null);
                            }
							ad.onAdLoadFail(LoopMeError.VIDEO_LOADING);
						}
					}
				});
		mFuture = ExecutorHelper.getExecutor().submit(mVideoTask);
	}
	
	private void setVideoState(VideoState state) {
		if (mAdView != null) {
			mAdView.setVideoState(state);
		}
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
				if (mPlayer.isPlaying()) {
					return;
				}
				Logging.out(LOG_TAG, "Play video", LogLevel.DEBUG);
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
	
	void setStreachVideoParameter(StretchOption option) {
		mStretch = option;
	}
	
	void destroy(boolean interruptFile) {
		Logging.out(LOG_TAG, "Destroy VideoController", LogLevel.DEBUG);
		if (mHandler != null) {
			mHandler.removeCallbacks(mRunnable);
		}
		mRunnable = null;
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
		if (mFuture != null) {
			boolean b = mFuture.cancel(true);
			if (b || interruptFile) {
				mVideoTask.deleteCorruptedVideoFile();
				mVideoTask = null;
			}
			mFuture = null;
		}
	}
	
	private void preparePlayer(String filePath) {
		mPlayer = new MediaPlayer();
		mPlayer.setLooping(false);
		mPlayer.setOnBufferingUpdateListener(this);
		mPlayer.setOnPreparedListener(this);
		mPlayer.setOnErrorListener(this);
		mPlayer.setOnCompletionListener(this);
		mPlayer.setOnInfoListener(this);
		mPlayer.setOnVideoSizeChangedListener(this);

		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mPlayer.setDataSource(filePath);
			mPlayer.prepareAsync();
		} catch (IllegalStateException e) {
			setVideoState(VideoState.BROKEN);
		} catch (IOException e) {
			setVideoState(VideoState.BROKEN);
		}
	}
	
	void muteVideo(boolean mute) {
		if (mPlayer != null) {
			mAdView.setVideoMute(mute);
			if (mute) {
				mPlayer.setVolume(0f, 0f);
			} else {
				mPlayer.setVolume(1f, 1f);
			}
		}
	}
	
	boolean isMediaPlayerValid() {
		return mAdView != null && mAdView.getCurrentVideoState() == VideoState.READY;
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		Logging.out(LOG_TAG, "onPrepared", LogLevel.DEBUG);
		mIsPlayerPrepared = true;
		setVideoState(VideoState.READY);
		
		mVideoDuration = mPlayer.getDuration();
		mAdView.setVideoDuration(mVideoDuration);
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		Logging.out(LOG_TAG, "Buffered " + percent + "%", LogLevel.DEBUG);
		setVideoState(VideoState.BUFFERING);
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		switch (extra) {
			case MediaPlayer.MEDIA_ERROR_IO:
				Logging.out(LOG_TAG, "onError: MediaPlayer.MEDIA_ERROR_IO", LogLevel.ERROR);
				break;

			case MediaPlayer.MEDIA_ERROR_MALFORMED:
				Logging.out(LOG_TAG, "onError: MEDIA_ERROR_MALFORMED", LogLevel.ERROR);
				break;

			case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
				Logging.out(LOG_TAG, "onError: MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK",
						LogLevel.ERROR);
				break;

			case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
				Logging.out(LOG_TAG, "onError: MEDIA_ERROR_SERVER_DIED", LogLevel.ERROR);
				break;

			case MediaPlayer.MEDIA_ERROR_UNKNOWN:
				Logging.out(LOG_TAG, "onError: MEDIA_ERROR_UNKNOWN", LogLevel.ERROR);
				break;

			case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
				Logging.out(LOG_TAG, "onError: MEDIA_ERROR_TIMED_OUT", LogLevel.ERROR);
				break;

			case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
				Logging.out(LOG_TAG, "onError: MEDIA_ERROR_UNSUPPORTED", LogLevel.ERROR);
				break;

			default:
				Logging.out(LOG_TAG, "onError: " + extra, LogLevel.ERROR);
				break;
		}

		mHandler.removeCallbacks(mRunnable);
		mAdView.setWebViewState(AdView.WebviewState.HIDDEN);
		mAdView.setVideoState(VideoState.PAUSED);

		if (mFormat == AdFormat.BANNER) {
			LoopMeBanner banner = LoopMeAdHolder.getBanner(mAppKey, null);
			banner.playbackFinishedWithError();
		}
		mp.setOnErrorListener(null);
		mp.setOnCompletionListener(null);

		mPlayer.reset();

		mWasError = true;
		return true;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (mAdView.getCurrentVideoState() != VideoState.COMPLETE) {
			mHandler.removeCallbacks(mRunnable);
			mAdView.setVideoCurrentTime(mVideoDuration);
			mAdView.setVideoState(VideoState.COMPLETE);
			sendVideoReachEndNotification();
		}
	}
	
	private void sendVideoReachEndNotification() {
		BaseAd base;
		if (mFormat == AdFormat.BANNER) {
			base = LoopMeAdHolder.getBanner(mAppKey, null);
		} else {
			base = LoopMeAdHolder.getInterstitial(mAppKey, null);
		}
		if (base != null) {
			base.onAdVideoDidReachEnd();
		}
	}
	
	void resizeVideo(final TextureView texture, int viewWidth, int viewHeight) {
    	
		if (mPlayer == null) {
			return;
		}
		
		if (mVideoHeight == 0 || mVideoWidth == 0) {
			return;
		}

	    FrameLayout.LayoutParams lp = (LayoutParams) texture.getLayoutParams();
	    lp.gravity = Gravity.CENTER;

	    int blackLines;
	    float percent;
	    
	    if (mVideoWidth > mVideoHeight) {
	    	lp.width = viewWidth;
	    	lp.height = (int) (((float)mVideoHeight / (float)mVideoWidth) * (float)viewWidth);

	    	blackLines = viewHeight - lp.height;
	    	percent = blackLines * 100 / lp.height; 
	    } else {
	    	lp.height = viewHeight;
	    	lp.width = (int) (((float)mVideoWidth / (float)mVideoHeight) * (float)viewHeight);
	    	
	    	blackLines = viewWidth - lp.width;
	    	percent = blackLines * 100 / lp.width;
	    }
	    
	    Logging.out(LOG_TAG, "stretch param  " + mStretch.toString(), LogLevel.DEBUG);
	    switch (mStretch) {
	    case NONE:
	    	if (percent < 11) {
		    	lp.width = viewWidth;
		    	lp.height = viewHeight;
		    }
	    	break;
	    	
	    case STRECH:
	    	lp.width = viewWidth;
	    	lp.height = viewHeight;
	    	break;
	    	
	    case NO_STRETCH:
	    	//
	    	break;
	    }
	    texture.setLayoutParams(lp);
	}

	void setSurface(final TextureView textureView) {
		ExecutorHelper.getExecutor().submit(new Runnable() {

			@Override
			public void run() {
				if (textureView != null && textureView.isAvailable()) {
					SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
					Surface surface = new Surface(surfaceTexture);
					Logging.out(LOG_TAG, "mPlayer.setSurface()", LogLevel.DEBUG);
					mPlayer.setSurface(surface);

				} else {
					Logging.out(LOG_TAG, "textureView not Available ", LogLevel.DEBUG);
				}
			}
		});
	}
}