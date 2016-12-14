package com.loopme.common;

import android.text.TextUtils;
import android.util.Log;

import com.loopme.debugging.LiveDebug;

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
}
