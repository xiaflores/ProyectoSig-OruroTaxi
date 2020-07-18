package com.example.proyectosig.activities.conductor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyectosig.R;
import com.example.proyectosig.activities.cliente.DetailRequestActivity;
import com.example.proyectosig.providers.AuthProvider;
import com.example.proyectosig.providers.ClientBookingProvider;
import com.example.proyectosig.providers.ClienteProvider;
import com.example.proyectosig.providers.GeofireProvider;
import com.example.proyectosig.providers.GoogleApiProvider;
import com.example.proyectosig.providers.TokenProvider;
import com.example.proyectosig.utils.DecodePoints;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapConductorBookingActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private AuthProvider mAuthProvider;
    private GeofireProvider mGeofireProvider;
    private TokenProvider mTokenProvider;
    private ClienteProvider mClienteProvider;
    private ClientBookingProvider mClientBookingProvider;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;

    private final static int LOCATION_REQUEST_CODE=1;
    private final static int SETTINGS_REQUEST_CODE=2;

    private Marker mMarker;
    private LatLng mCurrentLatlng;

    private TextView mTextViewClientBooking;
    private TextView mTextViewEmailClientBooking;
    private TextView mTextViewOriginClientBooking;
    private TextView mTextViewDestinationClientBooking;

    private String mExtraClientId;

    private LatLng mOriginLatlng;
    private LatLng mDestinationLatlng;

    private GoogleApiProvider mGoogleApiProvider;

    private List<LatLng> mPolyLineList;
    private PolylineOptions mPolyLineOptions;

    private Boolean mIsFirstTime=true;
    private Boolean mIsCloseToClient=false;

    private Button mButtonStarBooking;
    private Button mButtonfinishBooking;

    LocationCallback mLocationCallback=new LocationCallback(){
        @Override
        public  void onLocationResult(LocationResult locationResult){
            for(Location location: locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    mCurrentLatlng=new LatLng(location.getLatitude(),location.getLongitude());

                    if(mMarker!=null){
                        mMarker.remove();
                    }
                    mMarker=mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLatitude(),location.getLongitude()))
                            .title("tu posicion").icon(BitmapDescriptorFactory.fromResource(R.drawable.conductor)));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(),location.getLongitude()))
                                    .zoom(16f).build()
                    ));
                    updateLocation();
                    if(mIsFirstTime){
                        mIsFirstTime=false;
                        getClientBooking();

                    }
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_conductor_booking);
        mAuthProvider=new AuthProvider();
        mGeofireProvider=new GeofireProvider("conductor_trabajando");
        mTokenProvider=new TokenProvider();
        mClienteProvider=new ClienteProvider();
        mClientBookingProvider =new ClientBookingProvider();

        mFusedLocation= LocationServices.getFusedLocationProviderClient(this);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mTextViewClientBooking=findViewById(R.id.txtViewClientBooking);
        mTextViewEmailClientBooking=findViewById(R.id.txtViewEmailClientBooking);

        mTextViewOriginClientBooking=findViewById(R.id.txtViewOriginClientBooking);
        mTextViewDestinationClientBooking=findViewById(R.id.txtViewDestinationClientBooking);

        mButtonStarBooking=findViewById(R.id.btnStarBooking);
        mButtonfinishBooking=findViewById(R.id.btnFinishBooking);

        //mButtonStarBooking.setEnabled(false);

        mExtraClientId=getIntent().getStringExtra("idClient");

        mGoogleApiProvider=new GoogleApiProvider(MapConductorBookingActivity.this);

        getClient();
        mButtonStarBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsCloseToClient){
                    startBoking();
                }
                else{
                    Toast.makeText(MapConductorBookingActivity.this, "Debes estar mas cerca de la posion de recogida", Toast.LENGTH_SHORT).show();
                }

            }
        });
        mButtonfinishBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishbooking();
            }
        });

    }

    private void finishbooking() {
        mClientBookingProvider.updateStatus(mExtraClientId,"finish");
    }

    private void startBoking() {
        mClientBookingProvider.updateStatus(mExtraClientId,"start");
        mButtonStarBooking.setVisibility(View.GONE);
        mButtonStarBooking.setVisibility(View.GONE);
        mButtonfinishBooking.setVisibility(View.VISIBLE);
    }
    private double getDistanceBetween(LatLng clientLatlng,LatLng conductorLatlng){
        double distance=0;
        Location clientLocation=new Location("");
        Location conductorLocation=new Location("");
        clientLocation.setLatitude(clientLatlng.latitude);
        clientLocation.setLongitude(clientLatlng.longitude);
        conductorLocation.setLatitude(conductorLatlng.latitude);
        conductorLocation.setLongitude(conductorLatlng.longitude);
        distance=clientLocation.distanceTo(conductorLocation);
        return distance;
    }
    private void drawRoute(){
        mGoogleApiProvider.getDirections(mCurrentLatlng,mOriginLatlng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                //respuesta del servidor
                try {
                    JSONObject jsonObjet=new JSONObject(response.body());
                    JSONArray jsonArray=jsonObjet.getJSONArray("routes");
                    JSONObject route=jsonArray.getJSONObject(0);
                    JSONObject polylines=route.getJSONObject("overview_polyline");
                    String points=polylines.getString("points");
                    mPolyLineList= DecodePoints.decodePoly(points);
                    mPolyLineOptions=new PolylineOptions();
                    mPolyLineOptions.color(Color.DKGRAY);
                    mPolyLineOptions.width(15f);
                    mPolyLineOptions.startCap(new SquareCap());
                    mPolyLineOptions.jointType(JointType.ROUND);
                    mPolyLineOptions.addAll(mPolyLineList);
                    mMap.addPolyline(mPolyLineOptions);

                    JSONArray legs=route.getJSONArray("legs");
                    JSONObject leg=legs.getJSONObject(0);
                    JSONObject distance=leg.getJSONObject("distance");
                    JSONObject duration=leg.getJSONObject("duration");
                    String distanceText=distance.getString("text");
                    String durationText=duration.getString("text");
                }catch (Exception e){
                    Log.d("Error","Error econtrado"+e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                //si falla la respuesta el servidor

            }
        });
    }

    private void getClientBooking() {
        mClientBookingProvider.getClientBookimg(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String destino=dataSnapshot.child("destination").getValue().toString();
                    String origin=dataSnapshot.child("origin").getValue().toString();
                    double destinationLat= Double.parseDouble(dataSnapshot.child("detination_lat").getValue().toString());
                    double destinationLng= Double.parseDouble(dataSnapshot.child("detination_lng").getValue().toString());

                    double originLat= Double.parseDouble(dataSnapshot.child("origin_lat").getValue().toString());
                    double originLng= Double.parseDouble(dataSnapshot.child("origin_lng").getValue().toString());

                    mOriginLatlng=new LatLng(originLat,originLng);
                    mDestinationLatlng=new LatLng(destinationLat,destinationLat);

                    mTextViewOriginClientBooking.setText(origin);
                    mTextViewDestinationClientBooking.setText(destino);

                    mMap.addMarker(new MarkerOptions().position(mOriginLatlng).title("Recoger aqui" +
                            "").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_origin_route)));

                    drawRoute();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void getClient() {
        mClienteProvider.getClient(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String email=dataSnapshot.child("email").getValue().toString();
                    String name=dataSnapshot.child("name").getValue().toString();
                    mTextViewClientBooking.setText(name);
                    mTextViewEmailClientBooking.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateLocation(){
        if(mAuthProvider.existSesion() && mCurrentLatlng!=null){
            mGeofireProvider.saveLocation(mAuthProvider.getId(),mCurrentLatlng);
            if(mIsCloseToClient){
                if(mOriginLatlng!=null && mCurrentLatlng!=null){
                    double distance =getDistanceBetween(mOriginLatlng,mCurrentLatlng);//metors
                    if(distance<=200){
                     //   mButtonStarBooking.setEnabled(true);
                        mIsCloseToClient=true;
                        Toast.makeText(this, "esta cerca del cliente", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mLocationRequest=new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);
        startLocation();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==LOCATION_REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                    if(gpsActived()){
                        mFusedLocation.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                    else {
                        showAlertDialogNOGPS();
                    }

                }
                else{
                    checkLocationPermissions();
                }
            }
            else{
                checkLocationPermissions();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SETTINGS_REQUEST_CODE && gpsActived()){
            mFusedLocation.requestLocationUpdates(mLocationRequest,mLocationCallback,Looper.myLooper());
            mMap.setMyLocationEnabled(true);
        }
        else{
            showAlertDialogNOGPS();
        }
    }

    private void showAlertDialogNOGPS(){
        AlertDialog.Builder bulider=new AlertDialog.Builder(this);
        bulider.setMessage("Por favor activa tu ubicacion para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }
    private boolean gpsActived(){
        boolean isActive=false;
        LocationManager locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)){
            isActive=true;
        }
        return isActive;
    }
    private void disconnect(){

        if(mFusedLocation!=null){

            mFusedLocation.removeLocationUpdates(mLocationCallback);
            if(mAuthProvider.existSesion()){
                mGeofireProvider.removeLocation(mAuthProvider.getId());
            }

        }
        else{
            Toast.makeText(this, "No te puedes desconectar", Toast.LENGTH_SHORT).show();

        }
    }
    private void startLocation(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if(gpsActived()) {
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                }
                else{
                    showAlertDialogNOGPS();
                }
            }
            else
            {
                checkLocationPermissions();
            }

        }
        else {
            if(gpsActived()){
                mFusedLocation.requestLocationUpdates(mLocationRequest,mLocationCallback,Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            }
            else {
                showAlertDialogNOGPS();
            }

        }
    }
    private void checkLocationPermissions(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this).setTitle("Proporcionas los permiso para continuar")
                        .setTitle("Esta aplicacion requiere los permisos de ubicacion para poder utilizarse")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MapConductorBookingActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
                            }
                        }).create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(MapConductorBookingActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
            }

        }
    }
}