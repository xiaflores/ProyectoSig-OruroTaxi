package com.example.proyectosig.activities.cliente;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.proyectosig.R;
import com.example.proyectosig.includes.MyToolbar;
import com.example.proyectosig.providers.GoogleApiProvider;
import com.example.proyectosig.utils.DecodePoints;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DetailRequestActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private double mExtraOrigenLat;
    private double mExtraOrigenLng;
    private double mExtraDestinationLat;
    private double mExtraDestinationLng;

    private String mExtraOrigin;
    private String mExtraDestination;

    private LatLng mOriginLatlng;
    private LatLng mDestinationLatlng;

    private GoogleApiProvider mGoogleApiProvider;

    private List<LatLng> mPolyLineList;
    private PolylineOptions mPolyLineOptions;

    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private TextView mTextViewTime;
    private TextView mTextViewDistance;

    private Button mButtonRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_request);
        MyToolbar.show(this,"Cliente",true);
        mMapFragment=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mExtraOrigenLat=getIntent().getDoubleExtra("origin_lat",0);
        mExtraOrigenLng=getIntent().getDoubleExtra("origin_lng",0);
        mExtraDestinationLat=getIntent().getDoubleExtra("destination_lat",0);
        mExtraDestinationLng=getIntent().getDoubleExtra("destination_lng",0);

        mExtraOrigin=getIntent().getStringExtra("origin");
        mExtraDestination=getIntent().getStringExtra("destination");

        mOriginLatlng=new LatLng(mExtraOrigenLat,mExtraOrigenLng);
        mDestinationLatlng=new LatLng(mExtraDestinationLat,mExtraDestinationLng);

        mGoogleApiProvider=new GoogleApiProvider(DetailRequestActivity.this);

        mTextViewOrigin=findViewById(R.id.txtViewOrigin);
        mTextViewDestination=findViewById(R.id.txtViewDestination);
        mTextViewTime=findViewById(R.id.txtViewTime);
        mTextViewDistance=findViewById(R.id.txtViewDistance);
        mButtonRequest =findViewById(R.id.btnRequestNow);

        mTextViewOrigin.setText(mExtraOrigin);
        mTextViewDestination.setText(mExtraDestination);

        mButtonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRequestConductor();
            }
        });

    }
    private void goToRequestConductor(){
        Intent intent=new Intent(DetailRequestActivity.this,RequestConductorActivity.class);
        intent.putExtra("origin_lat",mOriginLatlng.latitude);
        intent.putExtra("origin_lng",mOriginLatlng.longitude);
        intent.putExtra("origin",mExtraOrigin);
        intent.putExtra("destination",mExtraDestination);
        intent.putExtra("destination_lat",mDestinationLatlng.latitude);
        intent.putExtra("destination_lng",mDestinationLatlng.longitude);

        startActivity(intent);
        finish();
    }
    private void drawRoute(){
        mGoogleApiProvider.getDirections(mOriginLatlng,mDestinationLatlng).enqueue(new Callback<String>() {
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
                    mTextViewTime.setText(durationText);
                    mTextViewDistance.setText(distanceText);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.addMarker(new MarkerOptions().position(mOriginLatlng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_origin_route)));
        mMap.addMarker(new MarkerOptions().position(mDestinationLatlng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_destination_route)));

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                .target(mOriginLatlng)
                .zoom(14f)
                .build()
        ));
        drawRoute();
    }
}