package com.example.proyectosig.view.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.proyectosig.R;
import com.example.proyectosig.providers.AuthProvider;
import com.example.proyectosig.providers.ClientServiceProvider;
import com.example.proyectosig.providers.ConductorProvider;
import com.example.proyectosig.providers.GeofireProvider;
import com.example.proyectosig.providers.GoogleApiProvider;
import com.example.proyectosig.providers.TokenProvider;
import com.example.proyectosig.utils.DecodePoints;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapClientServiceActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProvider mAuthProvider;

    private GeofireProvider mGeofireProvider;
    private TokenProvider mTokenProvider;
    private ClientServiceProvider mClientServiceProvider;
    private ConductorProvider mConductorProvider;

    private Marker mMarkerConductor;

    private Boolean mIsFirstTime = true;


    private String mOrigin;
    private LatLng mOriginLatlng;
    private LatLng mConductorLatlng;
    private LatLng mDestinoLatlng;

    private TextView mTextViewConductorService;
    private TextView mTextViewEmailConductorService;
    private TextView mTextViewOriginClientService;
    private TextView mTextViewDestinationClientService;
    private TextView mTextViewStatus;

    private GoogleApiProvider mGoogleApiProvider;
    private List<LatLng> mPolyLineList;
    private PolylineOptions mPolyLineOptions;

    private ValueEventListener mListener;
    private String mIdConductor;
    private ValueEventListener mListenerStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client_service);

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("conductor_trabajando");
        mTokenProvider = new TokenProvider();
        mClientServiceProvider = new ClientServiceProvider();
        mGoogleApiProvider = new GoogleApiProvider(MapClientServiceActivity.this);
        mConductorProvider =new ConductorProvider();

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        mTextViewConductorService = findViewById(R.id.txtViewConductorService);
        mTextViewEmailConductorService = findViewById(R.id.txtViewEmailConductorService);
        mTextViewOriginClientService = findViewById(R.id.txtViewOriginConductorService);
        mTextViewDestinationClientService = findViewById(R.id.txtViewDestinationConductorService);
        mTextViewStatus=findViewById(R.id.txtViewStatus);
        getStatus();

        getClientService();
    }

    private void getStatus() {
        mListenerStatus= mClientServiceProvider.getStatus(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String status=dataSnapshot.getValue().toString();
                    mTextViewStatus.setText(status);
                    if(status.equals("accept")){
                        mTextViewStatus.setText("Estado: aceptado");
                    }
                    if(status.equals("start")){
                        mTextViewStatus.setText("Estado: Viaje iniciado");
                        startService();
                    }
                    else if(status.equals("finish")){
                        mTextViewStatus.setText("Estado: Viaje finalizado");
                        finishService();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void finishService() {
        Intent intent= new Intent(MapClientServiceActivity.this, MapClienteActivity.class);
        startActivity(intent);
        finish();
    }

    private void startService() {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(mDestinoLatlng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_destination_route)));
        drawRoute(mDestinoLatlng);
    }

    private void drawRoute(LatLng latLng) {
        mGoogleApiProvider.getDirections(mConductorLatlng, latLng).enqueue(new Callback<String>() {
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
                    mPolyLineOptions.width(10f);
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
                            .title("Tu conductor")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.conductor)));
                    if (mIsFirstTime) {
                        mIsFirstTime = false;
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(mConductorLatlng)
                                        .zoom(15f)
                                        .build()
                        ));
                        drawRoute(mOriginLatlng);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void getClientService() {
        mClientServiceProvider.getClientService(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String destination = dataSnapshot.child("destination").getValue().toString();
                    String origin = dataSnapshot.child("origin").getValue().toString();
                    String idConductor = dataSnapshot.child("idConductor").getValue().toString();
                    mIdConductor=idConductor;
                    double destinationLat = Double.parseDouble(dataSnapshot.child("destinationLat").getValue().toString());
                    double destinationLng = Double.parseDouble(dataSnapshot.child("destinationLng").getValue().toString());

                    double originLat = Double.parseDouble(dataSnapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(dataSnapshot.child("originLng").getValue().toString());

                    mOriginLatlng = new LatLng(originLat, originLng);
                    mDestinoLatlng = new LatLng(destinationLat, destinationLng);
                    mTextViewOriginClientService.setText("Recoger"+origin);
                    mTextViewDestinationClientService.setText("Destino"+destination);

                    mMap.addMarker(new MarkerOptions().position(mOriginLatlng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_origin_route)));
                    getConductor(idConductor);
                    getConductorLocation(idConductor);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getConductor(String idConductor) {
        mConductorProvider.getConductor(idConductor).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String name=dataSnapshot.child("name").getValue().toString();
                    String email=dataSnapshot.child("email").getValue().toString();
                    mTextViewConductorService.setText(name);
                    mTextViewEmailConductorService.setText(email);
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
        if(mListenerStatus!=null){
            mClientServiceProvider.getStatus(mAuthProvider.getId()).removeEventListener(mListener);
        }
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMyLocationEnabled(true);
    }
}