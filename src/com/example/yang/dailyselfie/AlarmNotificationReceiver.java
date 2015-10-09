package com.example.yang.dailyselfie;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmNotificationReceiver extends BroadcastReceiver{

    // Notification ID to allow for future updates
    private static final int MY_NOTIFICATION_ID = 1;

    // Notification Text Elements
    private final CharSequence tickerText = "Selfie Time!";
    private final CharSequence contentTitle = "Daily Selfie";
    private final CharSequence contentText = "Time for another selfie!";

    // Notification Action Elements
    private Intent mNotificationIntent;
    private PendingIntent mContentIntent;

    @Override
    public void onReceive(Context context, Intent intent) {

        // The Intent to be used when the user clicks on the Notification View
        mNotificationIntent = new Intent(context, MainActivity.class);

        // The PendingIntent that wraps the underlying Intent
        mContentIntent = PendingIntent.getActivity(context, 0,
                mNotificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
        
        // Build the notification
        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_photo_camera_white_24dp)
                .setAutoCancel(true)
                .setTicker(tickerText)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(mContentIntent);

        // Get the NotificationManager
        NotificationManager mNotificationManager = (NotificationManager)context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // Pass the Notification to the NotificationManager
        mNotificationManager.notify(MY_NOTIFICATION_ID, notificationBuilder.build());
    }
}
