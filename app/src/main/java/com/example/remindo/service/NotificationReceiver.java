package com.example.remindo.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.remindo.service.MyBroadcastReceiver;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("notification_title");
        String time = intent.getStringExtra("notification_time");

        // Panggil metode notification di layanan Anda
        Intent serviceIntent = new Intent(context, MyBroadcastReceiver.class);
        serviceIntent.putExtra("notification_title", title);
        serviceIntent.putExtra("notification_time", time);
        context.startService(serviceIntent);
    }
}