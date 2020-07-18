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
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.proyectosig.R;
import com.example.proyectosig.activities.MainActivity;
import com.example.proyectosig.includes.MyToolbar;
import com.example.proyectosig.providers.AuthProvider;
import com.example.proyectosig.providers.ClientBookingProvider;
import com.example.proyectosig.providers.GeofireProvider;
import com.example.proyectosig.providers.TokenProvider;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MapConductorActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProvider mAuthProvider;
    private GeofireProvider mGeofireProvider;
    private TokenProvider mTokenProvider;


    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;

    private final static int LOCATION_REQUEST_CODE=1;
    private final static int SETTINGS_REQUEST_CODE=2;

    private Marker mMarker;

    private Button mButtonConectar;
    private Boolean mIsConect=false;

    private LatLng mCurrentLatlng;
    private ValueEventListener mListener;

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
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_conductor);

        MyToolbar.show(this,"Conductor",false);


        mAuthProvider=new AuthProvider();
        mGeofireProvider=new GeofireProvider("conductor_activo");
        mTokenProvider=new TokenProvider();
        mFusedLocation= LocationServices.getFusedLocationProviderClient(this);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mButtonConectar=findViewById(R.id.btnConectar);
        mButtonConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsConect){
                    disconnect();
                }
                else{
                    startLocation();
                }
            }
        });
        generatetoken();
        isConductorTrabajando();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if(mListener!=null){
            mGeofireProvider.isConductorTrabajando(mAuthProvider.getId()).removeEventListener(mListener);
        }
    }

    private void isConductorTrabajando() {
        mListener = mGeofireProvider.isConductorTrabajando(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    disconnect();
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

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==LOCATION_REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                    if(gpsActived()){
                        mFusedLocation.requestLocationUpdates(mLocationRequest,mLocationCallback,Looper.myLooper());
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
            mButtonConectar.setText("Conectarse");
            mIsConect=false;
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
                    mButtonConectar.setText("Desconectarse");
                    mIsConect=true;
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
                            ActivityCompat.requestPermissions(MapConductorActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
                        }
                    }).create()
                        .show();
        }
        else{
            ActivityCompat.requestPermissions(MapConductorActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.conductor_menu,menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.action_logout){
            logout();
        }
        return super.onOptionsItemSelected(item);
    }
    void logout(){
        disconnect();
        mAuthProvider.logout();
        Intent intent=new Intent(MapConductorActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
    void generatetoken(){
        mTokenProvider.create(mAuthProvider.getId());
    }
}