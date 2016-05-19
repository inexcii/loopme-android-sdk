package com.loopme.data;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Base64;

import com.loopme.common.Logging;
import com.loopme.common.StaticParams;
import com.loopme.request.AdRequestParametersProvider;
import com.loopme.tasks.AdvIdFetcher;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class DataCollector {

    private static final String LOG_TAG = "data";

    private static final String URL = "http://loopme.me/api/v2/events?et=INFO";

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
    private static final String PARAM_OLD_VIEWER_TOKEN = "ovt";
    private static final String PARAM_ACCOUNT_NAME = "an";
    private static final String PARAM_IMEI = "imei";

    private static final byte[] encryptionKey = new byte[] {
            (byte) 0xfa, (byte) 0x62, (byte) 0x44, (byte) 0xa2,
            (byte) 0x97, (byte) 0xa4, (byte) 0xba, (byte) 0x03,
            (byte) 0x2e, (byte) 0x89, (byte) 0xde, (byte) 0x9b,
            (byte) 0x77, (byte) 0xf3, (byte) 0xa2, (byte) 0xf9  };

    private static AlarmManager mAlarmManager;
    private static PendingIntent mAlarmIntent;
    private static boolean sStarted;

    private long mInterval;

    private static DataCollector sDataCollector;
    private Context mContext;

    private String mCurrentId;
    private String mOldId;

    public static DataCollector getInstance(Context context) {
        if (sDataCollector == null) {
            sDataCollector = new DataCollector(context);
        }
        return sDataCollector;
    }

    private DataCollector(Context context) {
        mContext = context;
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (mAlarmManager == null) {
            Logging.out(LOG_TAG, "AlarmManager not available");
            return;
        }
        Intent intent = new Intent(context, LoopMeReceiver.class);
        mAlarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        final SharedPreferences sp = context.getSharedPreferences(StaticParams.LOOPME_PREFERENCES,
                Context.MODE_PRIVATE);

        final String viewerToken = sp.getString(StaticParams.VIEWER_TOKEN, "");

        if (TextUtils.isEmpty(viewerToken)) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(new AdvIdFetcher(context, new AdvIdFetcher.Listener() {

                @Override
                public void onComplete(String advId, boolean isLimited) {
                    sp.edit().putString(StaticParams.VIEWER_TOKEN, advId).commit();
                }
            }));
        }
    }

    private List<String> detectAccounts(Context context) {
        List<String> result = new ArrayList<>();
        int permissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.GET_ACCOUNTS);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            AccountManager am = AccountManager.get(context);
            Account[] accounts = am.getAccountsByType("com.google");
            for (Account acc : accounts) {
                result.add(acc.name);
            }
        }
        return result;
    }

    public void onReceive(String oldId) {
        final List<String> accounts = detectAccounts(mContext);

        mOldId = oldId;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new AdvIdFetcher(mContext, new AdvIdFetcher.Listener() {

            @Override
            public void onComplete(String advId, boolean isLimited) {
                mCurrentId = advId;
                if (TextUtils.isEmpty(mCurrentId)) {
                    return;
                }
                Map<String, String> params = initPostDataParams(mOldId, mCurrentId, accounts);
                postDataToServer(params);
            }
        }));
    }

    private Map<String, String> initPostDataParams(String oldId, String currentId, List<String> accounts) {
        AdRequestParametersProvider provider = AdRequestParametersProvider.getInstance();

        Map<String, String> params = new HashMap<>(4);
        params.put(PARAM_CONNECTION_TYPE, String.valueOf(provider.getConnectionType(mContext)));
        params.put(PARAM_LANGUAGE, provider.getLanguage());
        params.put(PARAM_SDK_VERSION, StaticParams.SDK_VERSION);
        params.put(PARAM_APP_VERSION, provider.getAppVersion(mContext));
        params.put(PARAM_MRAID, provider.getMraidSupport());
        params.put(PARAM_ORIENTATION, provider.getOrientation(mContext));
        params.put(PARAM_BUNDLE_ID, mContext.getPackageName());
        params.put(PARAM_VIEWER_TOKEN, currentId);

        if (!TextUtils.isEmpty(oldId) && !currentId.equalsIgnoreCase(oldId)) {
            params.put(PARAM_OLD_VIEWER_TOKEN, oldId);
        }

        //user accounts
        if (accounts != null && accounts.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < accounts.size(); i++) {
                sb.append(accounts.get(i));
                if (i < (accounts.size() - 1)) {
                    sb.append(",");
                }
            }
            if (sb.length() != 0) {
                params.put(PARAM_ACCOUNT_NAME, sb.toString());
            }
        }

        String latitude = provider.getLatitude();
        if (latitude != null) {
            params.put(PARAM_LATITUDE, latitude);
        }

        String longitude = provider.getLongitude();
        if (longitude != null) {
            params.put(PARAM_LONGITUDE, longitude);
        }

        String carrier = provider.getCarrier(mContext);
        if (carrier != null) {
            params.put(PARAM_CARRIER, carrier);
        }

        String dntValue = provider.isDntPresent() ? "1" : "0";
        params.put(PARAM_DNT, dntValue);

        if (provider.isWifiInfoAvailable(mContext)) {
            String wifiName = provider.getWifiName(mContext);
            if (!TextUtils.isEmpty(wifiName)) {
                params.put(PARAM_WIFI_NAME, wifiName);
            }
        }

        String deviceId = provider.getDeviceId(mContext);
        if (!TextUtils.isEmpty(deviceId)) {
            params.put(PARAM_IMEI, deviceId);
        }

        return params;
    }

    private void postDataToServer(final Map<String, String> params) {
        URL url;
        HttpURLConnection urlConnection = null;

        try {
            url = new URL(URL + "&vt=" + params.get(PARAM_VIEWER_TOKEN));
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));

            String data = getPostDataString(params);

            String base64encryptedData = encodeData(data);

            writer.write(base64encryptedData);
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

    public static String encodeData(String initMessage) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return Base64.encodeToString(cipher.doFinal(initMessage.getBytes()), Base64.DEFAULT);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException |
                InvalidKeyException |
                IllegalBlockSizeException |
                BadPaddingException e) {
            // do nothing
        }
        return null;
    }

    private static String getPostDataString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first) {
                first = false;
            } else {
                result.append(",");
            }

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    public void start() {
        if (mAlarmManager != null && !sStarted) {
            Logging.out(LOG_TAG, "start");
            if (mInterval >= 0) {
                mInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
            }
            mAlarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(),
                    mInterval, mAlarmIntent);
            sStarted = true;
        }
    }

    /**
     * Define custom interval for data collector
     * By default: collector triggered once each 5 minutes
     * @param milliseconds - time in milliseconds
     */
    public void setInterval(long milliseconds) {
        mInterval = milliseconds;
    }

    /**
     * Stop data collector
     */
    public void cancel() {
        if (mAlarmManager != null) {
            Logging.out(LOG_TAG, "cancel");
            mAlarmManager.cancel(mAlarmIntent);
        }
    }
}
