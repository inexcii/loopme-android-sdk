package com.loopme.common;

import android.util.Log;

import com.loopme.common.StaticParams;
import com.loopme.debugging.DebugController;

public class Logging {

    private static final String PREFIX = "Debug.LoopMe.";

    private Logging() {}

    public static void out(String tag, final String text) {
        final String logTag = new StringBuilder(PREFIX).append(tag).toString();

        if (StaticParams.DEBUG_MODE) {
            Log.i(logTag, text);
        }
        DebugController.handle(logTag, text);
    }
}
