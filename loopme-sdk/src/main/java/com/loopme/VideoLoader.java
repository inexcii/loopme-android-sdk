package com.loopme;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.loopme.debugging.ErrorTracker;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class VideoLoader {

    private static final String LOG_TAG = VideoLoader.class.getSimpleName();

    private static final String VIDEO_FOLDER = "LoopMeAds";

    /**
     * 127 chars is max lenght of file name with extension (4 chars)
     */
    private static final int MAX_FILE_NAME_LENGHT = 127 - 4;

    private static final String MP4_FORMAT = ".mp4";

    private String mDownloadCompleteIntentName = DownloadManager.ACTION_DOWNLOAD_COMPLETE;
    private IntentFilter mDownloadCompleteIntentFilter = new IntentFilter(mDownloadCompleteIntentName);
    private long mDownloadId;
    private DownloadManager mDownloadManager;

    private Callback mCallback;
    private String mFilePath;

    private Context mContext;
    private String mVideoUrl;

    private boolean mPartPreload;

    public interface Callback {
        void onError(LoopMeError error);
        void onLoadFromUrl(String url);
        void onLoadFromFile(String filePath);
    }

    private BroadcastReceiver mDownloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            if (id != mDownloadId) {
                return;
            }
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(id);
            Cursor cursor = downloadManager.query(query);

            if (!cursor.moveToFirst()) {
                return;
            }

            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex)) {
                Logging.out(LOG_TAG, "Download Failed");
                if (mCallback != null) {
                    mCallback.onError(new LoopMeError("Download Failed"));
                }
                return;
            } else {
                Logging.out(LOG_TAG, "Download Success");
                if (mCallback != null) {
                    int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                    String downloadedPackageUriString = cursor.getString(uriIndex);
                    mCallback.onLoadFromFile(downloadedPackageUriString);
                }
            }
        }
    };

    public VideoLoader(@NonNull String videoUrl, boolean preload, @NonNull Context context,
                       @NonNull Callback callback) {
        mCallback = callback;
        mContext = context;
        mVideoUrl = videoUrl;
        mPartPreload = preload;

        mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        if (mDownloadManager == null) {
            Logging.out(LOG_TAG, "Error: DOWNLOAD_SERVICE not available");
        }
    }

    public void start() {
        Logging.out(LOG_TAG, "start");
        Logging.out(LOG_TAG, "Use mobile network for caching: " + StaticParams.USE_MOBILE_NETWORK_FOR_CACHING);
        deleteInvalidVideoFiles(mContext);

        String fileName = detectFileName(mVideoUrl) + MP4_FORMAT;
        mFilePath = "/" + VIDEO_FOLDER + "/" + fileName;

        File f = checkFileNotExists(fileName, mContext);
        if (f != null) {
            Logging.out(LOG_TAG, "Video file already exists");
            if (mCallback != null) {
                mCallback.onLoadFromFile(getParentDir(mContext).getAbsolutePath() + "/" + fileName);
            }
            return;
        }

        int connectiontype = AdRequestParametersProvider.getInstance().getConnectionType(mContext);
        if (connectiontype == ConnectionType.WIFI) {
            handlePreloadingType(mVideoUrl);
        } else {
            if (StaticParams.USE_MOBILE_NETWORK_FOR_CACHING) {
                handlePreloadingType(mVideoUrl);
            } else {
                if (mCallback != null) {
                    mCallback.onError(new LoopMeError("Mobile network. Video will not be cached"));
                }
            }
        }
    }

    /**
     * Load video when used part preload.
     * Triggered when video completely buffered
     */
    public void downloadVideo() {
        Logging.out(LOG_TAG, "downloadVideo");
        mCallback = null;
        int connectiontype = AdRequestParametersProvider.getInstance().getConnectionType(mContext);
        if (connectiontype == ConnectionType.WIFI) {
            downloadVideoToNewFile();
        } else {
            if (StaticParams.USE_MOBILE_NETWORK_FOR_CACHING) {
                downloadVideoToNewFile();
            } else {
                Logging.out(LOG_TAG, "Mobile network. Video will not be cached");
            }
        }
    }

    private void downloadVideoToNewFile() {
        mContext.registerReceiver(mDownloadCompleteReceiver, mDownloadCompleteIntentFilter);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mVideoUrl));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setDestinationInExternalFilesDir(mContext, null, mFilePath);

        mDownloadId = mDownloadManager.enqueue(request);
    }

    private void handlePreloadingType(String videoUrl) {
        if (mPartPreload) {
            if (mCallback != null) {
                mCallback.onLoadFromUrl(videoUrl);
            }
        } else {
            downloadVideoToNewFile();
        }
    }

    public void stop(boolean interruptFile) {
        Logging.out(LOG_TAG, "stop(" + interruptFile + ")");
        if (interruptFile) {
            mDownloadManager.remove(mDownloadId);
        }
        try {
            mContext.unregisterReceiver(mDownloadCompleteReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void deleteInvalidVideoFiles(Context context) {
        File parentDir = getParentDir(context);

        int amountOfCachedFiles = 0;
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                if (file.getName().endsWith(MP4_FORMAT)) {

                    File f = new File(file.getAbsolutePath());
                    long creationTime = f.lastModified();
                    long currentTime = System.currentTimeMillis();

                    if ((creationTime + StaticParams.CACHED_VIDEO_LIFE_TIME < currentTime) ||
                            (f.length() == 0)) {
                        f.delete();
                        Logging.out(LOG_TAG, "Deleted cached file: " + file.getAbsolutePath());
                    } else {
                        amountOfCachedFiles++;
                    }
                }
            }
        }
        Logging.out(LOG_TAG, "In cache " + amountOfCachedFiles + " file(s)");
        float cacheHours = StaticParams.CACHED_VIDEO_LIFE_TIME / (1000 * 60 * 60);
        Logging.out(LOG_TAG, "Cache time: " + cacheHours + " hours");
    }

    private String detectFileName(String videoUrl) {
        String fileName = null;
        try {
            URL url = new URL(videoUrl);
            fileName = url.getFile();
            if (fileName != null && !fileName.isEmpty()) {

                if (!fileName.endsWith(MP4_FORMAT)) {
                    Logging.out(LOG_TAG, "Wrong video url (not .mp4 format)");
                    ErrorTracker.post("Wrong video url (not .mp4 format): " + videoUrl);
                    return null;

                } else {
                    fileName = fileName.replace(MP4_FORMAT, "");
                    int lastSlash = fileName.lastIndexOf("/");
                    int lenght = fileName.length();
                    fileName = fileName.substring(lastSlash + 1, lenght);

                    if (fileName.length() > MAX_FILE_NAME_LENGHT) {
                        fileName = fileName.substring(0, MAX_FILE_NAME_LENGHT);
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    private File checkFileNotExists(String filename, Context context) {
        File parentDir = getParentDir(context);
        Logging.out(LOG_TAG, "Cache dir: " + parentDir.getAbsolutePath());

        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (!file.isDirectory() && file.getName().startsWith(filename)) {
                return file;
            }
        }
        return null;
    }

    private File getParentDir(Context context) {
        return context == null ? null : context.getExternalFilesDir(VIDEO_FOLDER);
    }
}
