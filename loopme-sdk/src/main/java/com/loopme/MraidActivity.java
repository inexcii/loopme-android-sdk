package com.loopme;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.loopme.common.Utils;
import com.loopme.ui.view.CloseButton;
import com.loopme.common.Logging;
import com.loopme.common.StaticParams;
import com.loopme.constants.AdFormat;
import com.loopme.constants.WebviewState;
import com.loopme.mraid.MraidView;

import static com.loopme.common.StaticParams.EXTRAS_CUSTOM_CLOSE;

public class MraidActivity extends Activity implements AdReceiver.Listener,
        MraidAdCloseButtonReceiver.MraidAdCloseButtonListener {

    private static final String LOG_TAG = MraidActivity.class.getSimpleName();

    private boolean mHasOwnCloseButton;

    private BaseAd mBaseAd;
    private int mFormat;
    private AdController mAdController;
    private RelativeLayout mLayout;
    private MraidView mMraidView;

    private AdReceiver mReceiver;
    private MraidAdCloseButtonReceiver mCloseButtonReceiver;
    private boolean mReceivedDestroyBroadcast;
    private CloseButton mCloseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        mHasOwnCloseButton = getIntent().getBooleanExtra(EXTRAS_CUSTOM_CLOSE, false);
        mFormat = getIntent().getIntExtra(StaticParams.FORMAT_TAG, 0);
        mBaseAd = LoopMeAdHolder.getAd(getIntent(), mFormat);
        applyOrientation();

        if (mBaseAd == null) {
            finish();
        }
        if (mBaseAd != null) {
            mAdController = mBaseAd.getAdController();
        }

        mLayout = buildLayout();
        if(mLayout == null){
            return;
        }
        setContentView(mLayout);

        initCloseButton();
        initDestroyReceiver();
        initMraidAdCloseButtonReceiver();
        onInterstitialShowCallback();
    }

    private void applyOrientation() {
        int orientation = mBaseAd.getAdController().getMraidOrientation();
        setRequestedOrientation(orientation);
    }

    private void onInterstitialShowCallback() {
        if (mBaseAd instanceof LoopMeInterstitialGeneral) {
            LoopMeInterstitialGeneral interstitial = (LoopMeInterstitialGeneral) mBaseAd;
            interstitial.onLoopMeInterstitialShow(interstitial);
        }
    }

    private void initMraidAdCloseButtonReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(StaticParams.MRAID_NEED_CLOSE_BUTTON);
        mCloseButtonReceiver = new MraidAdCloseButtonReceiver(this);
        registerReceiver(mCloseButtonReceiver, intentFilter);
    }

    private void initDestroyReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(StaticParams.DESTROY_INTENT);
//        filter.addAction(StaticParams.CLICK_INTENT);
        mReceiver = new AdReceiver(this);
        registerReceiver(mReceiver, filter);
    }

    private RelativeLayout buildLayout() {
        mLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        if (mAdController != null) {
            mMraidView = mAdController.getMraidView();
        } else {
            Logging.out(LOG_TAG, "mAdController is null");
        }

        if (mMraidView != null) {
            Utils.removeParent(mMraidView);
            mLayout.addView(mMraidView, params);
            return mLayout;
        } else {
            return null;
        }
    }

    private void initCloseButton() {
        mCloseButton = new CloseButton(this);
        mCloseButton.addInLayout(mLayout);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFormat == AdFormat.INTERSTITIAL) {
                    mAdController.getMraidController().close();
                } else {
                    collapseBanner();
                }
            }
        });
        if (mHasOwnCloseButton) {
            mCloseButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        Logging.out(LOG_TAG, "onDestroy");
        if (mAdController != null && mFormat == AdFormat.INTERSTITIAL) {
            mAdController.setMraidWebViewState(WebviewState.CLOSED);
        }
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        if (mCloseButtonReceiver != null) {
            unregisterReceiver(mCloseButtonReceiver);
        }
        if (mLayout != null) {
            mLayout.removeAllViews();
        }
        if (mFormat == AdFormat.INTERSTITIAL) {
            ((LoopMeInterstitialGeneral) mBaseAd).onLoopMeInterstitialHide(
                    (LoopMeInterstitialGeneral) mBaseAd);
        } else {
            //todo change state
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMraidView.notifySizeChangeEvent(400, 600);
        mMraidView.setIsViewable(true);
        if (mAdController != null) {
            mAdController.setMraidWebViewState(WebviewState.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMraidView.setIsViewable(false);
        if (!mReceivedDestroyBroadcast && mAdController != null) {
            mAdController.setMraidWebViewState(WebviewState.HIDDEN);
        }
    }

    @Override
    public void onDestroyBroadcast(int adIdToClose) {
        if (mBaseAd.getAdId() != adIdToClose) {
            return;
        }
        Logging.out(LOG_TAG, "onDestroyBroadcast");
        mReceivedDestroyBroadcast = true;
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        finish();
    }

    @Override
    public void onClickBroadcast() {
        Logging.out(LOG_TAG, "onClickBroadcast() in Mraid Activity is not implemented");
    }

    @Override
    public void onCloseButtonVisibilityChanged(boolean customCloseButton) {
        if (customCloseButton) {
            mCloseButton.setVisibility(View.GONE);
        } else {
            mCloseButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (mFormat == AdFormat.BANNER) {
            collapseBanner();
        }
        super.onBackPressed();
    }

    private void collapseBanner() {
        if (mAdController != null) {
            mAdController.collapseMraidBanner();
        }
    }
}
