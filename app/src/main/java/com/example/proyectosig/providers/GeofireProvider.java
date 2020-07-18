package com.example.proyectosig.providers;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GeofireProvider {
    private DatabaseReference mDatabase;
    private GeoFire mGeoFire;

    public GeofireProvider(String reference){
        mDatabase= FirebaseDatabase.getInstance().getReference().child(reference);
        mGeoFire=new GeoFire(mDatabase);
    }
    public void saveLocation (String idConductor, LatLng latlng){
        mGeoFire.setLocation(idConductor, new GeoLocation(latlng.latitude,latlng.longitude));
    }
    public void removeLocation(String idConductor) {
        mGeoFire.removeLocation(idConductor);
    }
    public GeoQuery getActiveConductor(LatLng latlng,double radius){
        GeoQuery geoQuery=mGeoFire.queryAtLocation(new GeoLocation(latlng.latitude,latlng.longitude),radius);
        geoQuery.removeAllListeners();
        return geoQuery;
    }
    public DatabaseReference getConductorLocation(String idConductor){
        return mDatabase.child(idConductor).child("l");
    }
    public DatabaseReference isConductorTrabajando(String idConductor){
        return FirebaseDatabase.getInstance().getReference().child("conductor_trabajando").child(idConductor);
    }

}
