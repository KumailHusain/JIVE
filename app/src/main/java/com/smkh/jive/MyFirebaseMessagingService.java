package com.smkh.jive;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by Kumail on 16-Jan-18.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            if (data.containsKey("pdflistarray")) {
                SharedPreferences preferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("pdflistarray", data.get("pdflistarray"));
                editor.apply();
                Boolean isNoisy = preferences.getBoolean("isNoisy", true); //are notifications enabled
                String type = data.get("isNoisy");
                if (type.equals("isNoisy") && isNoisy) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("fromnotification", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 1011, intent,
                            PendingIntent.FLAG_ONE_SHOT);
                    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                            //.setSmallIcon
                            .setSmallIcon(R.mipmap.ic_small_icon)
                            .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.logo))
                            .setColor(Color.rgb(14, 9, 41))
                            .setContentTitle("JIVE")
                            .setContentText("There's a new magazine available!")
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(10122, notificationBuilder.build());
                }
            } else if (data.containsKey("custommessage")) {
                SharedPreferences preferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);
                Boolean isNoisy = preferences.getBoolean("isNoisy", true);
                if (isNoisy) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 1011, intent,
                            PendingIntent.FLAG_ONE_SHOT);
                    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                            //.setSmallIcon
                            .setSmallIcon(R.mipmap.ic_small_icon)
                            .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.logo))
                            .setColor(Color.rgb(14, 9, 41))
                            .setContentTitle("JIVE")
                            .setContentText(data.get("custommessage"))
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(10123, notificationBuilder.build());
                }
            }
        }
    }
}
