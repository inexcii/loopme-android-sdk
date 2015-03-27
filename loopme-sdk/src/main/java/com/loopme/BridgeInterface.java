package com.loopme;

public interface BridgeInterface {

	//webview commands
	void onAppear();
	void onDisappear();
	void onHidden();
	
	//video commands
	void setVideoState(VideoState state);
	void setVideoDuration(int duration);
	void setVideoCurrentTime(int currentTime);
	void setVideoMute(boolean mute);
	
	void sendNativeCallFinished();
}
