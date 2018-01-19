package com.loopme;

import android.content.Intent;

import com.loopme.common.Logging;
import com.loopme.common.StaticParams;
import com.loopme.common.Utils;

import static com.loopme.common.StaticParams.EXTRAS_CUSTOM_CLOSE;

public class AdUtils {

    private static final String LOG_TAG = AdUtils.class.getSimpleName();

    public static void startAdActivity(BaseAd ad) {
        if (ad != null) {
            Logging.out(LOG_TAG, "Starting Ad Activity");
            LoopMeAdHolder.putAd(ad);
            Intent intent = new Intent(ad.getContext(), AdActivity.class);
            Utils.setAdIdOrAppKey(intent, ad);
            intent.putExtra(StaticParams.FORMAT_TAG, ad.getAdFormat());
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ad.getContext().startActivity(intent);
        }
    }

    public static void startMraidActivity(BaseAd ad, boolean customClose) {
        if (ad != null) {
            Logging.out(LOG_TAG, "Starting Mraid Activity");
            LoopMeAdHolder.putAd(ad);
            Intent intent = new Intent(ad.getContext(), MraidActivity.class);

            Utils.setAdIdOrAppKey(intent, ad);
            intent.putExtra(StaticParams.FORMAT_TAG, ad.getAdFormat());
            intent.putExtra(EXTRAS_CUSTOM_CLOSE, customClose);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ad.getContext().startActivity(intent);
        }
    }
}
