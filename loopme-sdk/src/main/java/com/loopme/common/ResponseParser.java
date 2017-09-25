package com.loopme.common;

import com.loopme.constants.AdFormat;
import com.loopme.debugging.ErrorLog;
import com.loopme.debugging.ErrorType;
import com.loopme.debugging.LiveDebug;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

public class ResponseParser {

    private static final String LOG_TAG = ResponseParser.class.getSimpleName();

    private static final String JSON_SCRIPT = "script";
    private static final String JSON_FORMAT = "format";
    private static final String JSON_ORIENTATION = "orientation";
    private static final String JSON_EXPIRED_TIME = "ad_expiry_time";
    private static final String JSON_SETTINGS = "settings";
    private static final String JSON_PACKAGE_IDS = "package_ids";
    private static final String JSON_TOKEN = "token";
    private static final String JSON_DEBUG = "debug";
    private static final String JSON_PART_PRELOAD = "preload25";

//    private static final String JSON_V360 = "v360";
    private static final String JSON_TRACKING = "measure_partners";
    private static final String JSON_ERROR = "error";
    private static final String JSON_MRAID = "mraid";
    private static final String JSON_V360 = "v360";
    private static final String JSON_AUTOLOADING = "autoloading";

    private Listener mListener;
    private int mAdFormat;

    public interface Listener {
        void onParseError(LoopMeError message);
    }

    public ResponseParser(Listener listener, int format) {
        if (listener == null) {
            Logging.out(LOG_TAG, "Wrong parameter(s)");
        }
        mListener = listener;
        mAdFormat = format;
    }

    public AdParams getAdParams(String result) {
        if (result == null) {
            return null;
        } else if (result.isEmpty()) {
            handleParseError("No content");
            ErrorLog.post("Broken response", ErrorType.SERVER);
            return null;
        }

        String format;
        JSONObject object;
        JSONObject settings;

        try {
            object = (JSONObject) new JSONTokener(result).nextValue();

            settings = object.getJSONObject(JSON_SETTINGS);

            format = settings.getString(JSON_FORMAT);
            if (!isValidFormat(format)) {
                ErrorLog.post("Broken response [wrong format parameter: " + format + "]", ErrorType.SERVER);
            }
            String requestedFormat;
            switch (mAdFormat) {
                case AdFormat.BANNER:
                    requestedFormat = StaticParams.BANNER_TAG;
                    break;
                case AdFormat.INTERSTITIAL:
                    requestedFormat = StaticParams.INTERSTITIAL_TAG;
                    break;
                default:
                    requestedFormat = "";
                    break;
            }
            if (!format.equalsIgnoreCase(requestedFormat)) {
                handleParseError("Wrong Ad format: " + format);
                return null;
            }

        } catch (JSONException e) {
            handleParseError("Exception during json parse");
            ErrorLog.post("Broken response", ErrorType.SERVER);
            return null;

        } catch (ClassCastException ex) {
            ex.printStackTrace();
            handleParseError("Exception during json parse");
            ErrorLog.post("Broken response", ErrorType.SERVER);
            return null;
        }

        int debugValue = parseInt(settings, JSON_DEBUG);
        boolean debug = debugValue == 1;
        LiveDebug.setLiveDebug(debug);

        int preloadValue = parseInt(settings, JSON_PART_PRELOAD);
        boolean preload = preloadValue == 1;
        StaticParams.PART_PRELOAD = preload;//todo remove. only for tester

        int video360Value = parseInt(settings, JSON_V360);
        boolean video360 = video360Value == 1;

        int mraidValue = parseInt(settings, JSON_MRAID);
        boolean mraid = mraidValue == 1;

        int autoloadingValue = parseIntWithDefaultTrue(settings, JSON_AUTOLOADING);
        boolean autoloading = autoloadingValue == 1;

        return new AdParams.AdParamsBuilder(format)
                .html(parseString(object, JSON_SCRIPT))
                .orientation(parseString(settings, JSON_ORIENTATION))
                .expiredTime(parseInt(settings, JSON_EXPIRED_TIME))
                .token(parseString(settings, JSON_TOKEN))
                .packageIds(parseArray(settings, JSON_PACKAGE_IDS))
                .trackers(parseArray(settings, JSON_TRACKING))
                .partPreload(preload)
                .video360(video360)
                .mraid(mraid)
                .autoloading(autoloading)
                .build();
    }

    private boolean isValidFormat(String format) {
        if (format == null) {
            return false;
        }
        return format.equalsIgnoreCase(StaticParams.BANNER_TAG) ||
                format.equalsIgnoreCase(StaticParams.INTERSTITIAL_TAG);
    }

    private void handleParseError(String mess) {
        if (mListener != null) {
            mListener.onParseError(new LoopMeError(mess));
        }
    }

    private List<String> parseArray(JSONObject object, String jsonParam) {
        List<String> packagIds = new ArrayList<String>();
        try {
            JSONArray array = object.getJSONArray(jsonParam);
            for (int i = 0; i < array.length(); i++) {
                String item = (String) array.get(i);
                packagIds.add(item);
            }
        } catch (JSONException e) {
            Logging.out(LOG_TAG, jsonParam + " absent");
        }
        return packagIds;
    }

    private String parseString(JSONObject object, String jsonParam) {
        String value = null;
        try {
            value = object.getString(jsonParam);
        } catch (JSONException e) {
            Logging.out(LOG_TAG, jsonParam + " absent");
        }
        return value;
    }

    private JSONObject parseJsonObject(JSONObject object, String jsonParam) {
        JSONObject value = null;
        try {
            value = object.getJSONObject(jsonParam);
        } catch (JSONException e) {
            Logging.out(LOG_TAG, jsonParam + " absent");
        }
        return value;
    }

    private int parseInt(JSONObject object, String jsonParam) {
        int value = 0;
        try {
            value = object.getInt(jsonParam);
        } catch (JSONException e) {
            Logging.out(LOG_TAG, jsonParam + " absent");
        }
        return value;
    }


    private int parseIntWithDefaultTrue(JSONObject object, String jsonParam) {
        int value = 1;
        try {
            value = object.getInt(jsonParam);
        } catch (JSONException e) {
            Logging.out(LOG_TAG, jsonParam + " absent");
        }
        return value;
    }

    private String extractTrackingUrl(JSONObject tracking) {
        String value = null;
        try {
            if (tracking != null) {
                value = tracking.getJSONArray(JSON_ERROR).getString(0);
            }
        } catch (JSONException e) {
            Logging.out(LOG_TAG, JSON_ERROR + " absent");
        }
        return value;
    }
}
