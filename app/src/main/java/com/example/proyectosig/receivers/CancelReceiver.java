package com.example.proyectosig.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.proyectosig.providers.ClientServiceProvider;

public class CancelReceiver extends BroadcastReceiver {
    private ClientServiceProvider mClientServiceProvider;
    @Override
    public void onReceive(Context context, Intent intent) {
        String idClient=intent.getExtras().getString("idClient");
        mClientServiceProvider =new ClientServiceProvider();
        mClientServiceProvider.updateStatus(idClient,"cancel");

        NotificationManager manager=(NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        manager.cancel(2);
    }
}
