package com.loopme.common;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.loopme.debugging.ErrorTracker;
import com.loopme.request.AdRequestParametersProvider;
import com.loopme.constants.ConnectionType;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class VideoLoader {

    private static final String LOG_TAG = VideoLoader.class.getSimpleName();

    public static final String VIDEO_FOLDER = "LoopMeAds";

    /**
     * 127 chars is max lenght of file name with extension (4 chars)
     */
    private static final int MAX_FILE_NAME_LENGHT = 127 - 4;

    public static final String MP4_FORMAT = ".mp4";

    private Callback mCallback;
    private Context mContext;
    private String mVideoUrl;
    private boolean mPartPreload;

    private File mVideoFile;
    private String mFileName;

    private volatile HttpURLConnection mConnection;
    private volatile boolean mIsVideoFullyDownloaded;
    private volatile boolean mStop;

    public VideoLoader(@NonNull String videoUrl, boolean preload, @NonNull Context context,
                       @NonNull Callback callback) {
        mCallback = callback;
        mContext = context;
        mVideoUrl = videoUrl;
        mPartPreload = preload;
    }

    public void start() {
        Logging.out(LOG_TAG, "start");
        Logging.out(LOG_TAG, "Use mobile network for caching: " + StaticParams.USE_MOBILE_NETWORK_FOR_CACHING);
        deleteInvalidVideoFiles(mContext);

        mFileName = detectFileName(mVideoUrl) + MP4_FORMAT;

        File f = checkFileNotExists(mFileName, mContext);

        if (f != null) {
            Logging.out(LOG_TAG, "Video file already exists");
            if (mCallback != null) {
                mCallback.onLoadFromFile(getParentDir(mContext).getAbsolutePath() + "/" + mFileName);
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
        ExecutorHelper.getExecutor().submit(new Runnable() {
            @Override
            public void run() {
                loadWithHttpUrlConnection(mFileName);
            }
        });

    }

    private void loadWithHttpUrlConnection(String filename) {
        if (mStop) {
            return;
        }
        Logging.out(LOG_TAG, "loadWithHttpUrlConnection: " + filename);
        InputStream stream = null;
        int downloaded = 0;
        String eTag = null;
        int lengthOfFile = 0;
        FileOutputStream out = null;
        try {
            URL url = new URL(mVideoUrl);
            mConnection = (HttpURLConnection) url.openConnection();
            mConnection.setRequestMethod("HEAD");

            if (mConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                eTag = mConnection.getHeaderField("ETag");

                lengthOfFile = mConnection.getContentLength();
                mConnection.disconnect();

                if(mStop) {
                    return;
                }

                Logging.out(LOG_TAG, "Length of file: " + lengthOfFile);
                mConnection = (HttpURLConnection) url.openConnection();
                mConnection.setRequestMethod("GET");
                mConnection.setReadTimeout(20000);
                mConnection.setConnectTimeout(20000);
                mConnection.setRequestProperty("Range", "bytes=0-" + lengthOfFile);
                mConnection.setRequestProperty("If-Range", eTag);

                stream = new BufferedInputStream(mConnection.getInputStream());

                if (mStop) {
                    return;
                }
                final String fullFileName = getParentDir(mContext).getAbsolutePath() + "/" + filename;
                mVideoFile = new File(fullFileName);

                out = new FileOutputStream(mVideoFile);
                byte buffer[] = new byte[4096];
                int length = 0;

                while ((length = stream.read(buffer)) != -1) {
                    out.write(buffer,0, length);
                    downloaded+=length;
                }
                out.close();

                mIsVideoFullyDownloaded = true;
                Logging.out(LOG_TAG, "download complete! file size: " + mVideoFile.length());

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onLoadFromFile(fullFileName);
                        }
                    }
                });
            } else {
                ErrorTracker.post("Bad asset: " + mVideoUrl);
                if (mCallback != null) {
                    mCallback.onError(new LoopMeError("Error during loading video"));
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            ErrorTracker.post("Bad asset: " + mVideoUrl);

        } catch (IOException e) {
            Logging.out(LOG_TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
            reconnect(downloaded, eTag, lengthOfFile);

        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void reconnect(int downloadedBefore, String eTag, int lengthOfFile) {
        if (mStop) {
            return;
        }
        Logging.out(LOG_TAG, "reconnect " + downloadedBefore);
        if (downloadedBefore == 0) {
            ErrorTracker.post("Bad asset: " + mVideoUrl);
            if (mCallback != null) {
                mCallback.onError(new LoopMeError("Error during video loading"));
            }
            return;
        }

        int downloaded = downloadedBefore;
        try {
            URL url = new URL(mVideoUrl);
            mConnection = (HttpURLConnection) url.openConnection();
            mConnection.setRequestMethod("GET");
            mConnection.setRequestProperty("Range", "bytes=" + downloadedBefore + "-" + lengthOfFile);
            mConnection.setRequestProperty("If-Range", eTag);

            InputStream stream = new BufferedInputStream(mConnection.getInputStream());

            FileOutputStream out = new FileOutputStream(mVideoFile, true);
            byte buffer[] = new byte[4096];
            int length = 0;

            while ((length = stream.read(buffer)) != -1) {
                out.write(buffer, 0, length);
                downloaded+=length;
            }
            stream.close();
            out.close();

            Logging.out(LOG_TAG, "download complete! file size " + mVideoFile.length());
            mIsVideoFullyDownloaded = true;

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    String fullFileName = getParentDir(mContext).getAbsolutePath() + "/" + mFileName;
                    if (mCallback != null) {
                        mCallback.onLoadFromFile(fullFileName);
                    }
                }
            });

        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            Logging.out(LOG_TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
            reconnect(downloaded, eTag, lengthOfFile);
        }
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

    public void stop() {
        Logging.out(LOG_TAG, "stop()");
        mStop = true;
        if (mConnection != null) {
            ExecutorHelper.getExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    Logging.out(LOG_TAG, "disconnect()");
                    mConnection.disconnect();
                }
            });
        }

        //delete file if it not fully downloaded
        if (!mIsVideoFullyDownloaded && mVideoFile != null && mVideoFile.exists()) {
            Logging.out(LOG_TAG, "remove bad file");
            mVideoFile.delete();
        }
    }

    private void deleteInvalidVideoFiles(Context context) {
        File parentDir = getParentDir(context);
        if (parentDir == null) {
            return;
        }

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
                    int urlHash = videoUrl.hashCode();
                    // return positive value
                    return Long.toString( urlHash & 0xFFFFFFFFL);
                } else {
                    fileName = fileName.replace(MP4_FORMAT, "");
                    int lastSlash = fileName.lastIndexOf("/");
                    int length = fileName.length();
                    fileName = fileName.substring(lastSlash + 1, length);

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
        if (parentDir == null) {
            return null;
        }
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

    public interface Callback {
        void onError(LoopMeError error);
        void onLoadFromUrl(String url);
        void onLoadFromFile(String filePath);
    }
}
