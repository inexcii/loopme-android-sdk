package com.loopme.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.loopme.AES;
import com.loopme.BaseAd;
import com.loopme.LoopMeBannerGeneral;
import com.loopme.LoopMeBannerView;
import com.loopme.constants.StretchOption;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.loopme.common.StaticParams.UNKNOWN_NAME;

public class Utils {

    private static final int START_BODY_TAG = 173;
    private static final String CHROME = "Chrome";
    private static final String CHROME_SHORTCUT = "Chrm";
    private static final String LOG_TAG = Utils.class.getSimpleName();
    private static final String DATE_PATTERN = "dd/MM/yy HH:mm:ss.s";
    private static WindowManager sWindowManager;
    private static Resources sResources;
    private static LocationManager sLocationManager;
    private static PackageManager sPackageManager;
    private static AudioManager sAudioManager;
    private static SimpleDateFormat sFormatter = new SimpleDateFormat(DATE_PATTERN);

    public static boolean isOnline(Context context) {
        boolean isOnline;
        try {
            final ConnectivityManager conMgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            if (conMgr == null) {
                return false;
            }

            final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

            return activeNetwork != null && activeNetwork.isConnected()
                    && activeNetwork.isAvailable();
        } catch (Exception e) {
            e.printStackTrace();
            isOnline = false;
        }
        return isOnline;
    }

