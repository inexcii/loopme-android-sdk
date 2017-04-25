package com.loopme.common;

import java.util.Locale;

public class StaticParams {

    private StaticParams() {}

    /**
     *      Do not remove toString().
     *      It was made for disabling javac's inlining of static final variables
     */

    private static final String VERSION = "5.1.6".toString();
    public static final String SDK_VERSION =  String.format(Locale.ENGLISH, "%s", VERSION);

    public static String BASE_URL = "loopme.me/api/loopme/ads/v3";

    public static boolean DEBUG_MODE = true;

    /**
     * AdParams default values
     */
    public static final int DEFAULT_EXPIRED_TIME = 1000 * 60 * 10;//10 minutes
    public static final String ORIENTATION_PORT = "portrait";
    public static final String ORIENTATION_LAND = "landscape";

    public static final String DESTROY_INTENT = "com.loopme.DESTROY_INTENT";
    public static final String CLICK_INTENT = "com.loopme.CLICK_INTENT";

    public static long CACHED_VIDEO_LIFE_TIME = 1000 * 60 * 60 * 32;//32 hours

    public static final long FETCH_TIMEOUT = 1000 * 60 * 3;//3 minutes

    public static final int SHRINK_MODE_KEEP_AFTER_FINISH_TIME = 1000;

    public static final boolean USE_MOBILE_NETWORK_FOR_CACHING = true;

    public static final String BANNER_TAG = "banner";
    public static final String INTERSTITIAL_TAG = "interstitial";

    public static final String APPKEY_TAG = "appkey";
    public static final String FORMAT_TAG = "format";

    public static final String LOOPME_PREFERENCES = "loopme";
    public static final String VIEWER_TOKEN = "viewer_token";

    public static boolean PART_PRELOAD = false;
}
