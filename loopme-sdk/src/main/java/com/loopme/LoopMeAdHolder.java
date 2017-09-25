package com.loopme;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import com.loopme.common.StaticParams;
import com.loopme.constants.AdFormat;

import java.util.HashMap;
import java.util.Map;

public class LoopMeAdHolder {

    private static final Map<Integer, LoopMeInterstitialGeneral> mNewImplInterstitialMap = new HashMap<Integer, LoopMeInterstitialGeneral>();
    private static final Map<Integer, LoopMeBannerGeneral> mNewImplBannerMap = new HashMap<Integer, LoopMeBannerGeneral>();

    private LoopMeAdHolder() {
    }

    static void putAd(BaseAd baseAd) {
        int id = baseAd.getAdId();
        if (baseAd.getAdFormat() == AdFormat.INTERSTITIAL) {
            mNewImplInterstitialMap.put(id, (LoopMeInterstitialGeneral) baseAd);
        } else {
            mNewImplBannerMap.put(id, (LoopMeBannerGeneral) baseAd);
        }
    }

    public static LoopMeInterstitialGeneral createInterstitial(String appKey, Activity activity) {
        if (activity == null || TextUtils.isEmpty(appKey)) {
            return null;
        } else {
            LoopMeInterstitialGeneral interstitial = new LoopMeInterstitialGeneral(activity, appKey);
            mNewImplInterstitialMap.put(interstitial.getAdId(), interstitial);
            return interstitial;
        }
    }

    private static LoopMeInterstitialGeneral findInterstitial(int adId) {
        if (mNewImplInterstitialMap.containsKey(adId)) {
            return mNewImplInterstitialMap.get(adId);
        } else {
            return null;
        }
    }

    public static LoopMeBannerGeneral createBanner(String appKey, Activity activity) {
        if (activity == null || TextUtils.isEmpty(appKey)) {
            return null;
        } else {
            LoopMeBannerGeneral banner = new LoopMeBannerGeneral(activity, appKey);
            mNewImplBannerMap.put(banner.getAdId(), banner);
            return banner;
        }
    }

    private static LoopMeBannerGeneral findBanner(int adId) {
        if (mNewImplBannerMap.containsKey(adId)) {
            return mNewImplBannerMap.get(adId);
        } else {
            return null;
        }
    }

    public static void removeAd(BaseAd baseAd) {
        if (baseAd != null) {
            mNewImplInterstitialMap.remove(baseAd.getAdId());
            mNewImplBannerMap.remove(baseAd.getAdId());
        }
    }

    public static BaseAd getAd(Intent intent, int format) {
        if (intent == null) {
            return null;
        }
        int adId = intent.getIntExtra(StaticParams.AD_ID_TAG, StaticParams.DEFAULT_AD_ID);
        if (format == AdFormat.BANNER) {
            return LoopMeAdHolder.findBanner(adId);
        } else {
            return LoopMeAdHolder.findInterstitial(adId);
        }
    }
}
