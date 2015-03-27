package com.loopme;

import java.util.HashMap;
import java.util.Map;

public class LoopMeAdHolder {
	
	private static final String LOG_TAG = LoopMeAdHolder.class.getSimpleName();

	private static final Map<String, BaseAd> mAdMap = new HashMap<String, BaseAd>();
	
	private LoopMeAdHolder() {
	}
	
	static void putAd(BaseAd ad) {
		String appKey = ad.getAppKey();
		mAdMap.put(appKey, ad);
	}
	
	public static BaseAd getAd(String appKey) {
		return mAdMap.get(appKey);
	}
	
	static void removeAd(String appKey) {
		mAdMap.remove(appKey);
	}
}
