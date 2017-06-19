package com.loopme.common;

import android.text.TextUtils;
import android.util.Log;

import com.loopme.debugging.LiveDebug;

import static com.loopme.common.StaticParams.APPEND_TO_FILE;

public class Logging {

    private static final String PREFIX = "Debug.LoopMe.";

    private Logging() {}

    public static void out(String tag, final String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        final String logTag = new StringBuilder(PREFIX).append(tag).toString();

        if (StaticParams.DEBUG_MODE) {
            Log.i(logTag, text);
        }
        LiveDebug.handle(logTag, text);
    }

    public static void logEvent(String appKey, String event) {
        if (TextUtils.isEmpty(appKey)) {
            return;
        }
        FileUtils.logToFile("AppKey: " + appKey, APPEND_TO_FILE);
        logEvent(event);
    }

    public static void logEvent(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        String message = Utils.getCurrentDate() + ": " + text;
        FileUtils.logToFile(message, APPEND_TO_FILE);
    }
}
