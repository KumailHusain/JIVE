package com.smkh.jive;

import android.content.Intent;
import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Kumail on 16-Jan-18.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        SharedPreferences.Editor editor = getSharedPreferences("SharedPreferences", MODE_PRIVATE).edit();
        editor.putString("token", refreshedToken);
        editor.putBoolean("isSentToken", false);
        editor.commit();
        Thread thread = new Thread() {
            @Override
            public void run() {
                Intent service = new Intent(getApplicationContext(), TokenServerSyncService.class);
                startService(service);
            }
        };
        thread.start();
    }
}
