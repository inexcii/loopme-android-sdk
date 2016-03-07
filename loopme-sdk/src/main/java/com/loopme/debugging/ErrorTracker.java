package com.loopme.debugging;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.loopme.request.AdRequestParametersProvider;
import com.loopme.common.Logging;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ErrorTracker {

    private static final String LOG_TAG = ErrorTracker.class.getSimpleName();

    private static final String SERVER_ISSUES_URL = "loopme.me/sj/tr";

    private static final String ERROR_TYPE = "error_type";
    private static final String ID = "id";
    private static final String ET = "et";
    private static final String ET_VALUE = "ERROR";

    private static String URL;

    private static ExecutorService sExecutor = Executors.newCachedThreadPool();

    private ErrorTracker() {}

    public static void init(String errorUrl) {
        Logging.out(LOG_TAG, "init " + errorUrl);
        if (TextUtils.isEmpty(errorUrl)) {
            return;
        }
        URL = errorUrl;

        Uri uri = Uri.parse(URL);
        Set<String> params = uri.getQueryParameterNames();
        Uri.Builder newUri = uri.buildUpon().clearQuery();
        for (String param : params) {
            if (!param.equals(ERROR_TYPE)) {
                String value = uri.getQueryParameter(param);
                newUri.appendQueryParameter(param, value);
            }
        }
        newUri.build();
        URL = newUri.toString();
    }

    private static String buildServerIssueUrl(String errorMessage) {
        String str = SERVER_ISSUES_URL;
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

        builder.appendQueryParameter(ET, ET_VALUE);
        String id = AdRequestParametersProvider.getInstance().getViewerToken();
        builder.appendQueryParameter(ID, id);
        builder.appendQueryParameter(ERROR_TYPE, errorMessage);
        builder.build();
        return builder.toString();
    }

    public static void post(String errorMessage) {
        Logging.out(LOG_TAG, errorMessage);
        String errorUrl;
        if (TextUtils.isEmpty(URL)) {
            errorUrl = buildServerIssueUrl(errorMessage);
        } else {
            errorUrl = addRequestParameter(ERROR_TYPE, errorMessage);
        }
        sendDataToServer(errorUrl);
    }

    private static String addRequestParameter(String parameter, String value) {
        Uri uri = Uri.parse(URL);
        Uri.Builder builder = uri.buildUpon();
        builder.appendQueryParameter(parameter, value).build();
        return builder.toString();
    }

    private static void sendDataToServer(final String errorUrl) {
        sExecutor.submit(new Runnable() {
            @Override
            public void run() {
                URL url;
                HttpURLConnection urlConnection = null;

                try {
                    url = new URL(errorUrl);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.getInputStream();

                } catch (MalformedURLException e) {
                    e.printStackTrace();

                } catch (IOException e) {
                    e.printStackTrace();

                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        });
    }
}
