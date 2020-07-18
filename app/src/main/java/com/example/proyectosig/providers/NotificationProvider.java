package com.example.proyectosig.providers;

import com.example.proyectosig.models.FCMBody;
import com.example.proyectosig.models.FCMResponse;
import com.example.proyectosig.retrofit.IFCMApi;
import com.example.proyectosig.retrofit.RetrofitClient;

import retrofit2.Call;

public class NotificationProvider {
    private  String url="https://fcm.googleapis.com";

    public NotificationProvider() {

    }

    public Call<FCMResponse> sendNotification(FCMBody body){
        return RetrofitClient.getClientObjet(url).create(IFCMApi.class).send(body);
    }
}
