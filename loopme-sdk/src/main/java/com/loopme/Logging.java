package com.loopme;

import android.content.Context;
import android.util.Log;

public class Logging {

	private static Context sContext;
	
	public enum LogLevel {
		INFO, 
		DEBUG, 
		ERROR
	}
	
	private Logging() {}
	
	public static void init(Context context) {
		sContext = context;
	}
	
	public static void out(String tag, String text, LogLevel logLevel) {
   		if (StaticParams.DEBUG_MODE) {
   			Log.d("Debug.LoopMe." + tag, text);
   		} else {
   			if (logLevel == LogLevel.INFO) {
   				Log.d("Debug.LoopMe." + tag, text);
   			}
   		}
    }
}
