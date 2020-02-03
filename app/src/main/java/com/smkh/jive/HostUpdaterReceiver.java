package com.smkh.jive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HostUpdaterReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, HostUpdaterReceiver.class);
        context.startService(intent1);
    }
}
