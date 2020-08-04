package com.example.proyectosig.view.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.proyectosig.R;
import com.example.proyectosig.models.ClientService;
import com.example.proyectosig.models.FCMBody;
import com.example.proyectosig.models.FCMResponse;
import com.example.proyectosig.providers.AuthProvider;
import com.example.proyectosig.providers.ClientServiceProvider;
import com.example.proyectosig.providers.GeofireProvider;
import com.example.proyectosig.providers.GoogleApiProvider;
import com.example.proyectosig.providers.NotificationProvider;
import com.example.proyectosig.providers.TokenProvider;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestConductorActivity extends AppCompatActivity {
    private LottieAnimationView mAnimation;
    private TextView mTextviewLookingFor;
    private Button mButtonCancelRequest;

    private String mExtraOrigin;
    private String mExtraDestination;

    private GoogleApiProvider mGoogleApiProvider;


    private GeofireProvider mGeofireProvider;
    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private double mExtraDestinationLat;
    private double mExtraDestinationLng;
    private LatLng mOriginLatlng;
    private LatLng mDestinationLatlng;

    private double mRadius=0.1;

    private boolean mConductorFoud=false;
    private String mIDConductorFound ="";
    private LatLng mConductorLatlng;

    private NotificationProvider mNotificationProvider;
    private TokenProvider mTokenProvider;
    private ClientServiceProvider mClientServiceProvider;
    private AuthProvider mAuthProvider;

    private ValueEventListener mListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_conductor);

        mAnimation=findViewById(R.id.animation);
        mTextviewLookingFor=findViewById(R.id.txtViewLookingFor);
        mButtonCancelRequest=findViewById(R.id.btnCancelRequest);

        mAnimation.playAnimation();

        mExtraOrigin=getIntent().getStringExtra("origin");
        mExtraDestination=getIntent().getStringExtra("destination");

        mExtraOriginLat=getIntent().getDoubleExtra("origin_lat",0);
        mExtraOriginLng=getIntent().getDoubleExtra("origin_lng",0);

        mExtraDestinationLat=getIntent().getDoubleExtra("destination_lat",0);
        mExtraDestinationLng=getIntent().getDoubleExtra("destination_lng",0);

        mOriginLatlng=new LatLng(mExtraOriginLat,mExtraOriginLng);
        mDestinationLatlng=new LatLng(mExtraDestinationLat,mExtraDestinationLng);

        mGoogleApiProvider=new GoogleApiProvider(RequestConductorActivity.this);

        mGeofireProvider=new GeofireProvider("conductor_activo");
        mNotificationProvider=new NotificationProvider();
        mTokenProvider=new TokenProvider();
        mClientServiceProvider=new ClientServiceProvider();
        mAuthProvider=new AuthProvider();

        getClosesConductor();
    }
    private void getClosesConductor(){
        mGeofireProvider.getActiveConductor(mOriginLatlng,mRadius).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!mConductorFoud){
                    mConductorFoud=true;
                    mIDConductorFound=key;
                    mConductorLatlng=new LatLng(location.latitude,location.longitude);
                    mTextviewLookingFor.setText("Conductor Encontrado\nEsperando respuesta");
                    createClientService();

                    Log.d("Conductor","id: "+mIDConductorFound);
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
//cuando termina la busques de conductor de radio de 5 km
                if(!mConductorFoud){
                    mRadius=mRadius+0.1f;
                    if(mRadius>2){
                        mTextviewLookingFor.setText("No hay conductores disponibles");
                        Toast.makeText(RequestConductorActivity.this, "No hay conductores disponibles", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{
                        getClosesConductor();
                    }

                }


            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }
    private void createClientService(){
        mGoogleApiProvider.getDirections(mOriginLatlng,mConductorLatlng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                //respuesta del servidor
                try {
                    JSONObject jsonObjet=new JSONObject(response.body());
                    JSONArray jsonArray=jsonObjet.getJSONArray("routes");
                    JSONObject route=jsonArray.getJSONObject(0);
                    JSONObject polylines=route.getJSONObject("overview_polyline");
                    String points=polylines.getString("points");

                    JSONArray legs=route.getJSONArray("legs");
                    JSONObject leg=legs.getJSONObject(0);
                    JSONObject distance=leg.getJSONObject("distance");
                    JSONObject duration=leg.getJSONObject("duration");
                    String distanceText=distance.getString("text");
                    String durationText=duration.getString("text");
                    sendNotication(durationText,distanceText);

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
    private void sendNotication(final String time, final String km){
        mTokenProvider.gettoken(mIDConductorFound).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String token=dataSnapshot.child("token").getValue().toString();
                    Map<String,String> map=new HashMap<>();
                    map.put("title","Solicitud de servicio"+" a "+time+" de tu posicion");
                    map.put("body","Un cliente esta soliciatando un servicio a una distancia de "+
                            km+"\n"+"Recoger en:" +mExtraOrigin+"\n"+"Destino: "+mExtraDestination);
                    map.put("idClient",mAuthProvider.getId());
                    FCMBody fcmBody=new FCMBody(token,"high",map);
                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if(response.body()!=null) {
                                if(response.body().getSuccess()==1) {
                                    ClientService clientService=new ClientService(
                                            mAuthProvider.getId(),
                                            mIDConductorFound,
                                            mExtraDestination,
                                            mExtraOrigin,
                                            time,
                                            km,
                                            "create",
                                            mExtraOriginLat,
                                            mExtraOriginLng,
                                            mExtraDestinationLat,
                                            mExtraDestinationLng
                                    );
                                    mClientServiceProvider.create(clientService).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            checkStatusClientService();
                                        }

                                    });
                                    //Toast.makeText(RequestConductorActivity.this, "La notification se ha enviado correctamente", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(RequestConductorActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                Toast.makeText(RequestConductorActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error","Error "+t.getMessage());

                        }
                    });
                }
                else {
                    Toast.makeText(RequestConductorActivity.this, "no se pudo enviar la notificacion, porque el conductor no ha inicio de sesion", Toast.LENGTH_SHORT).show();
                }
                }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void checkStatusClientService(){
        mListener= mClientServiceProvider.getStatus(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String status=dataSnapshot.getValue().toString();
                    if(status.equals("accept")){
                        Intent intent=new Intent(RequestConductorActivity.this, MapClientServiceActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else if(status.equals("cancel")){
                        Toast.makeText(RequestConductorActivity.this, "El conductor no acepto el viaje", Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(RequestConductorActivity.this,MapClienteActivity.class);
                        startActivity(intent);
                        finish();
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
            mClientServiceProvider.getStatus(mAuthProvider.getId()).removeEventListener(mListener);
        }
    }
}
