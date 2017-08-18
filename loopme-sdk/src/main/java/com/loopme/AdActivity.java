package com.loopme;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.loopme.common.Logging;
import com.loopme.common.StaticParams;
import com.loopme.common.Utils;
import com.loopme.constants.AdFormat;
import com.loopme.constants.WebviewState;

public final class AdActivity extends Activity
        implements AdReceiver.Listener {

    private static final String LOG_TAG = AdActivity.class.getSimpleName();

    private AdController mAdController;
    private IViewController mIViewController;
    private int mFormat;
    private boolean mIs360;

    private BaseAd mBaseAd;

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

    private int mInitialOrientation;

    private boolean mReceivedDestroyBroadcast;
    private boolean mFirstLaunchHtmlAd = true;

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
                if (mAdController != null) {
                    mAdController.onAdShake();
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mInitialOrientation = Utils.getScreenOrientation();

        String appKey = getIntent().getStringExtra(StaticParams.APPKEY_TAG);
        if (TextUtils.isEmpty(appKey)) {
            Logging.out(LOG_TAG, "Empty app key");
        }
        mFormat = getIntent().getIntExtra(StaticParams.FORMAT_TAG, 0);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        Logging.out(LOG_TAG, "onCreate");

        if (mFormat == AdFormat.INTERSTITIAL) {
            mBaseAd = LoopMeAdHolder.getInterstitial(appKey, null);

        } else if (mFormat == AdFormat.BANNER) {
            mBaseAd = LoopMeAdHolder.getBanner(appKey, null);
        }

        if (mBaseAd == null || mBaseAd.getAdController() == null) {
            Logging.out(LOG_TAG, "No ads with app key " + appKey);
            finish();
        } else {
            mIs360 = mBaseAd.getAdParams().isVideo360();
            mAdController = mBaseAd.getAdController();
            mAdController.setActivity(AdActivity.this);

            mIViewController = mAdController.getViewController();

            if (mFormat == AdFormat.INTERSTITIAL) {
                if (!mIs360) {
                    applyOrientationFromAdParams();
                }
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }

            mLayout = buildLayout();
            setContentView(mLayout);

            initSensor();
            initDestroyReceiver();

            if (mFormat == AdFormat.INTERSTITIAL) {
                if (mIs360) {
                    mIViewController.initVRLibrary(this);
                }
                ((LoopMeInterstitial) mBaseAd).onLoopMeInterstitialShow((LoopMeInterstitial) mBaseAd);
            }
            startMoatWebAdTacking();
            setMoatWebAdTrackerActivity();
        }
    }

    private void startMoatWebAdTacking() {
        if (mAdController != null) {
            mAdController.startMoatWebAdTacking();
        }
    }

    private void stopMoatWebAdTacking() {
        if (mAdController != null) {
            mAdController.stopMoatWebAdTacking();
        }
    }

    private void setMoatWebAdTrackerActivity() {
        if (mBaseAd != null && mBaseAd.isHtmlAd()) {
            mAdController.setMoatWebAdTrackerActivity(AdActivity.this);
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

        if (mFormat == AdFormat.INTERSTITIAL) {
            if (mAdController != null) {
                if (isVideoPresented()) {
                    mAdController.buildVideoAdView(mLayout);
                } else {
                    mAdController.buildStaticAdView(mLayout);
                }
            }
        } else {
            LoopMeBannerView banner = new LoopMeBannerView(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            mAdController.rebuildView(banner);
            mLayout.addView(banner, params);
        }

        return mLayout;
    }

    public boolean isVideoPresented() {
        return mAdController.isVideoPresented();
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
        String or = mBaseAd.getAdParams().getAdOrientation();
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
        Logging.out(LOG_TAG, "onDestroy");
        stopMoatWebAdTacking();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        if (mAdController != null && mBaseAd.getAdFormat() == AdFormat.INTERSTITIAL) {
            mAdController.setWebViewState(WebviewState.CLOSED);
        }
        if (mLayout != null) {
            mLayout.removeAllViews();
        }
        if (mBaseAd != null && mFormat == AdFormat.INTERSTITIAL) {
            if (mIs360) {
                mIViewController.onDestroy();
            }
            ((LoopMeInterstitial) mBaseAd).onLoopMeInterstitialHide((LoopMeInterstitial) mBaseAd);
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logging.out(LOG_TAG, "onPause");
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mSensorListener);
        }
        if (mIViewController != null) {
            mIViewController.onPause();
        }

        if (mFormat == AdFormat.BANNER) {
            if (!mReceivedDestroyBroadcast && mAdController != null) {
                mAdController.setWebViewState(WebviewState.HIDDEN);
            }

        } else if (!mKeepAlive && mFormat == AdFormat.INTERSTITIAL) {
            if (mAdController != null) {
                mAdController.setWebViewState(WebviewState.HIDDEN);
                mAdController.pauseVideo();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logging.out(LOG_TAG, "onResume");
        mKeepAlive = false;
        if (mAdController != null) {
            if (mAdController.getWebViewState() == WebviewState.HIDDEN) {
                mAdController.resumeVideo();
            }
            mAdController.setWebViewState(WebviewState.VISIBLE);

            if (mIViewController != null) {
                mIViewController.onResume();
            }
        }
        if (mSensorManager != null) {
            mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(
                    Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        }
        mFirstLaunchHtmlAd = false;
    }

    @Override
    public void onBackPressed() {
        if (mFormat == AdFormat.BANNER) {
            mAdController.switchToPreviousMode();
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroyBroadcast() {
        Logging.out(LOG_TAG, "onDestroyBroadcast");
        mReceivedDestroyBroadcast = true;
        if (mFormat == AdFormat.BANNER) {
            setRequestedOrientation(mInitialOrientation);
            mAdController.switchToPreviousMode();
        }
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        finish();
    }

    @Override
    public void onClickBroadcast() {
        mKeepAlive = true;
    }
}