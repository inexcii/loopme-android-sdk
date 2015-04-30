package com.loopme.tasks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

import android.content.Context;
import android.text.TextUtils;

import com.loopme.Logging;
import com.loopme.Logging.LogLevel;
import com.loopme.StaticParams;

public class VideoTask implements Runnable {

	private static final String LOG_TAG = VideoTask.class.getSimpleName();
	
	private static final int TIMEOUT = 3 * 60 * 1000;//3 minutes
	
	private String mVideoUrl;
	private File mVideoFile;
	private String mVideoFileName;
	
	private Context mContext;
	
	private Listener mListener;
	
	public interface Listener {
		void onComplete(String filePath);
	}
	
	public VideoTask(String videoUrl, Context context, Listener listener) {
		mVideoUrl = videoUrl;
		mContext = context;
		mListener = listener;
	}
	
	public void deleteCorruptedVideoFile() {
		if (mVideoFileName != null && mVideoFile != null) {
			Logging.out(LOG_TAG, "Delete corrupted video file", LogLevel.DEBUG);
			mVideoFile.delete();
		}
	}
	
	private String detectFileName(String videoUrl) {
		String fileName = null;
		try {
			URL url = new URL(videoUrl);
			fileName = url.getFile();
			if (!TextUtils.isEmpty(fileName)) {
				fileName = fileName.replace("/", "");
				fileName = fileName.replace(".mp4", "");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return fileName;
	}
	
	private void deleteInvalidVideoFiles() {
		File parentDir = getParentDir();
		
		ArrayList<File> inFiles = new ArrayList<File>();
	    File[] files = parentDir.listFiles();
	    for (File file : files) {
	        if (!file.isDirectory()) {
	            if(file.getName().endsWith(".mp4")){
	                inFiles.add(file);
	                
	                File f = new File(file.getAbsolutePath());
	                long creationTime = f.lastModified();
	                long currentTime = System.currentTimeMillis();
	                
	                if ((creationTime + StaticParams.CACHED_VIDEO_LIFE_TIME < currentTime) ||
	                		(f.length() == 0)) {
	                	f.delete();
	                	Logging.out(LOG_TAG, "Deleted cached file: " + file.getAbsolutePath(), LogLevel.DEBUG);
	                }
	            }
	        }
	    }
	}
	
	private File getParentDir() {
		return mContext.getDir("LoopMeAds", Context.MODE_WORLD_READABLE);
	}
	
	private File checkFileNotExists(String filename) {
		File parentDir = getParentDir();
		Logging.out(LOG_TAG, "Cache dir: " + parentDir.getAbsolutePath(), LogLevel.DEBUG);

		File[] files = parentDir.listFiles();
		for (File file : files) {
			if (!file.isDirectory() && file.getName().startsWith(filename)) {
				return file;
			}
		}
		return null;
	}
	
	@Override
	public void run() {
		deleteInvalidVideoFiles();

		mVideoFileName = detectFileName(mVideoUrl);
        if (mVideoFileName == null) {
            complete(null);
            return;
        }
		File f = checkFileNotExists(mVideoFileName);
		if (f != null) {
			Logging.out(LOG_TAG, "Video file already exists", LogLevel.DEBUG);
			complete(f.getAbsolutePath());
			return;
		}
		
		createNewFile(mVideoFileName);

		if (mVideoFile != null) {
			InputStream stream = getVideoInputStream();
			
			String filePath = writeStreamToFile(stream);
			complete(filePath);
		}
	}
	
	private void createNewFile(String fileName) {
		try {
			File dir = getParentDir();
			mVideoFile = File.createTempFile(fileName, ".mp4", dir);
			
		} catch (IOException e) {
			Logging.out(LOG_TAG, e.getMessage(), LogLevel.DEBUG);
			complete(null);
		}
	}
	
	private void complete(String filePath) {
		if (mListener != null) {
			mListener.onComplete(filePath);
		}
	}
	
	private String writeStreamToFile(InputStream stream) {
		if (stream == null || !mVideoFile.exists()) {
			return null;
		}
		Logging.out(LOG_TAG, "Write to file", LogLevel.DEBUG);
		
		String filePath = null;
		try {
			FileOutputStream out = new FileOutputStream(mVideoFile);
			byte buffer[] = new byte[1024];
			int length = 0;
			while ((length = stream.read(buffer)) != -1) { 
				out.write(buffer,0, length);
			}
			out.close();
			filePath = mVideoFile.getAbsolutePath();
			
		} catch (IOException e) {
			e.printStackTrace();
			mVideoFile.delete();
		}
		return filePath;
	}
	
	private InputStream getVideoInputStream() {
		Logging.out(LOG_TAG, "Download video", LogLevel.DEBUG);
		InputStream inputStream = null;
		try {
			URL url = new URL(mVideoUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setReadTimeout(TIMEOUT);
			urlConnection.setConnectTimeout(TIMEOUT);
			inputStream = new BufferedInputStream(urlConnection.getInputStream());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return inputStream;
	}

}
