package com.loopme.tasks;

import com.loopme.common.AdParams;
import com.loopme.common.Logging;
import com.loopme.common.LoopMeError;
import com.loopme.common.ResponseParser;
import com.loopme.common.StaticParams;
import com.loopme.common.Utils;
import com.loopme.debugging.ErrorLog;
import com.loopme.debugging.ErrorType;
import com.loopme.request.AdRequestParametersProvider;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class AdFetcher implements Runnable {

    private static final String LOG_TAG = AdFetcher.class.getSimpleName();
    private static final int RESPONSE_CODE_SUCCESS = 200;
    private static final int RESPONSE_CODE_UNKNOWN = 0;

    private final String mRequestUrl;
    private Listener mListener;
    private int mFormat;
    private String mAppKey;
    private LoopMeError mLoopMeError;
    private HttpURLConnection mUrlConnection;
    private static final String USER_AGENT = "User-Agent";


    public interface Listener {
        void onComplete(AdParams params, LoopMeError error);
    }

    public AdFetcher(String requestUrl, Listener listener, int format, String appKey) {
        mRequestUrl = requestUrl;
        mListener = listener;
        mFormat = format;
        mAppKey = appKey;
    }

    @Override
    public void run() {
        Logging.out(LOG_TAG, "Start making http request to server...");
        String result = getResponse(mRequestUrl);
        compete(result);
        Logging.out(LOG_TAG, "Response received.");
    }

    private void compete(String result) {
        if (result == null) {
            complete(null, mLoopMeError);
        } else {
            ResponseParser parser = new ResponseParser(new ResponseParser.Listener() {

                @Override
                public void onParseError(LoopMeError error) {
                    complete(null, error);
                }
            }, mFormat);
            AdParams adParams = parser.getAdParams(result);
            if (adParams != null) {
                complete(adParams, null);
            }
        }
    }

    private String getResponse(String url) {
        int responseCode = 0;
        try {
            URL request = new URL(url);
            mUrlConnection = (HttpURLConnection) request.openConnection();
            mUrlConnection.setRequestProperty(USER_AGENT, getUserAgent());
            mUrlConnection.setReadTimeout(StaticParams.REQUEST_TIMEOUT);
            mUrlConnection.setConnectTimeout(StaticParams.REQUEST_TIMEOUT);

            responseCode = mUrlConnection.getResponseCode();
            if (responseCode != RESPONSE_CODE_SUCCESS) {
                throw new IOException();
            }
            InputStream in = new BufferedInputStream(mUrlConnection.getInputStream());
            return Utils.getStringFromStream(in);

        } catch (SocketTimeoutException e) {
            Logging.out(LOG_TAG + "timeout ad_request", ErrorType.SERVER);
            mLoopMeError = new LoopMeError("Request timeout");
            ErrorLog.post("Request timeout", ErrorType.SERVER, mAppKey);
        } catch (IOException e) {
            Logging.out(LOG_TAG, e.getMessage());
            createError(responseCode);
            if (responseCode != RESPONSE_CODE_UNKNOWN && responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                ErrorLog.post("Bad servers response code " + responseCode, ErrorType.SERVER, mAppKey);
            }
        } finally {
            if (mUrlConnection != null) {
                mUrlConnection.disconnect();
            }
        }
        return null;
    }

    private void createError(int responseCode) {
        mLoopMeError = responseCode == HttpURLConnection.HTTP_NO_CONTENT ? new LoopMeError("No ads found")
                : new LoopMeError("Server code: " + responseCode);
    }

    private String getUserAgent() {
        AdRequestParametersProvider provider = AdRequestParametersProvider.getInstance();
        return provider.getUserAgent();
    }

    private void complete(final AdParams params, final LoopMeError error) {
        if (mListener != null) {
            mListener.onComplete(params, error);
        }
    }
}
