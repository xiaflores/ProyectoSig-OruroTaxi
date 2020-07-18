package com.example.proyectosig.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.proyectosig.activities.conductor.MapConductorBookingActivity;
import com.example.proyectosig.providers.AuthProvider;
import com.example.proyectosig.providers.ClientBookingProvider;
import com.example.proyectosig.providers.GeofireProvider;

public class AceptReceiver extends BroadcastReceiver {
    private ClientBookingProvider mClientBookingProvider;
    private GeofireProvider mGeofireProvider;
    public AuthProvider mAuthProvider;
    @Override
    public void onReceive(Context context, Intent intent) {
        mAuthProvider=new AuthProvider();
        mGeofireProvider =new GeofireProvider("conductor_activo");
        mGeofireProvider.removeLocation(mAuthProvider.getId());
        String idClient=intent.getExtras().getString("idClient");
        mClientBookingProvider=new ClientBookingProvider();
            mClientBookingProvider.updateStatus(idClient,"accept");

            NotificationManager manager=(NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
            manager.cancel(2);
            Intent intent1=new Intent(context, MapConductorBookingActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent1.setAction(Intent.ACTION_RUN);
            intent1.putExtra("idClient",idClient);
            context.startActivity(intent1);

    }

}
