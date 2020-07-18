package com.example.proyectosig.activities.cliente;

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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.proyectosig.R;
import com.example.proyectosig.activities.MainActivity;
import com.example.proyectosig.includes.MyToolbar;
import com.example.proyectosig.providers.AuthProvider;
import com.example.proyectosig.providers.GeofireProvider;
import com.example.proyectosig.providers.TokenProvider;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DatabaseError;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapClienteActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProvider mAuthProvider;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;

    private GeofireProvider mGeofireProvider;
    private TokenProvider mTokenProvider;

    private final static int LOCATION_REQUEST_CODE=1;
    private final static int SETTINGS_REQUEST_CODE=2;

    private Marker mMarker;
    private LatLng mCurrentLatlng;
    private List<Marker> mConductorMarker=new ArrayList<>();
    private Boolean mIsFirstTime=true;

    private AutocompleteSupportFragment mAutocomplete;
    private AutocompleteSupportFragment mAutocompleteDestination;
    private PlacesClient mPlaces;

    private String mDestino;
    private LatLng mDestinoLatlng;

    private String mOrigin;
    private LatLng mOriginLatlng;

    private GoogleMap.OnCameraIdleListener mCameraListener;


    private Button mButtonRequestConductor;

    LocationCallback mLocationCallback=new LocationCallback(){
        @Override
        public  void onLocationResult(LocationResult locationResult){
            for(Location location: locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    /*if(mMarker!=null){
                        mMarker.remove();
                    }*/
                    mCurrentLatlng=new LatLng(location.getLatitude(),location.getLongitude());
                    /*
                    mMarker=mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLatitude(),location.getLongitude()))
                            .title("tu posicion").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marker_client)));*/
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(),location.getLongitude()))
                                    .zoom(15f).build()
                    ));
                    if(mIsFirstTime){
                        mIsFirstTime=false;
                        getActiveConductor();
                        limitSearch();
                    }
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_cliente);

        MyToolbar.show(this,"Cliente",false);

        mAuthProvider=new AuthProvider();
        mGeofireProvider=new GeofireProvider("conductor_activo");
        mTokenProvider=new TokenProvider();
        mFusedLocation= LocationServices.getFusedLocationProviderClient(this);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
        mButtonRequestConductor=findViewById(R.id.btnRequestConductor);
        if(!Places.isInitialized()){
            Places.initialize(getApplicationContext(),getResources().getString(R.string.google_maps_key));
        }
        mPlaces=Places.createClient(this);
        instanceAutocompleteOrigin();
        instanceAutocompleteDestination();
        onCameraMover();

        mButtonRequestConductor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestConductor();
            }


        });

        generatetoken();


    }
    private void requestConductor() {
        if(mOriginLatlng!=null && mDestinoLatlng!=null){
            Intent intent =new Intent(MapClienteActivity.this, DetailRequestActivity.class);
            intent.putExtra("origin_lat",mOriginLatlng.latitude);
            intent.putExtra("origin_lng",mOriginLatlng.longitude);
            intent.putExtra("destination_lat",mDestinoLatlng.latitude);
            intent.putExtra("destination_lng",mDestinoLatlng.longitude);
            intent.putExtra("origin",mOrigin);
            intent.putExtra("destination",mDestino);
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "Debe seleccionar el lugar de recogida y destino", Toast.LENGTH_SHORT).show();
        }
    }
    private void limitSearch() {
        LatLng northSide= SphericalUtil.computeOffset(mCurrentLatlng,5000,0);
        LatLng sourthSide= SphericalUtil.computeOffset(mCurrentLatlng,5000,180);
        mAutocomplete.setCountry("BOL");
        mAutocomplete.setLocationBias(RectangularBounds.newInstance(sourthSide,northSide));
        mAutocompleteDestination.setCountry("BOL");

    }
    private void onCameraMover(){
        mCameraListener=new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                try {
                    Geocoder geocoder=new Geocoder(MapClienteActivity.this);
                    mOriginLatlng=mMap.getCameraPosition().target;
                    List<Address> addressList=geocoder.getFromLocation(mOriginLatlng.latitude,mOriginLatlng.longitude,1);
                    String city=addressList.get(0).getLocality();
                    String country=addressList.get(0).getCountryName();
                    String address=addressList.get(0).getAddressLine(0);
                    mOrigin=address+" "+city;
                    mAutocomplete.setText(address+" "+city);
                }
                catch (Exception e){
                    Log.d("Error","Mensaje error" + e.getMessage());
                }

            }
        };
    }
    private void instanceAutocompleteOrigin(){
        mAutocomplete=(AutocompleteSupportFragment)getSupportFragmentManager().findFragmentById(R.id.placeAutocompleteOrigen);
        mAutocomplete.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.LAT_LNG,Place.Field.NAME));
        mAutocomplete.setHint("lugar de recogida");
        mAutocomplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mOrigin=place.getName();
                mOriginLatlng=place.getLatLng();
                Log.d("PLACE","NAME"+mOrigin);
                Log.d("PLACE","lat"+mOriginLatlng.latitude);
                Log.d("PLACE","lng"+mOriginLatlng.longitude);

            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

    }
    private void instanceAutocompleteDestination(){
        mAutocompleteDestination=(AutocompleteSupportFragment)getSupportFragmentManager().findFragmentById(R.id.placeAutocompleteDestino);
        mAutocompleteDestination.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.LAT_LNG,Place.Field.NAME));
        mAutocompleteDestination.setHint("lugar de Destino");
        mAutocompleteDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mDestino=place.getName();
                mDestinoLatlng=place.getLatLng();
                Log.d("PLACE","NAME"+mDestino);
                Log.d("PLACE","lat"+mDestinoLatlng.latitude);
                Log.d("PLACE","lng"+mDestinoLatlng.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
    }
    private  void getActiveConductor(){
        mGeofireProvider.getActiveConductor(mCurrentLatlng,10).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override

            public void onKeyEntered(String key, GeoLocation location) {
                //se muestran los marcadores de los conductores
                for(Marker marker:mConductorMarker){
                    if(marker.getTag()!=null){
                        if(marker.getTag().equals(key)){
                            return;
                        }
                    }
                }
                LatLng conductorLatlng=new LatLng(location.latitude,location.longitude);
                Marker marker = mMap.addMarker(new MarkerOptions().position(conductorLatlng).title("Conductor Disponible").icon(BitmapDescriptorFactory.fromResource(R.drawable.conductor)));
                marker.setTag(key);
                mConductorMarker.add(marker);

            }

            @Override
            //eliminar marcadores de los que se conectan
            public void onKeyExited(String key) {
                for(Marker marker:mConductorMarker){
                    if(marker.getTag()!=null){
                        if(marker.getTag().equals(key)){
                            marker.remove();
                            mConductorMarker.remove(marker);
                            return;
                        }
                    }
                }

            }
            //se actualizan en realtime
            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for(Marker marker:mConductorMarker){
                    if(marker.getTag()!=null){
                        if(marker.getTag().equals(key)){
                            marker.setPosition(new LatLng(location.latitude,location.longitude));
                        }
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.setOnCameraIdleListener(mCameraListener);

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
        else if(requestCode == SETTINGS_REQUEST_CODE && !gpsActived()){
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
                                ActivityCompat.requestPermissions(MapClienteActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
                            }
                        }).create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(MapClienteActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
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
        mAuthProvider.logout();
        Intent intent=new Intent(MapClienteActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
    void generatetoken(){
        mTokenProvider.create(mAuthProvider.getId());
    }
}