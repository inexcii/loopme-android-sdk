package com.loopme;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.net.Uri;

import java.net.URL;

import android.util.Log;

public class EventManager {

	private static final String URL = "loopme.me/api/v2/events";
	
	private static final String EVENT_TYPE = "et";
	private static final String R = "r";
	private static final String ID = "id";
	
	private String build(String token) {
		String str = URL;
		List<String> list = Arrays.asList(str.split("/"));
		
		Uri.Builder builder = new Uri.Builder();
		builder.scheme("http");
		
		for (String s : list) {
			if (list.indexOf(s) == 0) {
				builder.authority(s);
			} else {
				builder.appendPath(s);
			}
		}
		
		builder.appendQueryParameter(EVENT_TYPE, "SDK_FEEDBACK")
		.appendQueryParameter(R, "1")
		.appendQueryParameter(ID, token)
		.build();
		
		Log.d("debug", "url event: " + builder.toString());
		return builder.toString();
	}
	
	public void trackSdkEvent(String token) {
		ExecutorService executor = Executors.newCachedThreadPool();
		final String eventUrl = build(token);
        executor.submit(new Runnable() {
			
			@Override
			public void run() {
				URL url = null;
		        HttpURLConnection urlConnection = null;
		        try {
		            url = new URL(eventUrl);
		            urlConnection = (HttpURLConnection) url.openConnection();
		            urlConnection.getInputStream();
		            urlConnection.disconnect();
		        } catch (MalformedURLException e) {
		            e.printStackTrace();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
			}
		});
	}
}
