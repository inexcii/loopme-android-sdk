package com.mopub.nativeads;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.Map;

public class LoopMeEventNative extends CustomEventNative {

    private static final String TAG = LoopMeEventNative.class.getSimpleName();

    private static final String APP_KEY = "app_key";
    private static final String POSITION = "position";

    private static LoopMeEventNative.Listener sListener;

    public interface Listener {
        void onNativeAdFailed(String appKey, int position);
    }

    public static void addListener(LoopMeEventNative.Listener listener) {
        Log.d(TAG, "addListener");
        sListener = listener;
    }

    @Override
    protected void loadNativeAd(Context context, CustomEventNativeListener customEventNativeListener, Map<String, Object> map, Map<String, String> map1) {
        Log.d(TAG, "loadNativeAd");

        String appKey = map1.get(APP_KEY);
        String positionStr = map1.get(POSITION);

        int position = 0;
        if (!TextUtils.isEmpty(positionStr)) {
            try {
                position = Integer.valueOf(positionStr);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        sListener.onNativeAdFailed(appKey, position);
    }

}
