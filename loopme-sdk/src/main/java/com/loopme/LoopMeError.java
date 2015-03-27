package com.loopme;

import com.loopme.Logging.LogLevel;

public class LoopMeError {
	
	/**
	 * Missing or invalid appkey
	 */
	public static final int INVALID_APPKEY = 0;
	
	/**
	 * No ads found
	 */
	public static final int NO_ADS_FOUND = 1;
	
	/**
	 * Ad processing timed out
	 */
	public static final int TIMEOUT = 2;
	
	/**
	 * Failed to process ad
	 */
	public static final int SPECIFIC_HOST = 3;
	
	/**
	 * Could not process ad: wrong format
	 */
	public static final int INCORRECT_FORMAT = 4;
	
	/**
	 * Failed to resolve URL
	 */
	public static final int URL_RESOLVE = 5;
	
	/**
	 * Error writing to disk
	 */
	public static final int WRITING_TO_DISK = 6;
	
	/**
	 * Not supported Android version. Expected Android 4.0+
	 */
	public static final int UNSUPPORTED_ANDROID_VERSION = 7;
	
	/**
	 * No internet connection
	 */
	public static final int NO_CONNECTION = 8;
	
	/**
	 * Broken response
	 */
	public static final int BROKEN_RESPONSE = 9;
	
	/**
	 * No valid ads found
	 */
	public static final int NO_VALID_ADS_FOUND = 10;
	
	/**
	 * Error during building ad request url
	 */
	public static final int REQUEST_URL = 11;
	
	/**
	 * Unknown status code from server
	 */
	public static final int UNKNOWN_SERVER_CODE = 12;
	
	/**
	 * Unable to process response
	 */
	public static final int RESPONSE_PROCESSING = 13;
	
	/**
	 * Unable to parse response
	 */
	public static final int RESPONSE_PARSING = 14;
	
	/**
	 * Error during video loading
	 */
	public static final int VIDEO_LOADING = 15;
	
	/**
	 * Error during loading html
	 */
	public static final int HTML_LOADING = 16;
	
	public static String getCodeMessage(int code) {
		String errorMessage = null;

		switch (code) {
		case INVALID_APPKEY:
			errorMessage = "Missing or invalid appkey";
			break;

		case NO_ADS_FOUND:
			errorMessage = "No ads found";
			break;
			
		case TIMEOUT:
			errorMessage = "Ad processing timed out";
			break;
			
		case SPECIFIC_HOST:
			errorMessage = "Failed to process ad";
			break;
			
		case INCORRECT_FORMAT:
			errorMessage = "Could not process ad: wrong format";
			break;
			
		case URL_RESOLVE:
			errorMessage = "Failed to resolve URL";
			break;
			
		case WRITING_TO_DISK:
			errorMessage = "Error writing to disk";
			break;
			
		case UNSUPPORTED_ANDROID_VERSION:
			errorMessage = "Not supported Android version. Expected Android 4.0+";
			break;
			
		case NO_CONNECTION:
			errorMessage = "No internet connection";
			break;
			
		case BROKEN_RESPONSE:
			errorMessage = "Broken response";
			break;
			
		case NO_VALID_ADS_FOUND:
			errorMessage = "No valid ads found";
			break;
			
		case REQUEST_URL:
			errorMessage = "Error during building ad request url";
			break;
			
		case UNKNOWN_SERVER_CODE:
			errorMessage = "Unknown status code from server";
			break;
			
		case RESPONSE_PROCESSING:
			errorMessage = "Unable to process response";
			break;
			
		case RESPONSE_PARSING:
			errorMessage = "Unable to parse response";
			break;
			
		case VIDEO_LOADING:
			errorMessage = "Error during video loading";
			break;
			
		case HTML_LOADING:
			errorMessage = "Error during loading html";
			break;

		default:
			errorMessage = "Error with status code " + code;
		}
		return errorMessage;
	}
}
