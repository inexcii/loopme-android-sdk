package com.loopme;

import android.app.Activity;
import android.text.TextUtils;

import com.loopme.constants.AdFormat;

import java.util.HashMap;
import java.util.Map;

public class LoopMeAdHolder {

    private static final Map<String, LoopMeInterstitial> mInterstitialMap =
            new HashMap<String, LoopMeInterstitial>();

    private static final Map<String, LoopMeBanner> mBannerMap =
            new HashMap<String, LoopMeBanner>();

    private LoopMeAdHolder() {
    }

    static void putAd(BaseAd ad) {
        String appKey = ad.getAppKey();

        if (ad.getAdFormat() == AdFormat.INTERSTITIAL) {
            mInterstitialMap.put(appKey, (LoopMeInterstitial) ad);
        } else {
            mBannerMap.put(appKey, (LoopMeBanner) ad);
        }
    }

    public static LoopMeInterstitial getInterstitial(String appKey, Activity activity) {
        if (mInterstitialMap.containsKey(appKey)) {
            return mInterstitialMap.get(appKey);
        } else {
            if (activity == null || TextUtils.isEmpty(appKey)) {
                return null;
            } else {
                LoopMeInterstitial interstitial = new LoopMeInterstitial(activity, appKey);
                mInterstitialMap.put(appKey, interstitial);
                return interstitial;
            }
        }
    }

    public static LoopMeBanner getBanner(String appKey, Activity activity) {
        if (mBannerMap.containsKey(appKey)) {
            return mBannerMap.get(appKey);
        } else {
            if (activity == null || TextUtils.isEmpty(appKey)) {
                return null;
            } else {
                LoopMeBanner banner = new LoopMeBanner(activity, appKey);
                mBannerMap.put(appKey, banner);
                return banner;
            }
        }
    }

    static void removeInterstitial(String appKey) {
        mInterstitialMap.remove(appKey);
    }

    static void removeBanner(String appKey) {
        mBannerMap.remove(appKey);
    }
}
