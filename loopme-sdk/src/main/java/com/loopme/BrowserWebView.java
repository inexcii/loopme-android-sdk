package com.loopme;

import android.content.Context;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.PluginState;

public class BrowserWebView extends WebView{

	public BrowserWebView(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		WebSettings webSettings = getSettings();
		webSettings.setJavaScriptEnabled(true);

		webSettings.setPluginState(PluginState.ON);

		webSettings.setBuiltInZoomControls(true);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setUseWideViewPort(true);
		setInitialScale(1);
	}
}
