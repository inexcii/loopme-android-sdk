package com.loopme.common;

import android.text.TextUtils;

import com.loopme.debugging.ErrorLog;
import com.loopme.debugging.ErrorType;

import java.util.ArrayList;
import java.util.List;

/**
 * Ad parameters. Builds from server response.
 */
public class AdParams {

    private static final String LOG_TAG = AdParams.class.getSimpleName();

    private final String mHtml;
    private final String mFormat;
    private final String mOrientation;
    private final int mExpiredDate;

    private List<String> mPackageIds = new ArrayList<String>();
    private List<String> mTrackers = new ArrayList<>();
    private String mToken;

    private boolean mPartPreload;
    private boolean mVideo360;
    private boolean mMraid;
    private boolean mOwnCloseButton;

    private AdParams(AdParamsBuilder builder) {
        mFormat = builder.mBuilderFormat;
        mHtml = builder.mBuilderHtml;
        mOrientation = builder.mBuilderOrientation;

        mExpiredDate = builder.mBuilderExpiredDate == 0 ?
                StaticParams.DEFAULT_EXPIRED_TIME :
                builder.mBuilderExpiredDate;

        mPackageIds = builder.mPackageIds;
        mTrackers = builder.mTrackers;
        mToken = builder.mToken;

        mPartPreload = builder.mPartPreload;
        mVideo360 = builder.mVideo360;

        mMraid = builder.mMraid;

        Logging.out(LOG_TAG, "Server response indicates  ad params: "
                + "format: " + mFormat + ", orientation: " + mOrientation
                + ", mraid: " + mMraid + ", expire in: " + mExpiredDate);
    }

    public boolean getPartPreload() {
        return mPartPreload;
    }

    public boolean isVideo360() {
        return mVideo360;
    }

    public boolean isMraid() {
        return mMraid;
    }

    public String getHtml() {
        return mHtml;
    }

    public String getAdFormat() {
        return mFormat;
    }

    public String getAdOrientation() {
        return mOrientation;
    }

    public int getExpiredTime() {
        return mExpiredDate;
    }

    public List<String> getPackageIds() {
        return mPackageIds;
    }

    public List<String> getTrackers(){
        return mTrackers;
    }
    public String getToken() {
        return mToken;
    }

    public boolean isOwnCloseButton() {
        return mOwnCloseButton;
    }

    public void setOwnCloseButton(boolean hasOwnCloseButton) {
        mOwnCloseButton = hasOwnCloseButton;
    }

    static class AdParamsBuilder {

        private final String mBuilderFormat;

        private String mBuilderHtml;
        private String mBuilderOrientation;
        private int mBuilderExpiredDate;

        private List<String> mPackageIds = new ArrayList<String>();
        private String mToken;

        private boolean mPartPreload;
        private boolean mVideo360;
        private boolean mMraid;
        private List<String> mTrackers = new ArrayList<>();

        public AdParamsBuilder(String format) {
            mBuilderFormat = format;
        }

        public AdParamsBuilder packageIds(List<String> installPacakage) {
            mPackageIds = installPacakage;
            return this;
        }

        public AdParamsBuilder trackers(List<String> trackers) {
            mTrackers = trackers;
            return this;
        }

        public AdParamsBuilder partPreload(boolean preload) {
            mPartPreload = preload;
            return this;
        }

        public AdParamsBuilder video360(boolean b) {
            mVideo360 = b;
            return this;
        }

        public AdParamsBuilder mraid(boolean isMraid) {
            mMraid = isMraid;
            return this;
        }

        public AdParamsBuilder token(String token) {
            mToken = token;
            return this;
        }

        public AdParamsBuilder html(String html) {
            if (TextUtils.isEmpty(html)) {
                ErrorLog.post("Broken response [empty html]", ErrorType.SERVER);
            }
            mBuilderHtml = html;
            return this;
        }

        public AdParamsBuilder orientation(String orientation) {
            if (isValidOrientationValue(orientation)) {
                mBuilderOrientation = orientation;
            } else {
                if (!TextUtils.isEmpty(mBuilderFormat) && mBuilderFormat.equalsIgnoreCase(StaticParams.INTERSTITIAL_TAG))
                    ErrorLog.post("Broken response [invalid orientation: " + orientation + "]", ErrorType.SERVER);
            }
            return this;
        }

        public AdParamsBuilder expiredTime(int time) {
            int timeSec = time * 1000;
            mBuilderExpiredDate = Math.max(StaticParams.DEFAULT_EXPIRED_TIME,
                    timeSec);
            return this;
        }

        public AdParams build() {
            if (isValidFormatValue()) {
                return new AdParams(this);
            } else {
                Logging.out(LOG_TAG, "Wrong ad format value");
                return null;
            }
        }

        private boolean isValidFormatValue() {
            return mBuilderFormat != null &&
                    (mBuilderFormat.equalsIgnoreCase(StaticParams.BANNER_TAG) ||
                            mBuilderFormat.equalsIgnoreCase(StaticParams.INTERSTITIAL_TAG));
        }

        private boolean isValidOrientationValue(String orientation) {
            return orientation != null && (orientation.equalsIgnoreCase(StaticParams.ORIENTATION_PORT) ||
                    orientation.equalsIgnoreCase(StaticParams.ORIENTATION_LAND));
        }
    }
}
