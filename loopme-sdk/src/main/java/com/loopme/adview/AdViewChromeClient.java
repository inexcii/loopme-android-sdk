package com.loopme.adview;

import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.loopme.common.Logging;
import com.loopme.debugging.ErrorLog;
import com.loopme.debugging.ErrorType;

public class AdViewChromeClient extends WebChromeClient {

    private static final String LOG_TAG = AdViewChromeClient.class.getSimpleName();

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        if (consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.ERROR ||
                consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.WARNING) {

            Logging.out(LOG_TAG, "Console Message: " + consoleMessage.message());
        }
        if (consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
            ErrorLog.post("Error from js console: " + consoleMessage.message(), ErrorType.JS);
        }
        return super.onConsoleMessage(consoleMessage);
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
    }
}
