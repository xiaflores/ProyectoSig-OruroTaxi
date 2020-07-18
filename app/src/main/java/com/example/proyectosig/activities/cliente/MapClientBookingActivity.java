package com.example.proyectosig.activities.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.proyectosig.R;
import com.example.proyectosig.activities.conductor.MapConductorBookingActivity;
import com.example.proyectosig.providers.AuthProvider;
import com.example.proyectosig.providers.ClientBookingProvider;
import com.example.proyectosig.providers.GeofireProvider;
import com.example.proyectosig.providers.GoogleApiProvider;
import com.example.proyectosig.providers.TokenProvider;
import com.example.proyectosig.utils.DecodePoints;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapClientBookingActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProvider mAuthProvider;

    private GeofireProvider mGeofireProvider;
    private TokenProvider mTokenProvider;
    private ClientBookingProvider mClientBookingProvider;

    private Marker mMarkerConductor;

    private Boolean mIsFirstTime = true;


    private String mDestino;
    private LatLng mDestinoLatlng;
    private LatLng mConductorLatlng;

    private String mOrigin;
    private LatLng mOriginLatlng;

    private TextView mTextViewClientBooking;
    private TextView mTextViewEmailClientBooking;
    private TextView mTextViewOriginClientBooking;
    private TextView mTextViewDestinationClientBooking;

    private GoogleApiProvider mGoogleApiProvider;

    private List<LatLng> mPolyLineList;
    private PolylineOptions mPolyLineOptions;

    private ValueEventListener mListener;

    private String mIdConductor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client_booking);

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("conductor_trabajando");
        mTokenProvider = new TokenProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mGoogleApiProvider = new GoogleApiProvider(MapClientBookingActivity.this);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        mTextViewClientBooking = findViewById(R.id.txtViewConductorBooking);
        mTextViewEmailClientBooking = findViewById(R.id.txtViewEmailConductorBooking);

        mTextViewOriginClientBooking = findViewById(R.id.txtViewOriginConductorBooking);
        mTextViewDestinationClientBooking = findViewById(R.id.txtViewDestinationConductorBooking);
        getClientBooking();
    }

    private void drawRoute() {
        mGoogleApiProvider.getDirections(mConductorLatlng, mOriginLatlng).enqueue(new Callback<String>() {
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

    private void getClientBooking() {
        mClientBookingProvider.getClientBookimg(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String destino = dataSnapshot.child("destination").getValue().toString();
                    String origin = dataSnapshot.child("origin").getValue().toString();
                    String idConductor = dataSnapshot.child("idConductor").getValue().toString();
                    mIdConductor=idConductor;
                    double destinationLat = Double.parseDouble(dataSnapshot.child("detination_lat").getValue().toString());
                    double destinationLng = Double.parseDouble(dataSnapshot.child("detination_lng").getValue().toString());

                    double originLat = Double.parseDouble(dataSnapshot.child("origin_lat").getValue().toString());
                    double originLng = Double.parseDouble(dataSnapshot.child("origin_lng").getValue().toString());

                    mOriginLatlng = new LatLng(originLat, originLng);
                    mDestinoLatlng = new LatLng(destinationLat, destinationLng);
                    mTextViewOriginClientBooking.setText(origin);
                    mTextViewDestinationClientBooking.setText(destino);

                    mMap.addMarker(new MarkerOptions().position(mOriginLatlng).title("Recoger aqui" +
                            "").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_origin_route)));
                    getConductorLocation(idConductor);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getConductorLocation(String idConductor) {
        mListener= mGeofireProvider.getConductorLocation(idConductor).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    double lat = Double.parseDouble(dataSnapshot.child("0").getValue().toString());
                    double lng = Double.parseDouble(dataSnapshot.child("1").getValue().toString());
                    mConductorLatlng = new LatLng(lat, lng);
                    if (mMarkerConductor != null) {
                        mMarkerConductor.remove();
                    }
                    mMarkerConductor = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title("tu conductor")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.conductor)));
                    if (mIsFirstTime) {
                        mIsFirstTime = false;
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(mConductorLatlng)
                                        .zoom(14f)
                                        .build()
                        ));
                        drawRoute();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mListener!=null){
            mGeofireProvider.getConductorLocation(mIdConductor).removeEventListener(mListener);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
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
        mMap.setMyLocationEnabled(true);
    }
}