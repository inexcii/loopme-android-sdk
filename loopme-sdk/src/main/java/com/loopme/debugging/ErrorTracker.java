package com.loopme.debugging;

import android.net.Uri;
import android.util.Log;

import com.loopme.AdRequestParametersProvider;
import com.loopme.Logging;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ErrorTracker {

    private static final String LOG_TAG = ErrorTracker.class.getSimpleName();

    private static final String URL = "loopme.me/api/v2/events";

    private static final String ID = "id";
    private static final String EVENT_TYPE = "et";
    private static final String MSG = "msg"; //todo it's no implemented on backend. Parameter can be differ

    private static ExecutorService sExecutor = Executors.newCachedThreadPool();

    private ErrorTracker() {}

    public static void post(String errorMessage) {
        Logging.out(LOG_TAG, errorMessage);

//        String token = AdRequestParametersProvider.getInstance().getViewerToken();
//        final String errorUrl = buildRequest(token, errorMessage);
//        Log.d(LOG_TAG, errorUrl);
//        sExecutor.submit(new Runnable() {
//            @Override
//            public void run() {
//                URL url;
//                HttpURLConnection urlConnection = null;
//
//                try {
//                    url = new URL(errorUrl);
//                    urlConnection = (HttpURLConnection) url.openConnection();
//                    urlConnection.getInputStream();
//
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//
//                } finally {
//                    if (urlConnection != null) {
//                        urlConnection.disconnect();
//                    }
//                }
//            }
//        });
    }

    private static String buildRequest(String token, String message) {
        String str = URL;
        List<String> list = Arrays.asList(str.split("/"));

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https");

        for (String s : list) {
            if (list.indexOf(s) == 0) {
                builder.authority(s);
            } else {
                builder.appendPath(s);
            }
        }

        builder.appendQueryParameter(EVENT_TYPE, "ERROR")
                .appendQueryParameter(ID, token)
                .appendQueryParameter(MSG, message)
                .build();

        return builder.toString();
    }
}
