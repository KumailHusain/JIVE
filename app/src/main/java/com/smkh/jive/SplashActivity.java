package com.smkh.jive;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(10122);
        Window w = getWindow(); // in Activity's onCreate() for instance
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        Thread thread = new Thread() {
            public void run() {
                SharedPreferences prefs = getSharedPreferences("SharedPreferences", MODE_PRIVATE);
                Boolean isTokenSent = prefs.getBoolean("isSentToken", false);
                if (!isTokenSent) {
                    boolean alarmUp = (PendingIntent.getBroadcast(getApplicationContext(), 0,
                            new Intent("com.smkh.jive.MyReceiver"),
                            PendingIntent.FLAG_NO_CREATE) != null);
                    if (!alarmUp) {
                        Intent intent = new Intent("com.smkh.jive.MyReceiver");
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
                        AlarmManager am= (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        am.setExact(AlarmManager.ELAPSED_REALTIME, 600000, pendingIntent);
                    }
                }
                boolean alarmUp = (PendingIntent.getBroadcast(getApplicationContext(), 0,
                        new Intent("com.smkh.jive.HostUpdaterReceiver"),
                        PendingIntent.FLAG_NO_CREATE) != null);
                if (!alarmUp) {
                    Intent intent = new Intent(getApplicationContext(), HostUpdaterService.class);
                    startService(intent);
                }

            }
        };
        thread.start();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }
}
