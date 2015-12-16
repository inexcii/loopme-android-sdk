package com.loopme;

import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.loopme.debugging.ErrorTracker;

public class AdViewChromeClient extends WebChromeClient {

    private static final String LOG_TAG = AdViewChromeClient.class.getSimpleName();

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        if (consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.ERROR ||
                consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.WARNING) {

            Logging.out(LOG_TAG, "Console Message: " + consoleMessage.message());
        }
        if (consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
            ErrorTracker.post("Error from console: " + consoleMessage.message());
        }
        return super.onConsoleMessage(consoleMessage);
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
    }
}
