package com.loopme.debugging;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Looper;
import android.os.Handler;

import com.loopme.AdRequestParametersProvider;
import com.loopme.Logging;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DebugController {

    private static final String LOG_TAG = DebugController.class.getSimpleName();

    private static final String URL = "http://loopme.me/api/errors";
    private static final String MSG = "msg";
    private static final String TOKEN = "token";
    private static final String PACKAGE = "package";
    private static final String DEBUG_LOGS = "debug_logs";

    private static final String MSG_VALUE = "sdk_debug";

    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

    //default debug time is 5 minutes
    private static final int DEBUG_TIME = 5 * 60 * 1000;
    private static final int REQUEST_TIMEOUT = 10000;

    private static LogDbHelper sLogDbHelper;
    private static ExecutorService sExecutor = Executors.newSingleThreadExecutor();

    private static CountDownTimer sDebugTimer;
    private static boolean sIsDebugOn;

    public static void init(Context context) {
        sLogDbHelper = new LogDbHelper(context);
    }

    public static void setLiveDebug(final boolean debug) {
        Logging.out(LOG_TAG, "setLiveDebug " + debug, Logging.LogLevel.DEBUG);
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
            Logging.out(LOG_TAG, "start debug timer", Logging.LogLevel.DEBUG);
            sDebugTimer.start();
        }
    }

    private static void sendToServer() {
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                if (sLogDbHelper != null) {
                    Logging.out(LOG_TAG, "send to server", Logging.LogLevel.DEBUG);
                    Map<String, String> params = initPostDataParams();
                    postDataToServer(params);
                }
            }
        });
    }

    private static Map<String, String> initPostDataParams() {
        String debugLogs = initLogsString();
        AdRequestParametersProvider provider = AdRequestParametersProvider.getInstance();

        Map<String, String> params = new HashMap<>(4);
        params.put(MSG, MSG_VALUE);
        params.put(TOKEN, provider.getViewerToken());
        params.put(PACKAGE, sLogDbHelper.getContext().getPackageName());
        params.put(DEBUG_LOGS, debugLogs);

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

    private static void postDataToServer(final Map<String, String> params) {
        URL url;
        HttpURLConnection urlConnection = null;

        try {
            url = new URL(URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(REQUEST_TIMEOUT);
            urlConnection.setConnectTimeout(REQUEST_TIMEOUT);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            urlConnection.setRequestProperty("Content-Type", CONTENT_TYPE);

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));

            String data = getPostDataString(params);
            writer.write(data);
            writer.flush();
            writer.close();
            os.close();

            urlConnection.connect();

            int code = urlConnection.getResponseCode();
            Logging.out(LOG_TAG, "response code : " + code, Logging.LogLevel.DEBUG);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Logging.out(LOG_TAG, e.getMessage(), Logging.LogLevel.DEBUG);

        } catch (IOException e) {
            e.printStackTrace();
            Logging.out(LOG_TAG, e.getMessage(), Logging.LogLevel.DEBUG);

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private static String getPostDataString(Map<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first) {
                first = false;
            } else {
                result.append("&");
            }

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
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