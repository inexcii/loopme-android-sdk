package com.loopme;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class Logging {

	private static Context sContext;
	
	public enum LogLevel {
		INFO, 
		DEBUG, 
		ERROR;
	}
	
	private Logging() {}
	
	public static void init(Context context) {
		sContext = context;
	}
	
	public static void out(String tag, String text, LogLevel logLevel) {
   		if (StaticParams.DEBUG_MODE) {
   			Log.d("Debug.LoopMe." + tag, text);
    		sendBroadcast(tag, text, logLevel);
   		} else {
   			if (logLevel == LogLevel.INFO) {
   				Log.d("Debug.LoopMe." + tag, text);
   			}
   		}
    }
    
    private static void sendBroadcast(String tag, String text, LogLevel level) {
    	if (sContext == null) {
    		return;
    	}
    	Intent intent = new Intent("com.loopme.logLevel");
		intent.putExtra("tag", tag);
		intent.putExtra("log", text);
		intent.putExtra("logLevel", level.toString());
		LocalBroadcastManager.getInstance(sContext).sendBroadcast(intent);
    }
}
