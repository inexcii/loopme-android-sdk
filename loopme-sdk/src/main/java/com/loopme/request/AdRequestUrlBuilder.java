package com.loopme.request;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.loopme.IntegrationType;
import com.loopme.common.Logging;
import com.loopme.common.StaticParams;

import java.util.Arrays;
import java.util.List;

import static com.loopme.constants.DeviceType.UNKNOWN;

/**
 * Builds request url.
 */
public class AdRequestUrlBuilder {

    private static final String LOG_TAG = AdRequestUrlBuilder.class.getSimpleName();

    private static final String PARAM_APPKEY = "ak";
    private static final String PARAM_CONNECTION_TYPE = "ct";
    private static final String PARAM_LANGUAGE = "lng";
    private static final String PARAM_SDK_VERSION = "sv";
    private static final String PARAM_APP_VERSION = "av";
    private static final String PARAM_MRAID = "mr";
    private static final String PARAM_ORIENTATION = "or";
    private static final String PARAM_VIEWER_TOKEN = "vt";
    private static final String PARAM_DNT = "dnt";
    private static final String PARAM_LATITUDE = "lat";
    private static final String PARAM_LONGITUDE = "lon";
    private static final String PARAM_CARRIER = "carrier";
    private static final String PARAM_BUNDLE_ID = "bundleid";
    private static final String PARAM_WIFI_NAME = "wn";
    private static final String PARAM_CHARGE_LEVEL = "chl";
    private static final String PARAM_PLUGGED = "plg";
    private static final String PARAM_WEBVIEW_VERSION = "webview";
    private static final String PARAM_INTEGRATION_TYPE = "it";
    private static final String PARAM_SCREEN_WIDTH = "ww";
    private static final String PARAM_SCREEN_HEIGHT = "wh";
    private static final String PARAM_AD_WIDTH = "width";
    private static final String PARAM_AD_HEIGHT = "height";
    private static final String PARAM_DEVICE_TYPE = "devicetype";
    private static final String PARAM_DEVICE_NAME = "pn";

    /**
     * Optional targeting parameters
     */
    private static final String PARAM_KEYWORDS = "keywords";
    private static final String PARAM_YEAR_OF_BIRTH = "yob";
    private static final String PARAM_GENDER = "gender";

    private static final String PARAM_V360 = "v360";

    private final Context mContext;
    private IntegrationType mIntegrationType;

    public AdRequestUrlBuilder(Context context) {
        mContext = context;
        if (context == null) {
            Logging.out(LOG_TAG, "Context should not be null. Can't build request url");
        }
    }

    public String buildRequestUrl(String appKey, AdTargetingData metadata, IntegrationType integrationType) {
        mIntegrationType = integrationType != null ? integrationType : IntegrationType.NORMAL;
        return buildRequestUrl(appKey, metadata);
    }

    public String buildRequestUrl(String appKey, AdTargetingData metadata) {
        Logging.out(LOG_TAG, "Start build request url");

        if (mContext == null) {
            return null;
        }
        AdRequestParametersProvider provider = AdRequestParametersProvider.getInstance();

        String str = StaticParams.BASE_URL;
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

        builder.appendQueryParameter(PARAM_APPKEY, appKey)
                .appendQueryParameter(PARAM_CONNECTION_TYPE, String.valueOf(provider.getConnectionType(mContext)))
                .appendQueryParameter(PARAM_LANGUAGE, provider.getLanguage())
                .appendQueryParameter(PARAM_SDK_VERSION, StaticParams.SDK_VERSION)
                .appendQueryParameter(PARAM_V360, "1")
                .appendQueryParameter(PARAM_APP_VERSION, provider.getAppVersion(mContext))
                .appendQueryParameter(PARAM_MRAID, provider.getMraidSupport())
                .appendQueryParameter(PARAM_ORIENTATION, provider.getOrientation(mContext))
                .appendQueryParameter(PARAM_VIEWER_TOKEN, provider.getViewerToken())
                .appendQueryParameter(PARAM_BUNDLE_ID, mContext.getPackageName())
                .appendQueryParameter(PARAM_WEBVIEW_VERSION, provider.getWebViewVersion(mContext))
                .appendQueryParameter(PARAM_INTEGRATION_TYPE, mIntegrationType.getType())
                .appendQueryParameter(PARAM_SCREEN_WIDTH, String.valueOf(provider.getScreenWidth()))
                .appendQueryParameter(PARAM_SCREEN_HEIGHT, String.valueOf(provider.getScreenHeight()))
                .appendQueryParameter(PARAM_AD_WIDTH, String.valueOf(provider.getAdWidth()))
                .appendQueryParameter(PARAM_AD_HEIGHT, String.valueOf(provider.getAdHeight()))
                .appendQueryParameter(PARAM_DEVICE_NAME, provider.getDeviceName());

        String latitude = provider.getLatitude();
        if (latitude != null) {
            builder.appendQueryParameter(PARAM_LATITUDE, latitude);
        }

        String longitude = provider.getLongitude();
        if (longitude != null) {
            builder.appendQueryParameter(PARAM_LONGITUDE, longitude);
        }

        String carrier = provider.getCarrier(mContext);
        if (carrier != null) {
            builder.appendQueryParameter(PARAM_CARRIER, carrier);
        }

        String deviceType = provider.getDeviceType();
        if (!TextUtils.equals(deviceType.toLowerCase(), UNKNOWN.toLowerCase())) {
            builder.appendQueryParameter(PARAM_DEVICE_TYPE, deviceType.toLowerCase());
        }

        String dntValue = provider.isDntPresent() ? "1" : "0";
        builder.appendQueryParameter(PARAM_DNT, dntValue);

        if (provider.isWifiInfoAvailable(mContext)) {
            String wifiName = provider.getWifiName(mContext);
            if (!TextUtils.isEmpty(wifiName)) {
                builder.appendQueryParameter(PARAM_WIFI_NAME, wifiName);
            }
        }

        if (metadata != null && metadata.getKeywords() != null) {
            builder.appendQueryParameter(PARAM_KEYWORDS, metadata.getKeywords());
        }

        if (metadata != null && metadata.getGender() != null) {
            builder.appendQueryParameter(PARAM_GENDER, metadata.getGender());
        }

        if (metadata != null && metadata.getYob() != 0) {
            builder.appendQueryParameter(PARAM_YEAR_OF_BIRTH, String.valueOf(metadata.getYob()));
        }

        if (metadata != null && !metadata.getCustomParameters().isEmpty()) {
            for (CustomRequestParameter crp : metadata.getCustomParameters()) {
                builder.appendQueryParameter(crp.getParamName(), crp.getParamValue());
            }
        }

        String[] batteryInfo = provider.getBatteryInfo(mContext);
        builder.appendQueryParameter(PARAM_CHARGE_LEVEL, batteryInfo[0]);
        builder.appendQueryParameter(PARAM_PLUGGED, batteryInfo[1]);

        String url = builder.build().toString();

        Logging.out(LOG_TAG, "Finish build request url");

        return url;
    }
}