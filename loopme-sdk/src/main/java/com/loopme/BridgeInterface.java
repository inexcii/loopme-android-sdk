package com.loopme;

public interface BridgeInterface {

	//webview commands
    void setWebViewState(AdView.WebviewState state);
	
	//video commands
	void setVideoState(VideoState state);
	void setVideoDuration(int duration);
	void setVideoCurrentTime(int currentTime);
	void setVideoMute(boolean mute);
	
	void sendNativeCallFinished();
}
