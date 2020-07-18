package com.example.proyectosig.retrofit;

import com.example.proyectosig.models.FCMBody;
import com.example.proyectosig.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAGEKzS4k:APA91bHUGRO_LYQoTQvMCfbSX3lSc4Rncx7ZMypk365U65TH3MHdDg0llfAgspOPVOOVshm-z8jpd_O9Tth6Ux7m5KSaD6NGXjuc7FyTeTOchUZehsnna_EVNFwEm8d4FerJELyQF_k7"
    })
    @POST("fcm/send")
    Call<FCMResponse> send (@Body FCMBody body);
}
