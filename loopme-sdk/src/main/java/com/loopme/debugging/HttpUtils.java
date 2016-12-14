package com.loopme.debugging;

import com.loopme.common.ExecutorHelper;
import com.loopme.common.Logging;
import com.loopme.common.StaticParams;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

class HttpUtils {

    private static final String LOG_TAG = HttpUtils.class.getSimpleName();

    private static final int REQUEST_TIMEOUT = 10000;
    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

    private static final String ERROR_URL = "https://track.loopme.me/api/errors";

    private static void handleRequest(final Map<String, String> params) {
        URL url;
        HttpURLConnection urlConnection = null;

        try {
            url = new URL(ERROR_URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(REQUEST_TIMEOUT);
            urlConnection.setConnectTimeout(REQUEST_TIMEOUT);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            urlConnection.setRequestProperty("Content-Type", CONTENT_TYPE);

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            String data = getPostDataString(params);

            writer.write(data);
            writer.flush();
            writer.close();
            os.close();

            urlConnection.connect();

            int code = urlConnection.getResponseCode();
            Logging.out(LOG_TAG, "response code : " + code);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Logging.out(LOG_TAG, e.getMessage());

        } catch (IOException e) {
            e.printStackTrace();
            Logging.out(LOG_TAG, e.getMessage());

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    public static void postDataToServer(final Map<String, String> params) {
        ExecutorHelper.getExecutor().submit(new Runnable() {
            @Override
            public void run() {
                handleRequest(params);
            }
        });
    }

    private static String getPostDataString(Map<String, String> params) throws UnsupportedEncodingException {
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
}
