package project.android.com.connect24;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService
{

    private NotificationCompat.Builder mBuilder;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";

    public String notif_title;
    public String notif_body;
    public  String click_action;
    public String from_user_id;
    public int notification_id;

    NotificationManager notificationManager;


    //Triggering when a friend request has been sent

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        super.onMessageReceived(remoteMessage);



        if(remoteMessage.getData().size()>0)
        {

            //Retreiving notification from firebase backend server (function) and showing it as foreground notification

            notif_title = remoteMessage.getNotification().getTitle();
            notif_body = remoteMessage.getNotification().getBody();
            click_action = remoteMessage.getNotification().getClickAction();
            from_user_id = remoteMessage.getData().get("from_user_id");//retreiving the user id from messaging service so we can open profile activity and show the user

             Log.e("from_userid",from_user_id);

                notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                notification_id = (int) System.currentTimeMillis();


                Intent resultIntent = new Intent(click_action);

                resultIntent.putExtra("user_id", from_user_id);

                PendingIntent resultPendingIntent = PendingIntent.getActivity(
                        this,
                        0 /* Request code */, resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);


                mBuilder = new NotificationCompat.Builder(this);
                mBuilder.setSmallIcon(R.mipmap.ic_launcher);
                mBuilder.setContentTitle(notif_title)
                        .setContentText(notif_body)
                        .setAutoCancel(true)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setContentIntent(resultPendingIntent);
                ;


                //WHEN API 26>=

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "GENERAL NOTIFICATIONS", importance);
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.RED);
                    notificationChannel.enableVibration(true);
                    notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    assert notificationManager != null;
                    mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
                    notificationManager.createNotificationChannel(notificationChannel);
                }

                assert notificationManager != null;
                notificationManager.notify(0, mBuilder.build());

        }
    }

}

