package com.loopme;

import android.content.Intent;

import com.loopme.common.Logging;
import com.loopme.common.StaticParams;

public class AdUtils {

    private static final String LOG_TAG = AdUtils.class.getSimpleName();

    public static void startAdActivity(BaseAd ad) {
        if (ad != null) {
            Logging.out(LOG_TAG, "Starting Ad Activity");
            LoopMeAdHolder.putAd(ad);
            Intent intent = new Intent(ad.getContext(), AdActivity.class);
            intent.putExtra(StaticParams.APPKEY_TAG, ad.getAppKey());
            intent.putExtra(StaticParams.FORMAT_TAG, ad.getAdFormat());
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ad.getContext().startActivity(intent);
        }
    }
}
