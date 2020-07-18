package com.example.proyectosig.channel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.PrecomputedText;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.proyectosig.R;

public class NotificationHelper extends ContextWrapper {
    private static final String CHANNEL_ID="com.example.proyectosig";
    private static final String CHANNEL_NAME="proyectoSig";

    private NotificationManager manager;
    public NotificationHelper(Context base){
        super(base);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            createChannel();
        }


    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel(){
        NotificationChannel notificationChannel=new NotificationChannel(
                CHANNEL_ID,CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLightColor(Color.GRAY);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManaganer().createNotificationChannel(notificationChannel);
    }
    public NotificationManager getManaganer(){
        if(manager==null){
            manager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        }
        return manager;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotification(String title, String body, PendingIntent intent, Uri sounUri){
        return new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(sounUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_car_taxi)
                .setStyle(new Notification.BigTextStyle().bigText(body).setBigContentTitle(title));
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotificationActions(String title, String body, Uri sounUri,Notification.Action aceptAction,Notification.Action cancelAction){
        return new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(sounUri)
                .setSmallIcon(R.drawable.ic_car_taxi)
                .addAction(aceptAction)
                .addAction(cancelAction)
                .setStyle(new Notification.BigTextStyle().bigText(body).setBigContentTitle(title));
    }

    public NotificationCompat.Builder getNotificationOldAPI(String title, String body, PendingIntent intent, Uri sounUri){
        return new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(sounUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_car_taxi)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title));
    }
    public NotificationCompat.Builder getNotificationOldAPIActions(String title, String body, Uri sounUri,
                                                                   NotificationCompat.Action aceptAction,NotificationCompat .Action cancelAction){
        return new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(sounUri)

                .setSmallIcon(R.drawable.ic_car_taxi)
                .addAction(aceptAction)
                .addAction(cancelAction)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title));
    }
}
