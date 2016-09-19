package com.integration.admob;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitial;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;
import com.google.android.gms.ads.mediation.MediationAdRequest;

import android.app.Activity;
import android.util.Log;
import android.content.Context;
import android.os.Bundle;

import com.loopme.LoopMeInterstitial;
import com.loopme.common.LoopMeError;

public class LoopMeAdMobBridge implements CustomEventInterstitial {

    private static final String LOG_TAG = LoopMeAdMobBridge.class.getSimpleName();

    private LoopMeInterstitial mInterstitial;
    private final LoopMeListener mLoopMeListener = new LoopMeListener();
    private CustomEventInterstitialListener mListener;

    @Override
    public void requestInterstitialAd(Context activity,
                                      CustomEventInterstitialListener customEventInterstitialListener,
                                      String s,
                                      MediationAdRequest mediationAdRequest,
                                      Bundle o) {

        Log.d(LOG_TAG, "requestInterstitialAd");

        mListener = customEventInterstitialListener;

        mInterstitial = LoopMeInterstitial.getInstance(s, activity);
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
    public void onResume() {
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        mInterstitial.destroy();
    }

    @Override
    public void onPause() {
    }

    private class LoopMeListener implements LoopMeInterstitial.Listener {

        @Override
        public void onLoopMeInterstitialClicked(LoopMeInterstitial arg0) {
            mListener.onAdClicked();
        }

        @Override
        public void onLoopMeInterstitialExpired(LoopMeInterstitial arg0) {
        }

        @Override
        public void onLoopMeInterstitialHide(LoopMeInterstitial arg0) {
            mListener.onAdClosed();
        }

        @Override
        public void onLoopMeInterstitialLeaveApp(LoopMeInterstitial arg0) {
        }

        @Override
        public void onLoopMeInterstitialLoadFail(LoopMeInterstitial arg0,
                                                 LoopMeError arg1) {
            mListener.onAdFailedToLoad(0);
        }

        @Override
        public void onLoopMeInterstitialLoadSuccess(LoopMeInterstitial arg0) {
            mListener.onAdLoaded();
        }

        @Override
        public void onLoopMeInterstitialShow(LoopMeInterstitial arg0) {
            mListener.onAdOpened();
        }

        @Override
        public void onLoopMeInterstitialVideoDidReachEnd(LoopMeInterstitial interstitial) {
        }
    }
}
