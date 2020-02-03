package com.smkh.jive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, TokenServerSyncService.class);
        context.startService(intent1);
    }
}
