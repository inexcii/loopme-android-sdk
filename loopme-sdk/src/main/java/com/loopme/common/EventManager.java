package com.loopme.common;

import android.net.Uri;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventManager {

    public static final String EVENT_VIDEO_25 = "VIDEO_25";
    public static final String EVENT_VIDEO_50 = "VIDEO_50";
    public static final String EVENT_VIDEO_75 = "VIDEO_75";

    private static final String LOG_TAG = EventManager.class.getSimpleName();

    private static final String URL = "loopme.me/api/v2/events";
    private static final String SDK_FEEDBACK = "SDK_FEEDBACK";

    private static final String EVENT_TYPE = "et";
    private static final String R = "r";
    private static final String ID = "id";

    private String build(String token) {
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

        builder.appendQueryParameter(EVENT_TYPE, SDK_FEEDBACK)
                .appendQueryParameter(R, "1")
                .appendQueryParameter(ID, token)
                .build();

        return builder.toString();
    }

    /**
     * If we advertise some app,
     * in AdResponse we have the list of package ids of this app.
     * We check if this package already installed, we send this event to server,
     * and it will not send us this ads any more.
     */
    public void trackSdkEvent(String token) {
        ExecutorService executor = Executors.newCachedThreadPool();
        final String eventUrl = build(token);
        executor.submit(new Runnable() {

            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL(eventUrl);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.getInputStream();
                } catch (IOException e) {
                    Logging.out(LOG_TAG, String.valueOf(e));
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        });
    }
}
