package com.example.proyectosig.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.proyectosig.providers.ClientBookingProvider;

public class CancelReceiver extends BroadcastReceiver {
    private ClientBookingProvider mClientBookingProvider;
    @Override
    public void onReceive(Context context, Intent intent) {
        String idClient=intent.getExtras().getString("idClient");
        mClientBookingProvider=new ClientBookingProvider();
        mClientBookingProvider.updateStatus(idClient,"cancel");

        NotificationManager manager=(NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        manager.cancel(2);
    }
}
