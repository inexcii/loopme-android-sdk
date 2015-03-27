package com.loopme;

import java.util.Locale;

import com.loopme.Logging.LogLevel;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.location.Location;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class AdRequestParametersProvider {
	
	private static final String LOG_TAG = AdRequestParametersProvider.class.getSimpleName();
	
	private static AdRequestParametersProvider sProvider;
	private Context mContext;
	
    private volatile String mAdvertisingId;
    private String mLoopMeId;

	private String mAppVersion;
	private String mMraid;
	private boolean mDntPresent;
	private String mCarrier;
	private boolean mCarrierInited;
	
	private AdRequestParametersProvider(Context context) throws NullPointerException {
		if (context == null) {
			Logging.out(LOG_TAG, "Context should not be null", LogLevel.ERROR);
			throw new NullPointerException();
		}
		mContext = context;
	}

	public static AdRequestParametersProvider getInstance(Context context) {
		if (sProvider == null) {
			sProvider = new AdRequestParametersProvider(context);
		} 
		return sProvider; 
	}
	
	void reset() {
		sProvider = null;
	}
	
	void setGoogleAdvertisingId(String advId) {
		Logging.out(LOG_TAG, "Advertising Id = " + advId, LogLevel.DEBUG);
    	mAdvertisingId = advId;
    }
	
	String getGoogleAdvertisingId() {
		return mAdvertisingId;
	}
	
	public int getConnectionType() {
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
			return ConnectionType.UNKNOWN;
		}
		int type = cm.getActiveNetworkInfo().getType();
		if (type == ConnectivityManager.TYPE_WIFI) {
			return ConnectionType.WIFI;
		} else if (type == ConnectivityManager.TYPE_ETHERNET) {
			return ConnectionType.ETHERNET;
		} else if (type == ConnectivityManager.TYPE_MOBILE) {

			TelephonyManager mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
			int networkType = mTelephonyManager.getNetworkType();
			switch (networkType) {
			case TelephonyManager.NETWORK_TYPE_GPRS:
			case TelephonyManager.NETWORK_TYPE_EDGE:
			case TelephonyManager.NETWORK_TYPE_CDMA:
			case TelephonyManager.NETWORK_TYPE_1xRTT:
			case TelephonyManager.NETWORK_TYPE_IDEN:
				return ConnectionType.MOBILE_2G;

			case TelephonyManager.NETWORK_TYPE_UMTS:
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
			case TelephonyManager.NETWORK_TYPE_HSDPA:
			case TelephonyManager.NETWORK_TYPE_HSUPA:
			case TelephonyManager.NETWORK_TYPE_HSPA:
			case TelephonyManager.NETWORK_TYPE_EVDO_B:
			case TelephonyManager.NETWORK_TYPE_EHRPD:
			case TelephonyManager.NETWORK_TYPE_HSPAP:
				return ConnectionType.MOBILE_3G;

			case TelephonyManager.NETWORK_TYPE_LTE:
				return ConnectionType.MOBILE_4G;

			default:
				return ConnectionType.MOBILE_UNKNOWN_GENERATION;
			}
		} else {
			return ConnectionType.UNKNOWN;
		}
	}
	
	public String getNetworkClass(Context context) {
	    TelephonyManager mTelephonyManager = (TelephonyManager)
	            context.getSystemService(Context.TELEPHONY_SERVICE);
	    int networkType = mTelephonyManager.getNetworkType();
	    switch (networkType) {
	        case TelephonyManager.NETWORK_TYPE_GPRS:
	        case TelephonyManager.NETWORK_TYPE_EDGE:
	        case TelephonyManager.NETWORK_TYPE_CDMA:
	        case TelephonyManager.NETWORK_TYPE_1xRTT:
	        case TelephonyManager.NETWORK_TYPE_IDEN:
	            return "2G";
	        case TelephonyManager.NETWORK_TYPE_UMTS:
	        case TelephonyManager.NETWORK_TYPE_EVDO_0:
	        case TelephonyManager.NETWORK_TYPE_EVDO_A:
	        case TelephonyManager.NETWORK_TYPE_HSDPA:
	        case TelephonyManager.NETWORK_TYPE_HSUPA:
	        case TelephonyManager.NETWORK_TYPE_HSPA:
	        case TelephonyManager.NETWORK_TYPE_EVDO_B:
	        case TelephonyManager.NETWORK_TYPE_EHRPD:
	        case TelephonyManager.NETWORK_TYPE_HSPAP:
	            return "3G";
	        case TelephonyManager.NETWORK_TYPE_LTE:
	            return "4G";
	        default:
	            return "Unknown";
	    }
	}
	
	public String getLanguage() {
		return Locale.getDefault().getLanguage();
	}
	
	public String getAppVersion() {
		if (mAppVersion == null) {
			initAppVersion();
		}
		return mAppVersion;
	}
	
	private void initAppVersion() {
		try {
			mAppVersion = mContext.getPackageManager()
				    .getPackageInfo(mContext.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Logging.out(LOG_TAG, "Can't get app version. Exception: " + e.getMessage(), 
					LogLevel.ERROR);
			mAppVersion = "0.0";
		}
	}
	
	public String getMraidSupport() {
		if (mMraid == null) {
			initMraidSupport();
		}
		return mMraid;
	}
	
	private void initMraidSupport() {
        mMraid = "1";
        try {
            Class.forName("com.loopme.MraidView");
        } catch (ClassNotFoundException e) {
        	mMraid = "0";
        }
    }
	
	public String getOrientation() {
		int orientation = mContext.getResources().getConfiguration().orientation; 
	    if (Configuration.ORIENTATION_LANDSCAPE == orientation) { 
	    	return "l";
	    } else { 
	    	return "p";
	    } 
	}
	
	public String getViewerToken() {
		String advId = mAdvertisingId;
		if (TextUtils.isEmpty(advId)) {
			mDntPresent = true;
			if (mLoopMeId == null) {
				String loopmeId = Long.toHexString(Double.doubleToLongBits(Math.random()));
				Logging.out(LOG_TAG, "LoopMe Id = " + loopmeId, LogLevel.DEBUG);
				mLoopMeId = loopmeId;
			} 
			return mLoopMeId;
		} else {
			return advId;
		}
	}
	
	public String getLatitude() {
		Location location = Utils.getLastKnownLocation();
		if (location != null) {
			return String.valueOf(location.getLatitude());
		} else {
			return null;
		}
	}
	
	public String getLongitude() {
		Location location = Utils.getLastKnownLocation();
		if (location != null) {
			return String.valueOf(location.getLongitude());
		} else {
			return null;
		}
	}
	
	public String getCarrier() {
		if (!mCarrierInited) {
			initCarrier();
		}
		return mCarrier;
	}
	
	private void initCarrier() {
		TelephonyManager telephonyManager =
                (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

		mCarrier = telephonyManager.getNetworkOperator();
		if (mCarrier.isEmpty()) {
			mCarrier = null;
		}
		mCarrierInited = true;
	}
	
	public boolean isDntPresent() {
		return mDntPresent;
	}
}
