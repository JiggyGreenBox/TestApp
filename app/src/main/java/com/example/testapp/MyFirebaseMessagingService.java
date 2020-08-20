package com.example.testapp;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

//
public class MyFirebaseMessagingService extends FirebaseMessagingService {


    public static final String UPDATE_FCM_TOKEN = "UPDATE_FCM_TOKEN";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Log.e("FIREBASE", remoteMessage.toString());
//        Log.e("FIREBASE", "Message Notification Body: " + remoteMessage.getNotification().getBody());

        remoteMessage.getData();
        Map<String, String> data = remoteMessage.getData();
//        int questionId = Integer.parseInt(data.get("questionId").toString());
//        String questionTitle = data.get("questionTitle").toString();
//        String userDisplayName = data.get("userDisplayName").toString();
        String message = data.get("message");
        Log.e("FIREBASE", "Message Notification Body: " + message);

        createNotification();
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.e("FIREBASE TOKEN", s);
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("fb", s).apply();

        Intent intent = new Intent(UPDATE_FCM_TOKEN);
        sendBroadcast(intent);
    }

    public static String getToken(Context context) {
        return context.getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty");
    }

    private void createNotification() {

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, TransactionVideoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, AppConstants.CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.paytm_assist_icon)
                .setContentTitle("My notification")
                .setContentText("Much longer text that cannot fit one line...")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Much longer text that cannot fit one line..."))
                //.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                // heads up notification attempt
                .setPriority(Notification.PRIORITY_HIGH)
                .setVibrate(new long[0]);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        //notificationManager.notify(notificationId, builder.build());
        notificationManager.notify(5, builder.build());
    }
}
