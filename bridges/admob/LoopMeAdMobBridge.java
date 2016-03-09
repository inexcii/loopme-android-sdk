package com.integration.admob;

import android.app.Activity;
import android.util.Log;

import com.google.ads.mediation.MediationAdRequest;
import com.google.ads.mediation.customevent.CustomEventInterstitial;
import com.google.ads.mediation.customevent.CustomEventInterstitialListener;
import com.loopme.common.LoopMeError;
import com.loopme.LoopMeInterstitial;

public class LoopMeAdMobBridge implements CustomEventInterstitial {

    private static final String LOG_TAG = LoopMeAdMobBridge.class.getSimpleName();

    private LoopMeInterstitial mInterstitial;
    private final LoopMeListener mLoopMeListener = new LoopMeListener();

    private CustomEventInterstitialListener mListener;

    @Override
    public void requestInterstitialAd(CustomEventInterstitialListener customEventInterstitialListener, Activity activity, String s, String s2, MediationAdRequest mediationAdRequest, Object o) {

        Log.d(LOG_TAG, "requestInterstitialAd");

        mListener = customEventInterstitialListener;

        mInterstitial = LoopMeInterstitial.getInstance(s2, activity);
        mInterstitial.setListener(mLoopMeListener);
        mInterstitial.load();
    }

    @Override
    public void showInterstitial() {
        Log.d(LOG_TAG, "showInterstitial");
        if (mInterstitial != null) {
            mInterstitial.show();
        }
    }

    @Override
    public void destroy() {
        mInterstitial.destroy();
    }

    private class LoopMeListener implements LoopMeInterstitial.Listener {

        @Override
        public void onLoopMeInterstitialClicked(LoopMeInterstitial arg0) {
        }

        @Override
        public void onLoopMeInterstitialExpired(LoopMeInterstitial arg0) {
        }

        @Override
        public void onLoopMeInterstitialHide(LoopMeInterstitial arg0) {
            mListener.onDismissScreen();
        }

        @Override
        public void onLoopMeInterstitialLeaveApp(LoopMeInterstitial arg0) {
            mListener.onLeaveApplication();
        }

        @Override
        public void onLoopMeInterstitialLoadFail(LoopMeInterstitial arg0,
                                                 LoopMeError arg1) {
            mListener.onFailedToReceiveAd();
        }

        @Override
        public void onLoopMeInterstitialLoadSuccess(LoopMeInterstitial arg0) {
            mListener.onReceivedAd();
        }

        @Override
        public void onLoopMeInterstitialShow(LoopMeInterstitial arg0) {
            mListener.onPresentScreen();
        }

        @Override
        public void onLoopMeInterstitialVideoDidReachEnd(
                LoopMeInterstitial interstitial) {
            // TODO Auto-generated method stub
        }
    }
}
