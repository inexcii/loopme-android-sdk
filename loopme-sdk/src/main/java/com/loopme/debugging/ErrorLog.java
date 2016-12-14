package com.loopme.debugging;

import android.text.TextUtils;

import com.loopme.common.StaticParams;
import com.loopme.request.AdRequestParametersProvider;
import com.loopme.common.Logging;

import java.util.HashMap;
import java.util.Map;

public class ErrorLog {

    private static final String LOG_TAG = ErrorLog.class.getSimpleName();

    private ErrorLog() {}

    public static void post(String errorMessage) {
        Logging.out(LOG_TAG, errorMessage);
        Map<String, String> params = initPostDataParams(errorMessage, null);
        HttpUtils.postDataToServer(params);
    }

    public static void post(String errorMessage, String type) {
        Logging.out(LOG_TAG, errorMessage);
        Map<String, String> params = initPostDataParams(errorMessage, type);
        HttpUtils.postDataToServer(params);
    }

    private static Map<String, String> initPostDataParams(String errorMessage, String type) {
        Map<String, String> params = new HashMap<>();
        AdRequestParametersProvider provider = AdRequestParametersProvider.getInstance();

        params.put(Params.DEVICE_OS, "android");
        params.put(Params.SDK_TYPE, "loopme");
        params.put(Params.SDK_VERSION, StaticParams.SDK_VERSION);
        params.put(Params.DEVICE_ID, provider.getViewerToken());
        params.put(Params.PACKAGE_ID, provider.getPackage());
        params.put(Params.APP_KEY, TextUtils.isEmpty(provider.getAppKey()) ?
                "unknown" : provider.getAppKey());

        params.put(Params.MSG, "sdk_error");
        params.put(Params.ERROR_TYPE, TextUtils.isEmpty(type) ? ErrorType.CUSTOM : type);

        params.put(Params.ERROR_MSG, errorMessage);
        return params;
    }
}
