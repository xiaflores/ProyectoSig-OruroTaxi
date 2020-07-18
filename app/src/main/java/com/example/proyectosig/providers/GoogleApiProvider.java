package com.example.proyectosig.providers;

import android.content.Context;

import com.example.proyectosig.R;
import com.example.proyectosig.retrofit.IGoogleApi;
import com.example.proyectosig.retrofit.RetrofitClient;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

import retrofit2.Call;

public class GoogleApiProvider {
    private Context context;
    public GoogleApiProvider(Context context){
    this.context=context;
    }
    public Call<String> getDirections(LatLng originLatlng,LatLng destinationLatlng){
        String baseurl="https://maps.googleapis.com";
        String query="/maps/api/directions/json?mode=driving&transit_routing_preferences=less_driving&"
                + "origin=" + originLatlng.latitude + "," + originLatlng.longitude + "&"
                + "destination=" + destinationLatlng.latitude + "," + destinationLatlng.longitude + "&"
                + "departure_time=" + (new Date().getTime() + (60*60*1000)) + "&"
                + "traffic_model=best_guess&"
                + "key=" + context.getResources().getString(R.string.google_maps_key);
        return RetrofitClient.getClient(baseurl).create(IGoogleApi.class).getDirections(baseurl+query);
    }
}