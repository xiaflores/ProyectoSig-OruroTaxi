package com.example.proyectosig.view.conductor;

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
import com.example.proyectosig.providers.AuthProvider;
import com.example.proyectosig.providers.ClientServiceProvider;
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

public class MapConductorServiceActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private AuthProvider mAuthProvider;
    private GeofireProvider mGeofireProvider;
    private TokenProvider mTokenProvider;
    private ClienteProvider mClienteProvider;
    private ClientServiceProvider mClientServiceProvider;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private Marker mMarker;
    private LatLng mCurrentLatlng;

    private TextView mTextViewClientService;
    private TextView mTextViewEmailClientService;
    private TextView mTextViewOriginClientService;
    private TextView mTextViewDestinationClientService;

    private String mExtraClientId;

    private LatLng mOriginLatlng;
    private LatLng mDestinationLatlng;

    private GoogleApiProvider mGoogleApiProvider;

    private List<LatLng> mPolyLineList;
    private PolylineOptions mPolyLineOptions;

    private Boolean mIsFirstTime = true;
    private Boolean mIsCloseToClient = false;

    private Button mButtonStarService;
    private Button mButtonfinishService;

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    mCurrentLatlng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (mMarker != null) {
                        mMarker.remove();
                    }
                    mMarker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .title("tu posicion").icon(BitmapDescriptorFactory.fromResource(R.drawable.conductor)));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(16f).build()
                    ));
                    updateLocation();
                    if (mIsFirstTime) {
                        mIsFirstTime = false;
                        getClientService();

                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_conductor_service);

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("conductor_trabajando");
        mTokenProvider = new TokenProvider();
        mClienteProvider = new ClienteProvider();
        mClientServiceProvider = new ClientServiceProvider();

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mTextViewClientService = findViewById(R.id.txtViewClientService);
        mTextViewEmailClientService = findViewById(R.id.txtViewEmailClientService);

        mTextViewOriginClientService = findViewById(R.id.txtViewOriginClientService);
        mTextViewDestinationClientService = findViewById(R.id.txtViewDestinationClientService);

        mButtonStarService = findViewById(R.id.btnStarService);
        mButtonfinishService = findViewById(R.id.btnFinishService);

       // mButtonStarService.setEnabled(false);

        mExtraClientId = getIntent().getStringExtra("idClient");
        mGoogleApiProvider = new GoogleApiProvider(MapConductorServiceActivity.this);

        getClient();
        mButtonStarService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsCloseToClient) {
                    startService();
                } else {
                    Toast.makeText(MapConductorServiceActivity.this, "Debes estar mas cerca de la posion de recogida", Toast.LENGTH_SHORT).show();
                }

            }
        });
        mButtonfinishService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishService();
            }
        });
    }

    private void finishService() {
        mClientServiceProvider.updateStatus(mExtraClientId, "finish");
        Intent intent= new Intent(MapConductorServiceActivity.this,MapConductorActivity.class);
        startActivity(intent);
        finish();
    }

    private void startService() {
        mClientServiceProvider.updateStatus(mExtraClientId, "start");
        mButtonStarService.setVisibility(View.GONE);
        mButtonfinishService.setVisibility(View.VISIBLE);
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(mDestinationLatlng).title("Destino" +
                "").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_destination_route)));
        drawRoute(mDestinationLatlng);
    }

    private double getDistanceBetween(LatLng clientLatlng, LatLng conductorLatlng) {
        double distance = 0;
        Location clientLocation = new Location("");
        Location conductorLocation = new Location("");
        clientLocation.setLatitude(clientLatlng.latitude);
        clientLocation.setLongitude(clientLatlng.longitude);
        conductorLocation.setLatitude(conductorLatlng.latitude);
        conductorLocation.setLongitude(conductorLatlng.longitude);
        distance = clientLocation.distanceTo(conductorLocation);
        return distance;
    }

    private void drawRoute(LatLng latLng) {
        mGoogleApiProvider.getDirections(mCurrentLatlng, latLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                //respuesta del servidor
                try {
                    JSONObject jsonObjet = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObjet.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    mPolyLineList = DecodePoints.decodePoly(points);
                    mPolyLineOptions = new PolylineOptions();
                    mPolyLineOptions.color(Color.DKGRAY);
                    mPolyLineOptions.width(15f);
                    mPolyLineOptions.startCap(new SquareCap());
                    mPolyLineOptions.jointType(JointType.ROUND);
                    mPolyLineOptions.addAll(mPolyLineList);
                    mMap.addPolyline(mPolyLineOptions);

                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");
                } catch (Exception e) {
                    Log.d("Error", "Error econtrado" + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                //si falla la respuesta el servidor

            }
        });
    }

    private void getClient() {
        mClienteProvider.getClient(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String email = dataSnapshot.child("email").getValue().toString();
                    String name = dataSnapshot.child("name").getValue().toString();
                    mTextViewClientService.setText(name);
                    mTextViewEmailClientService.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void getClientService() {
        mClientServiceProvider.getClientService(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String destino = dataSnapshot.child("destination").getValue().toString();
                    String origin = dataSnapshot.child("origin").getValue().toString();
                    double destinationLat = Double.parseDouble(dataSnapshot.child("destinationLat").getValue().toString());
                    double destinationLng = Double.parseDouble(dataSnapshot.child("destinationLng").getValue().toString());

                    double originLat = Double.parseDouble(dataSnapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(dataSnapshot.child("originLng").getValue().toString());

                    mOriginLatlng = new LatLng(originLat, originLng);
                    mDestinationLatlng = new LatLng(destinationLat, destinationLng);

                    mTextViewOriginClientService.setText("Recoger: " + origin);
                    mTextViewDestinationClientService.setText("Destino: " + destino);

                    mMap.addMarker(new MarkerOptions().position(mOriginLatlng).title("Recoger aqui" +
                            "").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_origin_route)));

                    drawRoute(mOriginLatlng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateLocation() {
        if (mAuthProvider.existSesion() && mCurrentLatlng != null) {
            mGeofireProvider.saveLocation(mAuthProvider.getId(), mCurrentLatlng);
            if (!mIsCloseToClient) {
                if (mOriginLatlng != null && mCurrentLatlng != null) {
                    double distance = getDistanceBetween(mOriginLatlng, mCurrentLatlng);//metros
                    if (distance <= 100) {
                        //mButtonStarService.setEnabled(true);
                        mIsCloseToClient = true;
                        Toast.makeText(this, "Esta cerca del cliente", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);
        startLocation();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    } else {
                        showAlertDialogNOGPS();
                    }

                } else {
                    checkLocationPermissions();
                }
            } else {
                checkLocationPermissions();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
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
                                ActivityCompat.requestPermissions(MapConductorServiceActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
                            }
                        }).create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(MapConductorServiceActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
            }

        }
    }
}