package com.sample.foo.simplewidget;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import classes.UpdateIntentService;


/**
 * Created by C.limbachiya on 4/22/2016.
 */
public class MyBroadcastReceiver extends BroadcastReceiver {

    int mId = 101;

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Receiver called", Toast.LENGTH_SHORT).show();

        if (null != intent) {
            String type = intent.getStringExtra("BROADCAST_TYPE");

            //Intent updateIntent = new Intent(context, WidgetUpdateService.class);
            Intent updateIntent = new Intent(context, UpdateIntentService.class);
            context.startService(updateIntent);
        }
    }

}
