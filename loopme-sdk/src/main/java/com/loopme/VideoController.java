package com.loopme;

import java.io.IOException;
import java.util.concurrent.Future;

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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

class VideoController implements OnBufferingUpdateListener,
OnPreparedListener, OnErrorListener, OnCompletionListener {

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
	
	public enum StretchOption {
		NONE,
		STRECH,
		NO_STRETCH;
	}
	
	public interface Listener {
		void onError(String mess);
	}
	
	public VideoController(String appKey, AdView adview) {
		mAppKey = appKey;
		mAdView = adview;
		mHandler = new Handler(Looper.getMainLooper());
		
		initRunnable();
	}
	
	public void loadVideoFile(String videoUrl, Context context, final Listener listener) {
		mVideoTask = new VideoTask(videoUrl, context, 
				new VideoTask.Listener() {
					
					@Override
					public void onComplete(String filePath) {
						if (filePath != null) {
							preparePlayer(filePath);
						} else {
							if (listener != null) {
								listener.onError("Exception during video file loading/creation");
							}
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
		if (mPlayer != null && mAdView != null) {
			if (mPlayer.isPlaying()) {
				mPlayer.pause();
				mAdView.setVideoState(VideoState.PAUSED);
			}
		}
	}
	
	void playVideo(int time) {
		if (mPlayer != null && mAdView != null) {
			if (time > 0) {
				mPlayer.seekTo(time);
			}
			mPlayer.start();
			mAdView.setVideoState(VideoState.PLAYING);
			mHandler.postDelayed(mRunnable, 200);
		}
	}
	
	private void initRunnable() {
		mRunnable = new Runnable() {
			
			@Override
			public void run() {
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
	
	StretchOption getStreachVideoParameter() {
		return mStretch;
	}

	void destroy(boolean interruptFile) {
		Logging.out(LOG_TAG, "Destroy VideoController", LogLevel.DEBUG);
		if (mHandler != null) {
			mHandler.removeCallbacks(mRunnable);
		}
		mRunnable = null;
		if (mPlayer != null) {
			mPlayer.setOnPreparedListener(null);
			mPlayer.setOnCompletionListener(null);
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
		mPlayer.setOnBufferingUpdateListener(this);
		mPlayer.setOnPreparedListener(this);
		mPlayer.setOnErrorListener(this);
		mPlayer.setOnCompletionListener(this);
		
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
	
	MediaPlayer getPlayer() {
		return mPlayer;
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		Logging.out(LOG_TAG, "onPrepared", LogLevel.DEBUG);
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
		return true;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (mAdView.getCurrentVideoState() != VideoState.COMPLETE) {
			mHandler.removeCallbacks(mRunnable);
			int duration = mp.getDuration();
			mAdView.setVideoCurrentTime(duration);
			mAdView.setVideoState(VideoState.COMPLETE);
			sendVideoReachEndNotification();
		}
	}
	
	private void sendVideoReachEndNotification() {
		if (LoopMeAdHolder.getAd(mAppKey) != null) {
			LoopMeAdHolder.getAd(mAppKey).onAdVideoDidReachEnd();
		}
	}
	
	void resizeVideo(SurfaceView surfaceView, SurfaceHolder holder, int viewWidth, int viewHeight) {
    	
		if (mPlayer == null) {
			return;
		}
		
		mPlayer.setDisplay(holder);
		
		int videoWidth = mPlayer.getVideoWidth();
	    int videoHeight = mPlayer.getVideoHeight();

	    FrameLayout.LayoutParams lp = (LayoutParams) surfaceView.getLayoutParams();
	    lp.gravity = Gravity.CENTER;

	    int blackLines =0;
	    float percent = 0;
	    
	    if (videoWidth > videoHeight) {
	    	lp.width = viewWidth;
	    	lp.height = (int) (((float)videoHeight / (float)videoWidth) * (float)viewWidth);

	    	blackLines = viewHeight - lp.height;
	    	percent = blackLines * 100 / lp.height; 
	    } else {
	    	lp.height = viewHeight;
	    	lp.width = (int) (((float)videoWidth / (float)videoHeight) * (float)viewHeight);
	    	
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
	    surfaceView.setLayoutParams(lp);
	}

}