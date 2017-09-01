package com.loopme.debugging;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;

import com.loopme.common.Logging;
import com.loopme.common.StaticParams;
import com.loopme.common.Utils;
import com.loopme.request.AdRequestParametersProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LiveDebug {

    private static final String LOG_TAG = LiveDebug.class.getSimpleName();

    //default debug time is 5 minutes
    private static final int DEBUG_TIME = 5 * 60 * 1000;

    private static LogDbHelper sLogDbHelper;
    private static ExecutorService sExecutor = Executors.newSingleThreadExecutor();

    private static CountDownTimer sDebugTimer;
    private static boolean sIsDebugOn;

    public static void init(Context context) {
        sLogDbHelper = new LogDbHelper(context);
    }

    public static void setLiveDebug(final boolean debug) {
        Logging.out(LOG_TAG, "setLiveDebug " + debug);
        if (sIsDebugOn != debug) {
            if (debug) {
                sIsDebugOn = debug;
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        startTimer();
                    }
                });
            }
        }
    }

    public static void handle(String logTag, String text) {
        if (sIsDebugOn) {
            saveLog(logTag, text);
        }
    }

    private static void startTimer() {
        if (sDebugTimer == null) {
            sDebugTimer = new CountDownTimer(DEBUG_TIME, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    sendToServer();
                    sIsDebugOn = false;
                    sDebugTimer = null;
                }
            };
            Logging.out(LOG_TAG, "start debug timer");
            sDebugTimer.start();
        }
    }

    private static void sendToServer() {
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                if (sLogDbHelper != null) {
                    Logging.out(LOG_TAG, "send to server");
                    Map<String, String> params = initPostDataParams();
                    HttpUtils.postDataToServer(params);
                }
            }
        });
    }

    private static Map<String, String> initPostDataParams() {
        String debugLogs = initLogsString();
        AdRequestParametersProvider provider = AdRequestParametersProvider.getInstance();

        Map<String, String> params = new HashMap<>();
        params.put(Params.DEVICE_OS, "android");
        params.put(Params.SDK_TYPE, "loopme");
        params.put(Params.SDK_VERSION, StaticParams.SDK_VERSION);
        params.put(Params.DEVICE_ID, provider.getViewerToken());
        params.put(Params.PACKAGE_ID, provider.getPackage());
        params.put(Params.APP_KEY, provider.getAppKey());
        params.put(Params.MSG, "sdk_debug");
        params.put(Params.DEBUG_LOGS, debugLogs);
        params.put(Params.APP_IDS, Utils.getPackageInstalledAsString(provider.getPackagesInstalled()));
        return params;
    }

    private static String initLogsString() {
        if (sLogDbHelper != null) {
            List<String> loglist = sLogDbHelper.getLogs();
            sLogDbHelper.clear();
            StringBuilder sb = new StringBuilder();
            for (String s : loglist) {
                sb.append(s);
                sb.append("\n");
            }
            return sb.toString();
        }
        return null;
    }

    private static void saveLog(String logTag, String text) {
        final String logString = formatLogMessage(logTag, text);
        if (sLogDbHelper != null) {
            sExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    sLogDbHelper.putLog(logString);
                }
            });
        }
    }

    private static String formatLogMessage(String logTag, String text) {
        String thread = (Looper.getMainLooper() == Looper.myLooper()) ? "ui" : "bg";
        StringBuilder sb = new StringBuilder()
                .append(thread)
                .append(": ")
                .append(logTag)
                .append(": ")
                .append(text);
        return sb.toString();
    }
}