    public static int convertDpToPixel(float dp) {
        if (sResources != null) {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                    sResources.getDisplayMetrics());
        } else {
            return 0;
        }
    }

    public static void init(Context context) {
        if (context != null) {
            sWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            sResources = context.getResources();
            sLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            sPackageManager = context.getPackageManager();
            sAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }
    }

    public static String getStringFromStream(InputStream inputStream) {
        int numberBytesRead = 0;
        StringBuilder out = new StringBuilder();
        byte[] bytes = new byte[4096];

        try {
            while ((numberBytesRead = inputStream.read(bytes)) != -1) {
                out.append(new String(bytes, 0, numberBytesRead));
            }
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toString();
    }

    public static Location getLastKnownLocation() {
        Location result;

        if (sLocationManager == null) {
            return null;
        }
        Location gpsLocation = null;
        try {
            gpsLocation = sLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            Logging.out(LOG_TAG, "Failed to retrieve GPS location: access appears to be disabled.");
        } catch (IllegalArgumentException e) {
            Logging.out(LOG_TAG, "Failed to retrieve GPS location: device has no GPS provider.");
        }

        Location networkLocation = null;
        try {
            networkLocation = sLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException e) {
            Logging.out(LOG_TAG, "Failed to retrieve network location: access appears to be disabled.");
        } catch (IllegalArgumentException e) {
            Logging.out(LOG_TAG, "Failed to retrieve network location: device has no network provider.");
        }

        if (gpsLocation == null && networkLocation == null) {
            return null;
        }

        if (gpsLocation != null && networkLocation != null) {
            if (gpsLocation.getTime() > networkLocation.getTime()) {
                result = gpsLocation;
            } else {
                result = networkLocation;
            }
        } else if (gpsLocation != null) {
            result = gpsLocation;
        } else {
            result = networkLocation;
        }

        return result;
    }

    public static DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (sWindowManager == null) {
            return displayMetrics;
        }
        sWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    public static boolean isPackageInstalled(List<String> packadeId) {
        if (sPackageManager == null) {
            return false;
        }
        List<PackageInfo> packages = sPackageManager.getInstalledPackages(0);

        for (PackageInfo packageInfo : packages) {
            for (int i = 0; i < packadeId.size(); i++) {
                if (packadeId.get(i).equalsIgnoreCase(packageInfo.packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<String> getPackageInstalled(List<String> packadeId) {
        List<String> allPackages = new ArrayList<>();
        if (sPackageManager == null) {
            return null;
        }
        List<PackageInfo> packages = sPackageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            allPackages.add(packageInfo.packageName);
        }
        return allPackages;
    }

    public static void animateAppear(View view) {
        view.animate()
                .setDuration(500)
                .alpha(1.0f);
    }

    public static float getSystemVolume() {
        if (sAudioManager != null) {
            int volume_level = sAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int max = sAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int percent = Math.round(volume_level * 100 / max);
            return (float) percent / 100;
        } else {
            return 1.0f;
        }
    }

    public static int getScreenOrientation() {
        if (sWindowManager == null || sWindowManager.getDefaultDisplay() == null) {
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        int rotation = sWindowManager.getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        sWindowManager.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:S
        else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }

    public static int getScreenWidth() {
        if (sWindowManager == null) {
            return 0;
        }
        Display display = sWindowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static int getScreenHeight() {
        if (sWindowManager == null) {
            return 0;
        }
        Display display = sWindowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static BitmapDrawable decodeImage(String base64drawable) {
        byte[] rawImageData = Base64.decode(base64drawable, 0);
        return new BitmapDrawable(null, new ByteArrayInputStream(rawImageData));
    }

    public static FrameLayout.LayoutParams calculateNewLayoutParams(
            FrameLayout.LayoutParams lp,
            int mVideoWidth, int mVideoHeight,
            int mResizeWidth, int mResizeHeight,
            StretchOption mStretch) {

        lp.gravity = Gravity.CENTER;

        int blackLines;
        float percent = 0;

        if (mVideoWidth > mVideoHeight) {
            lp.width = mResizeWidth;
            lp.height = (int) ((float) mVideoHeight / (float) mVideoWidth * (float) mResizeWidth);

            blackLines = mResizeHeight - lp.height;
            if (lp.height != 0) {
                percent = blackLines * 100 / lp.height;
            }
        } else {
            lp.height = mResizeHeight;
            lp.width = (int) ((float) mVideoWidth / (float) mVideoHeight * (float) mResizeHeight);

            blackLines = mResizeWidth - lp.width;
            if (lp.width != 0) {
                percent = blackLines * 100 / lp.width;
            }
        }

        switch (mStretch) {
            case NONE:
                if (percent < 11) {
                    lp.width = mResizeWidth;
                    lp.height = mResizeHeight;
                }
                break;

            case STRETCH:
                lp.width = mResizeWidth;
                lp.height = mResizeHeight;
                break;

            case NO_STRETCH:
                //
                break;
        }
        return lp;
    }

    public static String getViewVisibility(View view) {
        String visibilityStr = null;
        switch (view.getVisibility()) {
            case View.VISIBLE:
                visibilityStr = "VISIBLE";
                break;
            case View.INVISIBLE:
                visibilityStr = "INVISIBLE";
                break;
            case View.GONE:
                visibilityStr = "GONE";
                break;
        }
        return visibilityStr;
    }

    public static void clearCache(Context context) {
        VideoUtils.clearCache(context);
    }

    public static boolean isEmulator() {
        return Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK")
                || Build.MANUFACTURER.contains("Genymotion");
    }

    public static int convertPixelToDp(int pixels) {
        if (sResources != null) {
            return (int) (pixels / sResources.getDisplayMetrics().density);
        } else {
            return 0;
        }
    }

    public static ViewGroup.LayoutParams getParamsSafety(LoopMeBannerGeneral banner) {
        try {
            return banner.getBannerView().getLayoutParams();
        } catch (NullPointerException e) {
            Logging.out(LOG_TAG, "Warning! Check integration of LoopMeBanner");
        }
        return null;
    }

    public static String getCurrentDate() {
        return sFormatter.format(new Date());
    }

    private static String deleteLastCharacter(String encryptedString) {
        if (!TextUtils.isEmpty(encryptedString)) {
            return encryptedString.substring(0, encryptedString.length() - 1);
        }
        return "";
    }

    public static String getEncryptedString(String name) {
        AES.setDefaultKey();
        AES.encrypt(name);
        return Utils.deleteLastCharacter(AES.getEncryptedString());
    }

    public static String getUrlEncodedString(String stringToEncode) {
        try {
            return URLEncoder.encode(stringToEncode, StaticParams.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return UNKNOWN_NAME;
    }

    public static void setCacheDirectory(Context context) {
        if (TextUtils.isEmpty(StaticParams.sCacheDirectory)) {
            StaticParams.sCacheDirectory = context.getFilesDir().getAbsolutePath();
        }
    }

    public static String makeChromeShortCut(String userString) {
        if (!TextUtils.isEmpty(userString) && userString.contains(CHROME)) {
            return userString.replace(CHROME, CHROME_SHORTCUT);
        } else {
            return userString;
        }
    }

    public static void setAdIdOrAppKey(Intent intent, BaseAd baseAd) {
        if (intent == null || baseAd == null) {
            return;
        }
        intent.putExtra(StaticParams.AD_ID_TAG, baseAd.getAdId());
    }
    public static String getPackageInstalledAsString(List<String> packagesIds) {
        List<String> packagesInstalled = getPackageInstalled(packagesIds);
        if(packagesInstalled == null || packagesInstalled.size() == 0){
            return "";
        }
        String[] packagesInstalledArray = new String[packagesInstalled.size()];
        packagesInstalledArray = packagesInstalled.toArray(packagesInstalledArray);

        StringBuilder stringBuilder = new StringBuilder();
        for(String packageName : packagesInstalledArray){
            stringBuilder.append(packageName);
            stringBuilder.append(",");
        }
        String formattedString = stringBuilder.toString();
        if (formattedString.length() > 0 && formattedString.charAt(formattedString.length() - 1) == ',') {
            formattedString = formattedString.substring(0, formattedString.length() - 1);
        }
        Log.i(LOG_TAG, formattedString);
        return formattedString;
    }

    public static String formatTime(double time) {
        DecimalFormat formatter = new DecimalFormat("0.00");
        return formatter.format(time);
    }

    public static void removeParent(ViewGroup view) {
        if (view != null && view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    public static String addMraidScript(String html) {
        if (TextUtils.isEmpty(html)) {
            return "";
        }
        String firstPart = html.substring(0, START_BODY_TAG);
        String lastPart = html.substring(START_BODY_TAG);
        return firstPart + StaticParams.MRAID_SCRIPT + lastPart;
    }

    public static void broadcastDestroyIntent(Context context, int adId) {
        Intent intent = new Intent();
        intent.putExtra(StaticParams.AD_ID_TAG, adId);
        intent.setAction(StaticParams.DESTROY_INTENT);
        context.sendBroadcast(intent);
    }

    @SuppressLint("NewApi")
    public static void addBordersToView(LoopMeBannerView bannerView) {
        ShapeDrawable drawable = new ShapeDrawable(new RectShape());
        drawable.getPaint().setColor(Color.BLACK);
        drawable.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
        drawable.getPaint().setAntiAlias(true);

        bannerView.setPadding(2, 2, 2, 2);
        bannerView.setBackground(drawable);
    }

    public static void configMinimizedViewLayoutParams(LoopMeBannerView bannerView, MinimizedMode minimizedMode) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) bannerView.getLayoutParams();
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.bottomMargin = minimizedMode.getMarginBottom();
        lp.rightMargin = minimizedMode.getMarginRight();
        bannerView.setLayoutParams(lp);
    }

    public static Animation getRemoveViewAnim(Context context, boolean toRight) {
        Animation anim = AnimationUtils.makeOutAnimation(context, toRight);
        anim.setDuration(200);
        return anim;
    }
}