package com.tutorial.chatapps;

/**
 * Created by mibe on 7/25/2016.
 */
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    public LocalBroadcastManager mBroadcaster;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = "";
        String type = "";
        Log.d("data: ", data.toString());
        message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        Intent i = new Intent("MyCustom");
        i.setAction("MyCustom");
        i.putExtra("Message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        //mBroadcaster.send(i);
        //i
        //.receiveChatMessage(message);
//        sendNotification(message, type);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    @Override
    public void onCreate() {
        super.onCreate();
        mBroadcaster = LocalBroadcastManager.getInstance(this);
    }
    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message, String type) {
        Intent intent = new Intent(this, MainActivity.class);

        intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        //        .setSmallIcon(R.mipmap.ic_launcher)
          //      .setContentTitle(getString(R.string.app_short_name))
            //    .setContentText(message)
              //  .setAutoCancel(true)
               // .setSound(defaultSoundUri)
               // .setContentIntent(pendingIntent);
        notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}