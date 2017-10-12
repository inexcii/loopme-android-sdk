package com.loopme.common;

public class StaticParams {

    private StaticParams() {
    }

    public static final long ONE_MINUTE_IN_MILLIS = 1000 * 60;
    public static final long SLEEP_TIME = 1000 * 60 * 15;// 15 min
    public static final int MAX_FAIL_COUNT = 4;
    public static final String CACHED_LOG_FILE_NAME = "events_log.txt";
    public static final boolean APPEND_TO_FILE = true;
    public static final String SDK_VERSION = "5.2.5";
    public static String BASE_URL = "loopme.me/api/loopme/ads/v3";
    public static final String UTF_8 = "UTF-8";
    public static final String UNKNOWN_NAME = "unknown";
    public static String sCacheDirectory = "";
    public static final int DEFAULT_AD_ID = -1;

    public static boolean DEBUG_MODE = true;

    /**
     * AdParams default values
     */
    public static final int DEFAULT_EXPIRED_TIME = 1000 * 60 * 10;//10 minutes
    public static final String ORIENTATION_PORT = "portrait";
    public static final String ORIENTATION_LAND = "landscape";

    public static final String EXTRAS_CUSTOM_CLOSE = "customClose";
    public static final String MRAID_NEED_CLOSE_BUTTON = "com.loopme.MRAID_NEED_CLOSE_BUTTON";
    public static final String DESTROY_INTENT = "com.loopme.DESTROY_INTENT";
    public static final String CLICK_INTENT = "com.loopme.CLICK_INTENT";

    public static long CACHED_VIDEO_LIFE_TIME = 1000 * 60 * 60 * 32;//32 hours

    public static final long FETCH_TIMEOUT = 1000 * 60 * 3;//3 minutes
    public static final int REQUEST_TIMEOUT = 1000 * 20; //20 second

    public static final int SHRINK_MODE_KEEP_AFTER_FINISH_TIME = 1000;

    public static boolean USE_MOBILE_NETWORK_FOR_CACHING = false;

    public static final String BANNER_TAG = "banner";
    public static final String INTERSTITIAL_TAG = "interstitial";

    public static final String APPKEY_TAG = "appkey";
    public static final String FORMAT_TAG = "format";

    public static final String AD_ID_TAG = "adid";

    public static final String LOOPME_PREFERENCES = "loopme";
    public static final String VIEWER_TOKEN = "viewer_token";

    public static boolean PART_PRELOAD = false;
}
