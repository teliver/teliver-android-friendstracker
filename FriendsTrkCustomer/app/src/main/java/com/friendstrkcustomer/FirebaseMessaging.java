package com.friendstrkcustomer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.GsonBuilder;
import com.teliver.sdk.core.Teliver;
import com.teliver.sdk.models.NotificationData;

import java.util.Map;


public class FirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (Teliver.isTeliverPush(remoteMessage)) {
            Map<String, String> pushData = remoteMessage.getData();
            final NotificationData data = new GsonBuilder().create().fromJson(pushData.get("description"), NotificationData.class);

            NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
            notification.setContentTitle("Teliver");
            notification.setContentText(data.getMessage());
            notification.setSmallIcon(R.drawable.ic_notification_icon);

            notification.setStyle(new NotificationCompat.BigTextStyle().bigText(data.getMessage()).setBigContentTitle("Teliver"));
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("msg", data.getMessage());
            intent.putExtra("tracking_id", data.getTrackingID());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            notification.setContentIntent(pendingIntent);
            notification.setAutoCancel(true);
            notification.setPriority(Notification.PRIORITY_MAX);
            notification.setDefaults(Notification.DEFAULT_ALL);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, notification.build());
        }
    }
}
