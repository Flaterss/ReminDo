package com.example.remindo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationDeleteReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationDeleteReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Notification deleted by user.");
    }
}
