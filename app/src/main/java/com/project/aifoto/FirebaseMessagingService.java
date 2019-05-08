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
        //super.onMessageReceived(remoteMessage);



        //if(remoteMessage.getData().size()>0) {

//            String messageTitle = remoteMessage.getNotification().getTitle();
//            String messageBody = remoteMessage.getNotification().getBody();
//            String click_action = remoteMessage.getNotification().getClickAction();

        String messageTitle = remoteMessage.getData().get("title");
        String messageBody = remoteMessage.getData().get("body");
        //String click_action = remoteMessage.getNotification().getClickAction();

            String postId = remoteMessage.getData().get("post_id");
            String userId = remoteMessage.getData().get("to_user_id");

            Intent resultIntent = new Intent(this, CommentsActivity.class);
            resultIntent.putExtra("postId", postId);
            resultIntent.putExtra("user_id",userId);

            Log.d("Post Id: ", postId);
            Log.d("User Id: ", userId);

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
      //  }

    }
}
