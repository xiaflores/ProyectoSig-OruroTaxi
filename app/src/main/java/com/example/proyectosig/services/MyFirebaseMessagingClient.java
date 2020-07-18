package com.example.proyectosig.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;

import com.example.proyectosig.R;
import com.example.proyectosig.channel.NotificationHelper;
import com.example.proyectosig.receivers.AceptReceiver;
import com.example.proyectosig.receivers.CancelReceiver;
import com.google.android.gms.common.internal.ICancelToken;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Calendar;
import java.util.Map;

public class MyFirebaseMessagingClient extends FirebaseMessagingService {
    private static final int NOTIFICATION_CODE=100;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        RemoteMessage.Notification notification=remoteMessage.getNotification();
        Map<String,String> data= remoteMessage.getData();
        String title=data.get("title");
        String body=data.get("body");
        if (title!=null){
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                if(title.contains("Solicitud de servicio")){
                    String idClient=data.get("idClient");
                    showNotificationAPIoreoActions(title,body,idClient);
                }
                else {
                    showNotificationAPIoreo(title,body);
                }

            }
            else {
                if(title.contains("Solicitud de servicio")){
                    String idClient=data.get("idClient");
                    showNotificationActions(title,body,idClient);
                }
                else {
                    showNotification(title,body);
                }

            }
        }
    }

    private void showNotification(String title,String body) {
        PendingIntent intent=PendingIntent.getActivity(getBaseContext(),0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        Uri soud= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper=new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder=notificationHelper.getNotificationOldAPI(title,body,intent,soud);
        notificationHelper.getManaganer().notify(1,builder.build());
    }

    private void showNotificationActions(String title,String body,String idClient) {
        //Aceptar el servcio
        Intent aceptIntent=new Intent(this, AceptReceiver.class);
        aceptIntent.putExtra("idClient",idClient);
        PendingIntent aceptPendingIntent=PendingIntent.getBroadcast(this,NOTIFICATION_CODE,aceptIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action aceptAction=new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                aceptPendingIntent
        ).build();
        //cancelar el servicio
        Intent cancelIntent=new Intent(this, CancelReceiver.class);
        cancelIntent.putExtra("idClient",idClient);
        PendingIntent cancelPendingIntent=PendingIntent.getBroadcast(this,NOTIFICATION_CODE,cancelIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action cancelAction=new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelPendingIntent
        ).build();
        Uri soud= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper=new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder=notificationHelper.getNotificationOldAPIActions(title,body,soud,aceptAction,cancelAction);
        notificationHelper.getManaganer().notify(2,builder.build());
    }
    @RequiresApi(api=Build.VERSION_CODES.O)
    private void showNotificationAPIoreo(String title,String body) {
        PendingIntent intent=PendingIntent.getActivity(getBaseContext(),0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        Uri soud= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper=new NotificationHelper(getBaseContext());
        Notification.Builder builder=notificationHelper.getNotification(title,body,intent,soud);
        notificationHelper.getManaganer().notify(1,builder.build());
    }

    //versionde de android superiores a oreo
    @RequiresApi(api=Build.VERSION_CODES.O)
    private void showNotificationAPIoreoActions(String title,String body, String idClient) {
        //aceptar el servicio
        Intent aceptIntent=new Intent(this, AceptReceiver.class);
        aceptIntent.putExtra("idClient",idClient);
        PendingIntent aceptPendingIntent=PendingIntent.getBroadcast(this,NOTIFICATION_CODE,aceptIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Action aceptAction=new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                aceptPendingIntent
        ).build();
        //´cancelar el servicio
        Intent cancelIntent=new Intent(this, CancelReceiver.class);
        cancelIntent.putExtra("idClient",idClient);
        PendingIntent cancelPendingIntent=PendingIntent.getBroadcast(this,NOTIFICATION_CODE,cancelIntent ,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Action cancelAction=new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelPendingIntent
        ).build();
        Uri soud= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper=new NotificationHelper(getBaseContext());
        Notification.Builder builder=notificationHelper.getNotificationActions(title,body,soud,aceptAction,cancelAction);
        notificationHelper.getManaganer().notify(2,builder.build());
    }

}
