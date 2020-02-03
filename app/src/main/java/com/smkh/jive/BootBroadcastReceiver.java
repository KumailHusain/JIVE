package com.smkh.jive;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Kumail on 05-Mar-18.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context pContext, Intent intentx) {
        SharedPreferences prefs = pContext.getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        Boolean isTokenSent = prefs.getBoolean("isSentToken", false);
        if (!isTokenSent) {
            boolean alarmUp = (PendingIntent.getBroadcast(pContext, 0,
                    new Intent("com.smkh.jive.MyReceiver"),
                    PendingIntent.FLAG_NO_CREATE) != null);
            if (!alarmUp) {
                Intent intent = new Intent("com.smkh.jive.MyReceiver");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(pContext, 0, intent, 0);
                AlarmManager am= (AlarmManager) pContext.getSystemService(Context.ALARM_SERVICE);
                am.setExact(AlarmManager.ELAPSED_REALTIME, 600000, pendingIntent);
            }
        }
        Intent intent = new Intent("com.smkh.jive.HostUpdaterReceiver");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(pContext, 0, intent, 0);
        AlarmManager am= (AlarmManager) pContext.getSystemService(Context.ALARM_SERVICE);
        am.setExact(AlarmManager.ELAPSED_REALTIME, 300000, pendingIntent);
    }
}