package com.loopme;

public class StaticParams {
	
	private StaticParams() {}
	
	public static final String SDK_VERSION = "4.2.1";
	public static String BASE_URL = "loopme.me/api/loopme/ads/v3";
	
	static boolean DEBUG_MODE = true;
	
	/**
	 * AdParams default values
	 */
	static final int DEFAULT_EXPIRED_TIME = 1000 * 60 * 10;//10 minutes
	static final String ORIENTATION_PORT = "portrait";
	static final String ORIENTATION_LAND = "landscape";
	
	static final String DESTROY_INTENT = "com.loopme.DESTROY_INTENT";
	static final String CLICK_INTENT = "com.loopme.CLICK_INTENT";
	
	public static long CACHED_VIDEO_LIFE_TIME = 1000 * 60 * 60 * 8;//8 hours
	
	static final long FETCH_TIMEOUT = 1000 * 60 * 3;//3 minutes
	
	static final int SHRINK_MODE_KEEP_AFTER_FINISH_TIME = 1000;
}
