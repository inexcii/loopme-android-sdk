package com.loopme;

import com.loopme.AdView.WebviewState;

/**
 * Class helper to build LoopMe javascript bridge commands 
 */
class BridgeCommandBuilder {

	private static final String PREFIX = "javascript:window.L.bridge.set";
	
	String isNativeCallFinished(boolean b) {
		StringBuilder builder = new StringBuilder();
		builder.append(PREFIX)
		.append("('webview', {isNativeCallFinished: ")
		.append("'")
		.append(b)
		.append("'});");
		return builder.toString();
	}
	
	String shake(boolean b) {
		StringBuilder builder = new StringBuilder();
		builder.append(PREFIX)
		.append("('webview', {shake: ")
		.append("'")
		.append(b)
		.append("'});");
		return builder.toString();
	}
	
	String videoMute(boolean b) {
		StringBuilder builder = new StringBuilder();
		builder.append(PREFIX)
		.append("('video', {mute: ")
		.append(b)
		.append("});");
		return builder.toString();
	}
	
	String videoCurrentTime(int time) {
		StringBuilder builder = new StringBuilder();
		builder.append(PREFIX)
		.append("('video', {currentTime: ")
		.append(time)
		.append("});");
		return builder.toString();
	}
	
	String videoDuration(int time) {
		StringBuilder builder = new StringBuilder();
		builder.append(PREFIX)
		.append("('video', {duration: ")
		.append(time)
		.append("});");
		return builder.toString();
	}
	
	String videoState(VideoState state) {
		StringBuilder builder = new StringBuilder();
		builder.append(PREFIX)
		.append("('video', {state: ")
		.append("'")
		.append(state.toString())
		.append("'});");
		return builder.toString();
	}
	
	String webviewState(WebviewState state) {
		StringBuilder builder = new StringBuilder();
		builder.append(PREFIX)
		.append("('webview', {state: ")
		.append("'")
		.append(state.toString())
		.append("'});");
		return builder.toString();
	}
}
