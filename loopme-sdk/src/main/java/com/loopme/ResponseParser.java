package com.loopme;

import com.loopme.debugging.DebugController;
import com.loopme.debugging.ErrorTracker;

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
        }
        String format = null;
        JSONObject object = null;
        JSONObject settings = null;

        try {
            object = (JSONObject) new JSONTokener(result).nextValue();

            settings = object.getJSONObject(JSON_SETTINGS);

            format = settings.getString(JSON_FORMAT);
            if (!isValidFormat(format)) {
                ErrorTracker.post("Response broken. Wrong format parameter: " + format);
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
                handleParseError("Wrong Ad format");
                return null;
            }

        } catch (JSONException e) {
            handleParseError("Exception during json parse");
            ErrorTracker.post("Broken json");
            return null;

        } catch (ClassCastException ex) {
            ex.printStackTrace();
            handleParseError("Exception during json parse");
            ErrorTracker.post("Broken json");
            return null;
        }

        int debugValue = parseInt(settings, JSON_DEBUG);
        boolean debug = debugValue == 1 ? true : false;
        DebugController.setLiveDebug(debug);

        int preloadValue = parseInt(settings, JSON_PART_PRELOAD);
        boolean preload = preloadValue == 1 ? true : false;
//        StaticParams.USE_PART_PRELOAD = preload;//todo remove, only for preload tester

        return new AdParams.AdParamsBuilder(format)
                .html(parseString(object, JSON_SCRIPT))
                .orientation(parseString(settings, JSON_ORIENTATION))
                .expiredTime(parseInt(settings, JSON_EXPIRED_TIME))
                .token(parseString(settings, JSON_TOKEN))
                .packageIds(parseArray(settings, JSON_PACKAGE_IDS))
//                .partPreload(preload)
                .build();
    }

    private boolean isValidFormat(String format) {
        if (format == null) {
            return false;
        }
        if (format.equalsIgnoreCase(StaticParams.BANNER_TAG) ||
                format.equalsIgnoreCase(StaticParams.INTERSTITIAL_TAG)) {
            return true;
        }
        return false;
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

    private int parseInt(JSONObject object, String jsonParam) {
        int value = 0;
        try {
            value = object.getInt(jsonParam);
        } catch (JSONException e) {
            Logging.out(LOG_TAG, jsonParam + " absent");
        }
        return value;
    }
}
