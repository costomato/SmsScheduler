package com.sms.smsscheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyTask extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent intent1 = new Intent("myBroadcast");
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.sendBroadcast(intent1);

    }
}
