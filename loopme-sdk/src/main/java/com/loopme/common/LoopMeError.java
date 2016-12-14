package com.loopme.common;

import android.text.TextUtils;

public class LoopMeError {

    private String mErrorMessage;

    public LoopMeError(String errMessage) {
        if (errMessage != null && errMessage.length() > 0) {
            mErrorMessage = errMessage;
        } else {
            mErrorMessage = "Unknown error";
        }
    }

    public String getMessage() {
        return mErrorMessage;
    }
}
