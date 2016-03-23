package com.loopme.common;

import android.text.TextUtils;
import android.util.Log;

import com.loopme.common.StaticParams;
import com.loopme.debugging.DebugController;

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
        DebugController.handle(logTag, text);
    }
}
