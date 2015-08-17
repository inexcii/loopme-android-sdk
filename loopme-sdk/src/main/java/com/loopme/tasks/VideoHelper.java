package com.loopme.tasks;

import android.text.TextUtils;
import com.loopme.Logging;

import java.net.MalformedURLException;
import java.net.URL;

public class VideoHelper {

    private static final String LOG_TAG = VideoHelper.class.getSimpleName();
    /**
     * 127 chars is max lenght of file name with extension (4 chars)
     */
    private static final int MAX_FILE_NAME_LENGHT = 127 - 4;
    private static final String MP4_FORMAT = ".mp4";

    String detectFileName(String videoUrl) {
        String fileName = null;
        try {
            URL url = new URL(videoUrl);
            fileName = url.getFile();
            if (fileName != null && !fileName.isEmpty()) {

                if (!fileName.endsWith(MP4_FORMAT)) {
                    Logging.out(LOG_TAG, "Wrong video url (not .mp4 format)", Logging.LogLevel.DEBUG);
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
}
