package com.loopme.debugging;

import android.text.TextUtils;

import com.loopme.common.Logging;
import com.loopme.common.StaticParams;
import com.loopme.request.AdRequestParametersProvider;

import java.util.HashMap;
import java.util.Map;

public class ErrorLog {

    private static final String LOG_TAG = ErrorLog.class.getSimpleName();

    private ErrorLog() {
    }

    public static void post(String errorMessage) {
        post(errorMessage, null, null);
    }

    public static void post(String errorMessage, String type) {
        post(errorMessage, type, null);
    }

    public static void post(String errorMessage, String type, String appKey) {
        Logging.out(LOG_TAG, errorMessage);
        Map<String, String> params = initGeneralPostDataParams(type, appKey);
        params.put(Params.MSG, Params.SDK_ERROR);
        params.put(Params.ERROR_MSG, errorMessage);
        HttpUtils.postDataToServer(params);
    }

    private static Map<String, String> initGeneralPostDataParams(String type, String appkey) {
        Map<String, String> params = new HashMap<>();
        AdRequestParametersProvider provider = AdRequestParametersProvider.getInstance();

        params.put(Params.DEVICE_OS, Params.OS_ANDROID);
        params.put(Params.SDK_TYPE, Params.SDK_TYPE_LOOP_ME);
        params.put(Params.SDK_VERSION, StaticParams.SDK_VERSION);
        params.put(Params.DEVICE_ID, provider.getViewerToken());
        params.put(Params.PACKAGE_ID, provider.getPackage());
        params.put(Params.APP_KEY, TextUtils.isEmpty(appkey) ? provider.getAppKey() : appkey);
        params.put(Params.ERROR_TYPE, TextUtils.isEmpty(type) ? ErrorType.CUSTOM : type);
        return params;
    }

    public static void postDebugEvent(String param, String value) {
        if (LiveDebug.isDebugOn()) {
            Logging.out(LOG_TAG, param + "=" + value);
            Map<String, String> params = initGeneralPostDataParams(null, null);
            params.put(param, value);
            HttpUtils.postDataToServer(params);
        }
    }
}
