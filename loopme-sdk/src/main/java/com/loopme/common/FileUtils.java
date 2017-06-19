package com.loopme.common;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by vynnykiakiv on 4/20/17.
 */

public class FileUtils {
    private FileUtils() {
    }

    public static void logToFile(String message, boolean append) {
        if (isExternalStorageAvailable() && !isExternalStorageReadOnly()) {
            File file = getCachedLogFile();
            try (FileWriter writer = new FileWriter(file, append)) {
                writer.write(message + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    public static File getCachedLogFile() {
        File directoryPath = new File(StaticParams.sCacheDirectory);
        createDirectory(directoryPath);
        File logFile = new File(directoryPath, StaticParams.CACHED_LOG_FILE_NAME);
        createNewFile(logFile);

        return logFile;
    }

    private static void createDirectory(File directory) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    private static void createNewFile(File file) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
