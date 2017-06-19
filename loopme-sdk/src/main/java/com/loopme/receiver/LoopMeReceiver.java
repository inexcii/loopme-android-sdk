package com.loopme.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.loopme.common.StaticParams;

import java.util.Calendar;

public class LoopMeReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "data";

    @Override
    public void onReceive(final Context context, Intent intent) {
//        Logging.out(LOG_TAG, "onReceive");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        final SharedPreferences sp = context.getSharedPreferences(StaticParams.LOOPME_PREFERENCES,
                Context.MODE_PRIVATE);
        String oldId = sp.getString(StaticParams.VIEWER_TOKEN, "");

//        DataCollector.getInstance(context).onReceive(oldId);
    }
}
