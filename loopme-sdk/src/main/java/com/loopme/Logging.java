package com.loopme;

import android.util.Log;

import com.loopme.debugging.DebugController;

public class Logging {

    private static final String PREFIX = "Debug.LoopMe.";

    public enum LogLevel {
        INFO,
        DEBUG,
        ERROR
    }

    private Logging() {}

    public static void out(String tag, final String text, LogLevel logLevel) {
        final String logTag = new StringBuilder(PREFIX).append(tag).toString();

        if (StaticParams.DEBUG_MODE) {
            Log.i(logTag, text);
        } else {
            if (logLevel == LogLevel.INFO) {
                Log.i(logTag, text);
            }
        }
        DebugController.handle(logTag, text);
    }
}
