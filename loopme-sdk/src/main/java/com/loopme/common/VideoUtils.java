package com.loopme.common;

import android.content.Context;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

class VideoUtils {

    private static final String LOG_TAG = VideoUtils.class.getSimpleName();

    public static final String MP4_FORMAT = ".mp4";
    public static final String VIDEO_FOLDER = "LoopMeAds";
    /**
     * 127 chars is max lenght of file name with extension (4 chars)
     */
    private static final int MAX_FILE_NAME_LENGHT = 127 - 4;

    public static void deleteInvalidVideoFiles(Context context) {
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

    public static File checkFileNotExists(String filename, Context context) {
        File parentDir = VideoUtils.getParentDir(context);
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

    public static String detectFileName(String videoUrl) {
        String fileName = null;
        try {
            URL url = new URL(videoUrl);
            fileName = url.getFile();
            if (fileName != null && !fileName.isEmpty()) {
                if (!fileName.endsWith(VideoUtils.MP4_FORMAT)) {
                    int urlHash = videoUrl.hashCode();
                    // return positive value
                    return Long.toString( urlHash & 0xFFFFFFFFL);
                } else {
                    fileName = fileName.replace(VideoUtils.MP4_FORMAT, "");
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

    public static File getParentDir(Context context) {
        return context == null ? null : context.getExternalFilesDir(VIDEO_FOLDER);
    }

    static void clearCache(Context context) {
        Logging.out(LOG_TAG, "Clear cache");
        File parentDir = context.getExternalFilesDir(VIDEO_FOLDER);

        File[] files = new File[0];
        if (parentDir == null) {
            return;
        }
        files = parentDir.listFiles();
        int deletedFilesCounter = 0;
        for (File file : files) {
            if (!file.isDirectory()) {
                if (file.getName().endsWith(MP4_FORMAT)) {
                    File f = new File(file.getAbsolutePath());
                    f.delete();
                    deletedFilesCounter++;
                }
            }
        }
        Logging.out(LOG_TAG, "Deleted " + deletedFilesCounter + " file(s)");
    }
}
