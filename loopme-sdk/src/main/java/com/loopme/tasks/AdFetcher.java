package com.loopme.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.loopme.AdParams;
import com.loopme.LoopMeAdHolder;
import com.loopme.Logging;
import com.loopme.LoopMeError;
import com.loopme.ResponseParser;
import com.loopme.Utils;
import com.loopme.Logging.LogLevel;

public class AdFetcher implements Runnable {
	
	private static final String LOG_TAG = AdFetcher.class.getSimpleName();

	private final String mRequestUrl;
	private Listener mListener;
	
	private String mAppKey;
	
	//timeout for response from server 20 seconds
	private static final int TIMEOUT = 20000;
	private int mLoopMeError = -1;
	
	public interface Listener {
		void onComplete(AdParams params, int error);
	}
	
	public AdFetcher(String requestUrl, Listener listener, String appkey) {
		mRequestUrl = requestUrl;
		mListener = listener;
		mAppKey = appkey;
	}
	
	@Override
	public void run() {
		String result = getResponse(mRequestUrl);
		
		if (result == null) {
			complete(null, mLoopMeError);
		} else {
			ResponseParser parser = new ResponseParser(new ResponseParser.Listener() {
				
				@Override
				public void onParseError(String message) {
					complete(null, LoopMeError.RESPONSE_PARSING);
				}
			}, LoopMeAdHolder.getAd(mAppKey).getAdFormat());
			AdParams adParams = parser.getAdParams(result);
			if (adParams != null) {
				complete(adParams, -1);
			}
		}
	}
	
	private void complete(final AdParams params, final int error) {
		if (mListener != null) {
			mListener.onComplete(params, error);
		}
	}
	
	private String getResponse(String url) {
		String result = null;
		try {
			HttpParams httpParameters = new BasicHttpParams();

            HttpConnectionParams.setConnectionTimeout(httpParameters, TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpParameters, TIMEOUT);

			HttpClient httpClient = new DefaultHttpClient(httpParameters);
			httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, System.getProperty("http.agent"));
			HttpGet httpGet = new HttpGet(url);

			String type = LoopMeAdHolder.getAd(mAppKey).getAdFormat().toString();
			Logging.out(LOG_TAG, type + " loads ad with URL: " + url, LogLevel.DEBUG);
			
			HttpResponse responce = httpClient.execute(httpGet);
			HttpEntity entity = responce.getEntity();
			
			int statusCode = responce.getStatusLine().getStatusCode();
			
			switch (statusCode) {
			case HttpURLConnection.HTTP_NO_CONTENT:
				mLoopMeError = LoopMeError.NO_ADS_FOUND;
				break;

			case HttpURLConnection.HTTP_NOT_FOUND:
				mLoopMeError = LoopMeError.INVALID_APPKEY;
				break;

			case HttpURLConnection.HTTP_OK:
				InputStream is = entity.getContent();
				result = Utils.getStringFromStream(is);
				break;

			default:
				mLoopMeError = LoopMeError.UNKNOWN_SERVER_CODE;
				break;
			}
			
		} catch (ConnectTimeoutException ex) {
			mLoopMeError = LoopMeError.RESPONSE_PROCESSING;
		} catch (SocketTimeoutException ex) {
			mLoopMeError = LoopMeError.RESPONSE_PROCESSING;
		} catch (IOException ex) {
			mLoopMeError = LoopMeError.RESPONSE_PROCESSING;
		}
		return result;
	}
}
