package com.project.aifoto;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String messageTitle = remoteMessage.getData().get("title");
        String messageBody = remoteMessage.getData().get("body");

        String postId = remoteMessage.getData().get("post_id");
        String userId = remoteMessage.getData().get("to_user_id");
        String postDesc = remoteMessage.getData().get("post_desc");
        String postImageThumbUrl = remoteMessage.getData().get("post_image_url");

        Intent resultIntent = new Intent(this, CommentsActivity.class);
        resultIntent.putExtra("postId", postId);
        resultIntent.putExtra("user_id", userId);
        resultIntent.putExtra("post_desc", postDesc);
        resultIntent.putExtra("post_image_url", postImageThumbUrl);

        Log.d("Post Id: ", postId);
        Log.d("User Id: ", userId);
        Log.d("post_desc", postDesc);
        Log.d("post_image_url", postImageThumbUrl);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        builder.setContentIntent(resultPendingIntent);

        int mNotificationId = (int) System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(getString(R.string.default_notification_channel_id), "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(mNotificationId, builder.build());
    }
}
