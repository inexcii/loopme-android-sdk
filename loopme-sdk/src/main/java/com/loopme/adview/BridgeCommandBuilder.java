package com.loopme.adview;

import com.loopme.constants.VideoState;
import com.loopme.constants.WebviewState;

/**
 * Class helper to build LoopMe javascript bridge commands
 */
public class BridgeCommandBuilder {

    private static final String PREFIX = "javascript:window.L.bridge.set";
    private static final String PREFIX_FUNCTION_BEGIN = "javascript:(function(){ ";
    private static final String PREFIX_360 = "javascript:window.L.track";

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

    String videoMute(boolean mute) {
        StringBuilder builder = new StringBuilder();
        builder.append(PREFIX)
                .append("('video', {mute: ")
                .append("'")
                .append(mute)
                .append("'});");
        return builder.toString();
    }

    String videoCurrentTime(int time) {
        StringBuilder builder = new StringBuilder();
        builder.append(PREFIX)
                .append("('video', {currentTime: ")
                .append("'")
                .append(time)
                .append("'});");
        return builder.toString();
    }

    String videoDuration(int time) {
        StringBuilder builder = new StringBuilder();
        builder.append(PREFIX)
                .append("('video', {duration: ")
                .append("'")
                .append(time)
                .append("'});");
        return builder.toString();
    }

    String videoState(int state) {
        StringBuilder builder = new StringBuilder();
        builder.append(PREFIX)
                .append("('video', {state: ")
                .append("'")
                .append(VideoState.toString(state))
                .append("'});");
        return builder.toString();
    }

    public String webviewState(int state) {
        StringBuilder builder = new StringBuilder();
        builder.append(PREFIX)
                .append("('webview', {state: ")
                .append("'")
                .append(WebviewState.toString(state))
                .append("'});");
        return builder.toString();
    }

    String fullscreenMode(boolean b) {
        StringBuilder builder = new StringBuilder();
        builder.append(PREFIX)
                .append("('webview', {fullscreenMode: ")
                .append(b)
                .append("});");
        return builder.toString();
    }

    String event360(String event) {
        StringBuilder builder = new StringBuilder();
        builder.append(PREFIX_360)
                .append("({eventType: 'INTERACTION', customEventName: 'video360&mode=")
                .append(event)
                .append("'});");
        return builder.toString();
    }

    public String unMuteAndPlayVideo() {
        StringBuilder builder = new StringBuilder();
        builder.append(PREFIX_FUNCTION_BEGIN)
                .append(playVideo())
                .append(unMuteVideo())
                .append(" })()");
        return builder.toString();
    }

    private String playVideo() {
        return "document.getElementsByTagName('video')[0].play();";
    }

    private String unMuteVideo() {
        StringBuilder builder = new StringBuilder();
        builder.append("setTimeout(function(){")
                .append("document.getElementById('gwd-video_1').volume = 1;")
                .append("document.getElementById('gwd-video_1').muted = false;")
                .append(deleteUnMuteAttribute())
                .append("}, 0);},0)");
        return builder.toString();
    }

    private String deleteUnMuteAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append("setTimeout(function(){")
                .append("document.getElementById('gwd-video_1').removeAttribute('muted');");
        return builder.toString();
    }
}

