package com.loopme.common;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.loopme.debugging.ErrorLog;
import com.loopme.debugging.ErrorType;
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

    private static final int TIMEOUT = 20000;
    private Callback mCallback;
    private Context mContext;
    private String mVideoUrl;
    private boolean mPartPreload;

    private File mVideoFile;
    private String mShortFileName;

    private int lenghtOfPreview;

    private volatile HttpURLConnection mConnection;
    private volatile boolean mIsVideoFullyDownloaded;
    private volatile boolean mStop;

    private FileOutputStream mOutputStream;

    public VideoLoader(@NonNull String videoUrl, boolean preload, @NonNull Context context,
                       @NonNull Callback callback) {
        mCallback = callback;
        mContext = context;
        mVideoUrl = videoUrl;
        mPartPreload = preload;
    }

    public void start() {
        Logging.out(LOG_TAG, "start");
        handleEmulator();
        Logging.out(LOG_TAG, "Use mobile network for caching: " + StaticParams.USE_MOBILE_NETWORK_FOR_CACHING);
        VideoUtils.deleteInvalidVideoFiles(mContext);

        mShortFileName = VideoUtils.detectFileName(mVideoUrl) + VideoUtils.MP4_FORMAT;

        File f = VideoUtils.checkFileNotExists(mShortFileName, mContext);

        if (f != null) {
            Logging.out(LOG_TAG, "Video file already exists");
            if (mCallback != null) {
                mCallback.onFullVideoLoaded(VideoUtils.getParentDir(mContext).getAbsolutePath() + "/" + mShortFileName);
            }
            return;
        }

        int connectiontype = AdRequestParametersProvider.getInstance().getConnectionType(mContext);
        if (connectiontype == ConnectionType.WIFI) {
            preloadVideo(mPartPreload);
        } else {
            if (StaticParams.USE_MOBILE_NETWORK_FOR_CACHING) {
                preloadVideo(mPartPreload);
            } else {
                if (mCallback != null) {
                    mCallback.onError(new LoopMeError("Mobile network. Video will not be cached"));
                }
            }
        }
    }

    private void handleEmulator() {
        if (Utils.isEmulator()) {
            Logging.out(LOG_TAG, "running on emulator");
        }
    }

    private void load(String filename, boolean preview) {
        if (mStop) {
            return;
        }
        InputStream stream;
        int downloaded = 0;
        String eTag = null;
        int lengthOfFile = 0;
        lenghtOfPreview = 0;
        try {
            URL url = new URL(mVideoUrl);
            mConnection = (HttpURLConnection) url.openConnection();
            mConnection.setRequestMethod("HEAD");

            if (mConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                eTag = mConnection.getHeaderField("ETag");

                lengthOfFile = mConnection.getContentLength();
                mConnection.disconnect();

                if (mStop) {
                    return;
                }

                Logging.out(LOG_TAG, "Length of file: " + lengthOfFile);
                if (preview) {
                    lenghtOfPreview = lengthOfFile / 4;
                }

                mConnection = (HttpURLConnection) url.openConnection();
                mConnection.setRequestMethod("GET");
                if (preview) {
                    configGetConnection(eTag, lenghtOfPreview);
                } else {
                    configGetConnection(eTag, lengthOfFile);
                }

                stream = new BufferedInputStream(mConnection.getInputStream());

                if (mStop) {
                    return;
                }

                mShortFileName = VideoUtils.getParentDir(mContext).getAbsolutePath() + "/" + filename;
                mVideoFile = new File(mShortFileName);
                mOutputStream = new FileOutputStream(mVideoFile);

                byte buffer[] = new byte[4096];
                int length;

                while ((length = stream.read(buffer)) != -1) {
                    mOutputStream.write(buffer, 0, length);
                    downloaded += length;
                }

                if (preview) {
                    handleVideoPreviewLoaded(downloaded, eTag, lengthOfFile);
                } else {
                    handleVideoFullDownloaded();
                }

            } else {
                if (mCallback != null) {
                    mCallback.onError(new LoopMeError("Error during loading video"));
                }
                ErrorLog.post("Bad asset[responseCode == " + mConnection.getResponseCode() + "]:" + mVideoUrl,
                        ErrorType.BAD_ASSET);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            ErrorLog.post("Bad asset[MalformedURLException]: " + mVideoUrl, ErrorType.BAD_ASSET);

        } catch (IOException e) {
            Logging.out(LOG_TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
            int lenght = preview ? lenghtOfPreview : lengthOfFile;
            reconnect(downloaded, eTag, lenght, preview);

        } finally {
            if (mOutputStream != null) {
                try {
                    mOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleVideoPreviewLoaded(int downloaded, String eTag, int lengthOfFile) {
        Logging.out(LOG_TAG, "downloaded preview! file size: " + mVideoFile.length());

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    mCallback.onPreviewLoaded(mShortFileName);
                }
            }
        });

        Logging.out(LOG_TAG, "load rest of file");
        reconnect(downloaded, eTag, lengthOfFile, false);
    }

    private void reconnect(int downloadedBefore, String eTag, int lengthOfFile, boolean preview) {
        if (mStop) {
            return;
        }
        Logging.out(LOG_TAG, "reconnect " + downloadedBefore + " " + preview);
        if (downloadedBefore == 0) {
            ErrorLog.post("Bad asset[loaded 0 bytes]: " + mVideoUrl, ErrorType.BAD_ASSET);
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

            FileOutputStream fos = new FileOutputStream(mVideoFile, true);
            byte buffer[] = new byte[4096];
            int length = 0;

            while ((length = stream.read(buffer)) != -1) {
                fos.write(buffer, 0, length);
                downloaded += length;
            }
            stream.close();
            fos.close();

            if (preview) {
                handleVideoPreviewLoaded(downloaded, eTag, lengthOfFile);
            } else {
                handleVideoFullDownloaded();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            Logging.out(LOG_TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
            reconnect(downloaded, eTag, lengthOfFile, preview);
        }
    }

    private void handleVideoFullDownloaded() {
        mIsVideoFullyDownloaded = true;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    mCallback.onFullVideoLoaded(mShortFileName);
                }
            }
        });

    }

    private void preloadVideo(final boolean partPreload) {
        Logging.out(LOG_TAG, "preloadVideo " + partPreload);
        ExecutorHelper.getExecutor().submit(new Runnable() {
            @Override
            public void run() {
                load(mShortFileName, partPreload);
            }
        });
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

    private void configGetConnection(String eTag, int lenght) {
        mConnection.setReadTimeout(TIMEOUT);
        mConnection.setConnectTimeout(TIMEOUT);
        mConnection.setRequestProperty("Range", "bytes=0-" + lenght);
        mConnection.setRequestProperty("If-Range", eTag);
    }

    public interface Callback {
        void onError(LoopMeError error);

        void onPreviewLoaded(String filePath);

        void onFullVideoLoaded(String filePath);
    }
}
