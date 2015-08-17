package com.loopme;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.media.AudioManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import com.loopme.Logging.LogLevel;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utils {
	
	private static final String LOG_TAG = Utils.class.getSimpleName();
	
	private static Context sContext;
	
	public static boolean isOnline(Context context) {
		boolean isOnline = false;
		try {
			final ConnectivityManager conMgr = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			
			if (conMgr == null) {
				return false;
			}
			
			final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
			
			return activeNetwork != null && activeNetwork.isConnected() 
					&& activeNetwork.isAvailable();
		} catch(Exception e) {
			e.printStackTrace();
			isOnline = false;
		}
		return isOnline;
	}

    public static int convertDpToPixel(float dp){
    	return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, 
    			sContext.getResources().getDisplayMetrics());
    }
    
    static void init(Context context) {
    	sContext = context;
    }
    
    public static String getStringFromStream(InputStream inputStream) {
        int numberBytesRead = 0;
        StringBuilder out = new StringBuilder();
        byte[] bytes = new byte[4096];

        try {
            while ((numberBytesRead = inputStream.read(bytes)) != -1) {
                out.append(new String(bytes, 0, numberBytesRead));
            }
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toString();
    }
    
    public static Location getLastKnownLocation() {
        Location result = null;

        LocationManager locationManager = (LocationManager) sContext.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
        	return null;
        }
        Location gpsLocation = null;
        try {
            gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
        	Logging.out(LOG_TAG, "Failed to retrieve GPS location: access appears to be disabled.", LogLevel.DEBUG);
        } catch (IllegalArgumentException e) {
        	Logging.out(LOG_TAG, "Failed to retrieve GPS location: device has no GPS provider.", LogLevel.DEBUG);
        }

        Location networkLocation = null;
        try {
            networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException e) {
        	Logging.out(LOG_TAG, "Failed to retrieve network location: access appears to be disabled.", LogLevel.DEBUG);
        } catch (IllegalArgumentException e) {
        	Logging.out(LOG_TAG, "Failed to retrieve network location: device has no network provider.", LogLevel.DEBUG);
        }

        if (gpsLocation == null && networkLocation == null) {
            return null;
        } 
        
        if (gpsLocation != null && networkLocation != null) {
            if (gpsLocation.getTime() > networkLocation.getTime()) {
                result = gpsLocation;
            } else {
                result = networkLocation;
            }
        } else if (gpsLocation != null) {
            result = gpsLocation;
        } else {
            result = networkLocation;
        }

        return result;
    }
    
    public static DisplayMetrics getDisplayMetrics(Context context) {
    	DisplayMetrics displayMetrics = new DisplayMetrics();
    	WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    	if (windowManager == null) {
    		return displayMetrics;
    	}
    	windowManager.getDefaultDisplay().getMetrics(displayMetrics);
    	return displayMetrics;
    }
    
    public static boolean isPackageInstalled(List<String> packadeId) {
    	if (sContext == null) {
    		return false;
    	}
    	PackageManager pm = sContext.getPackageManager();
		List<PackageInfo> packages = pm.getInstalledPackages(0);
		
		for (PackageInfo packageInfo : packages) {
			for (int i = 0; i < packadeId.size(); i++) {
				if (packadeId.get(i).equalsIgnoreCase(packageInfo.packageName)) {
					return true;
				}
			}
		}
		return false;
    }
    
    public static void animateAppear(View view) {
        view.animate()
		    .setDuration(500)
		    .alpha(1.0f);
    }

    public static float getSystemVolume() {
        if (sContext == null) {
            return 1.0f;
        }
        AudioManager am= (AudioManager) sContext.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            int volume_level = am.getStreamVolume(AudioManager.STREAM_RING);
            int max = am.getStreamMaxVolume(AudioManager.STREAM_RING);
            int percent = Math.round(volume_level * 100 / max);
            return (float) percent / 100;
        } else {
            return 1.0f;
        }
    }
}
