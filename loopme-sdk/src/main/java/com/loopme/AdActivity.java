package com.loopme;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.loopme.Logging.LogLevel;

public final class AdActivity extends Activity implements AdReceiver.Listener {

	private static final String LOG_TAG = AdActivity.class.getSimpleName();
	
	private static final String APPKEY = "appkey";
	
    private LoopMeInterstitial mInterstitial;
    private ViewController mViewController;
    
    private FrameLayout mLayout;

    /**
     * If true - standard activity behaviour when it become inactive.
     * Used to keep activity alive when ad clicked and started AdBrowserActivity.
     * If false - finish activity at any time when it become inactive (incoming call or etc.) 
     */
    private boolean mKeepAlive = true;
    private AdReceiver mReceiver;
    
    private SensorManager mSensorManager;
    
    private float mAccel;
	private float mAccelCurrent;
	private float mAccelLast;
	
	private final SensorEventListener mSensorListener = new SensorEventListener() {

		public void onSensorChanged(SensorEvent se) {
			float x = se.values[0];
			float y = se.values[1];
			float z = se.values[2];
			mAccelLast = mAccelCurrent;
			mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
			float delta = mAccelCurrent - mAccelLast;
			mAccel = mAccel * 0.9f + delta;
			
			if (delta > 5) {
				if (mViewController != null) {
					mViewController.onAdShake();
				}
			}
		}
		
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};
    
    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String appKey = getIntent().getStringExtra(APPKEY);
        if (TextUtils.isEmpty(appKey)) {
        	Logging.out(LOG_TAG, "Empty app key", LogLevel.ERROR);
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        Logging.out(LOG_TAG, "onCreate", LogLevel.DEBUG);
        
        mInterstitial = LoopMeAdHolder.getInterstitial(appKey, null);
        
        if (mInterstitial == null || mInterstitial.getViewController() == null) {
        	Logging.out(LOG_TAG, "No ads with app key " + appKey, LogLevel.ERROR);
			finish();
        } else {
        	mViewController = mInterstitial.getViewController();
        	
        	applyOrientationFromAdParams();
        
        	mLayout = buildLayout();
        	setContentView(mLayout);
        
        	initSensor();
        	initDestroyReceiver();

        	mInterstitial.onLoopMeInterstitialShow(mInterstitial);
        }
    }
    
    private void initSensor() {
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    mAccel = 0.00f;
	    mAccelCurrent = SensorManager.GRAVITY_EARTH;
	    mAccelLast = SensorManager.GRAVITY_EARTH;
	}
    
    private FrameLayout buildLayout() {
		mLayout = new FrameLayout(this);

		if (mViewController != null) {
			if (isVideoPresented()) {
				mViewController.buildVideoAdView(mLayout);
			} else {
				mViewController.buildStaticAdView(mLayout);
			}
		}
		return mLayout;
	}
    
    private boolean isVideoPresented() {
    	VideoController videoController = mViewController.getVideoController(); 
    	return videoController != null
    			&& videoController.isMediaPlayerValid(); 
    }
    
    private void initDestroyReceiver() {
    	IntentFilter filter = new IntentFilter();
    	filter.addAction(StaticParams.DESTROY_INTENT);
    	filter.addAction(StaticParams.CLICK_INTENT);
    	mReceiver = new AdReceiver(this);
    	registerReceiver(mReceiver, filter);
    }
    
    /**
     * Apply orientation from AdParams.
     * Do nothing if orientation parameter absent in AdParams.
     */
    private void applyOrientationFromAdParams() {
    	String or = mInterstitial.getAdParams().getAdOrientation();
    	if (or == null) {
    		return;
    	}
    	
    	if (or.equalsIgnoreCase(StaticParams.ORIENTATION_PORT)) {
    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
    	} else if (or.equalsIgnoreCase(StaticParams.ORIENTATION_LAND)) {
    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    	}
    }
    
    @Override
    protected void onDestroy() {
    	Logging.out(LOG_TAG, "onDestroy", LogLevel.DEBUG);
    	unregisterReceiver(mReceiver);
		if (mLayout != null) {
			mLayout.removeAllViews();
		}
    	if (mInterstitial != null) {
    		mInterstitial.onLoopMeInterstitialHide(mInterstitial);
        }
    	super.onDestroy();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	Logging.out(LOG_TAG, "onPause", LogLevel.DEBUG);
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(mSensorListener);
		}
    	if (!mKeepAlive) {
    		if (mViewController != null) {
                mViewController.setWebViewState(AdView.WebviewState.CLOSED);
    		}
    		finish();
    	}
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	Logging.out(LOG_TAG, "onResume", LogLevel.DEBUG);
        mKeepAlive = false;
        if (mViewController != null) {
            mViewController.setWebViewState(AdView.WebviewState.VISIBLE);
        }
        
        if (mSensorManager != null) {
			mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(
					Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		}
    }
    
    @Override
	public void onBackPressed() {
	}

	@Override
	public void onDestroyBroadcast() {
		finish();
	}

	@Override
	public void onClickBroadcast() {
		mKeepAlive = true;
	}
}