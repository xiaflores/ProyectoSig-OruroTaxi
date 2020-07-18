package com.example.proyectosig.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {

    public static Retrofit getClient(String url){
        Retrofit retrofit=new Retrofit.Builder().baseUrl(url).addConverterFactory(ScalarsConverterFactory.create()).build();
        return retrofit;
    }
    public static Retrofit getClientObjet(String url){
        Retrofit retrofit=new Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build();
            return retrofit;
    }
}
