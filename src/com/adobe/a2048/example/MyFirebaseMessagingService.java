/*
Copyright 2019 Adobe
All Rights Reserved.

NOTICE: Adobe permits you to use, modify, and distribute this file in
accordance with the terms of the Adobe license agreement accompanying
it. If you have received this file from a source other than Adobe,
then your use, modification, or distribution of it requires the prior
written permission of Adobe. 
*/

package com.adobe.a2048.example;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.adobe.mobile.Analytics;
import com.adobe.mobile.Config;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.neolane.android.v1.Neolane;
import com.neolane.android.v1.NeolaneAsyncRunner;
import com.neolane.android.v1.NeolaneException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if(!remoteMessage.getData().isEmpty()) {
            Map<String, String> data = remoteMessage.getData();

            Log.d(TAG, "data payload: " + data);
            sendNotification(data);
            sendTracking(data);
        } else if (remoteMessage.getNotification() != null) {
            Map<String, String> data = new HashMap<>();
            data.put("title",remoteMessage.getNotification().getTitle());
            data.put("body",remoteMessage.getNotification().getBody());
            Log.d(TAG, "notification payload: " + data);
            sendNotification(data);
        } else {
            Log.d(TAG, "Notification was not processed");
        }
    }


    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Implement this method to send token to your app server.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(com.adobe.a2048.example.R.string.push_token_key), token).apply();

        if (AppConfig.CAMPAIGN_STANDARD_ENABLED) {
            Config.setPushIdentifier(token);
        } else if (AppConfig.CAMPAIGN_CLASSIC_ENABLED) {
            // Neolane SDK
            NeolaneAsyncRunner neolaneAs = new NeolaneAsyncRunner(Neolane.getInstance());
            // TODO: get real userKey
            String userKey = Double.valueOf(Math.random()).toString();
            neolaneAs.registerDevice(token, userKey, null, this, new NeolaneAsyncRunner.RequestListener() {
                public void onComplete(String arg0, Object state) {
                    Log.d(TAG, "successfully registered with ACC");
                }

                public void onNeolaneException(NeolaneException e, Object state) {
                    Log.e(TAG, "failure registering with ACC", e);
                }

                public void onIOException(IOException e, Object state) {
                    Log.e(TAG, "failure registering with ACC", e);
                }
            });
        }
    }

    private void sendTracking(Map<String, String> data) {
        String deliveryId = data.get("_dId");
        String messageId = data.get("_mId");

        if (AppConfig.CAMPAIGN_STANDARD_ENABLED) {
            HashMap<String, Object> contextData = new HashMap<>();

            if (deliveryId != null && messageId != null) {
                contextData.put("deliveryId", deliveryId);
                contextData.put("broadlogId", messageId);
                contextData.put("action", "1");
                Analytics.trackAction("tracking", contextData);
            }
        } else if (AppConfig.CAMPAIGN_CLASSIC_ENABLED) {
            // Neolane SDK
            // notify Neolane notification was received

            if (deliveryId != null && messageId != null) {
                NeolaneAsyncRunner nas = new NeolaneAsyncRunner(Neolane.getInstance());
                nas.notifyReceive(Integer.valueOf(messageId), deliveryId, new NeolaneAsyncRunner.RequestListener() {
                    public void onNeolaneException(NeolaneException e, Object arg1) {
                        Log.e(TAG, "failure notifying receipt to ACC", e);
                    }

                    public void onIOException(IOException e, Object arg1) {
                        Log.e(TAG, "failure notifying receipt to ACC", e);
                    }

                    public void onComplete(String arg0, Object arg1) {
                        Log.d(TAG, "successfully notified receipt to ACC");
                    }
                });
            }
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     */
    private void sendNotification(Map<String, String> data) {
        String title = data.get("title");
        String message = data.get("body");
        if(message==null) {
            message = data.get("message");
        }
        String deepLink = data.get("uri");

        if (title != null) {
            Log.d(TAG, "title: " + title);
        } else {
            title = "Default 2048 title";
        }

        if (message != null) {
            Log.d(TAG, "body: " + message);
        } else {
            message = "Default 2048 message";
        }

        Intent openIntent;
        Intent dismissIntent = new Intent(this, NotificationDismissedReceiver.class);

        /*
         * help from here to open different activity when push is clicked:
         * https://stackoverflow.com/questions/37407366/firebase-fcm-notifications-click-action-payload/39722606#39722606
         * https://stackoverflow.com/questions/13716723/open-application-after-clicking-on-notification/13716784#13716784
         */
        if (deepLink!=null) {
            openIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink));
        } else {
            openIntent = new Intent(this, CollectPIIActivity.class);
        }

        openIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //put the data map into the intent to track clickthroughs
        Bundle pushData = new Bundle();
        Set<String> keySet = data.keySet();
        for (String key : keySet) {
            pushData.putString(key, data.get(key));
        }

        openIntent.putExtras(pushData);
        dismissIntent.putExtras(pushData);

        /*
         * Talks about requests code need to be different otherwise will override the previous intent with same request code:
         * https://stackoverflow.com/questions/7370324/notification-passes-old-intent-extras/7370448#7370448
         *
         * Why we need FLAG_UPDATE_CURRENT:
         * https://stackoverflow.com/questions/1198558/how-to-send-parameters-from-a-notification-click-to-an-activity/1201239#1201239
         */
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent onDismissPendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, dismissIntent, 0);


        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, AppConfig.EXAMPLE_CHANNEL_ID);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(message);
        notificationBuilder.setSound(defaultSoundUri);
        notificationBuilder.setContentIntent(pendingIntent);
        //When the notification has been swiped or the "Clear all" has been clicked
        //The app will call this class so we can send a click Tracking to ACS
        notificationBuilder.setDeleteIntent(onDismissPendingIntent);

        String attachmentUrl = data.get("media-attachment-url");

        if(attachmentUrl != null) {
            Bitmap image = getBitmapFromURL(attachmentUrl);
            if (image != null) {
                notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(image));
            }
        }

        NotificationManager notificationManager =
                (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // https://developer.android.com/training/notify-user/channels
        // https://github.com/firebase/quickstart-android/blob/master/messaging/app/src/main/java/com/google/firebase/quickstart/fcm/java/MyFirebaseMessagingService.java
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(AppConfig.EXAMPLE_CHANNEL_ID,
                    AppConfig.EXAMPLE_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        if (notificationManager != null) {
            notificationManager.notify(getCurrentNotificationId(), notificationBuilder.build());
        }
    }

    private Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

   private int getCurrentNotificationId() {
        String notificationNumberPreference = "com.adobe.a2048.v2.notificationNumber";

        /*
         * display multiple notifications:
         * http://stackoverflow.com/questions/18102052/how-to-display-multiple-notifications-in-android/26465254#26465254
         */
        SharedPreferences prefs = getSharedPreferences(MyFirebaseMessagingService.class.getSimpleName(), Context.MODE_PRIVATE);
        int notificationNumber = prefs.getInt(notificationNumberPreference, 0);

        if (notificationNumber > 100) {
            notificationNumber = 0;
        }

        SharedPreferences.Editor editor = prefs.edit();
        notificationNumber++;
        editor.putInt(notificationNumberPreference, notificationNumber);
        editor.apply();

        return notificationNumber;
   }
}

