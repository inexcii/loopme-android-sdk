package com.loopme.common;

public class StaticParams {

    private StaticParams() {}

    public static final String SDK_VERSION = "4.7.0";
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

    public static final long BUFFERING_TIMEOUT = 1000 * 7;//7 seconds

    public static final int SHRINK_MODE_KEEP_AFTER_FINISH_TIME = 1000;

    public static boolean USE_MOBILE_NETWORK_FOR_CACHING = false;

    public static final String BANNER_TAG = "banner";
    public static final String INTERSTITIAL_TAG = "interstitial";

    public static final String APPKEY_TAG = "appkey";
    public static final String FORMAT_TAG = "format";

    /**
     * Buffering level for play video, when used part preload.
     */
    public static final int BUFFERING_LEVEL = 25;

    public static final String LOOPME_PREFERENCES = "loopme";
    public static final String VIEWER_TOKEN = "viewer_token";
}
