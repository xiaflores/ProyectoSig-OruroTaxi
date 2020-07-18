package com.example.proyectosig.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.proyectosig.R;
import com.example.proyectosig.activities.cliente.MapClienteActivity;
import com.example.proyectosig.activities.conductor.MapConductorActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    Button mButtonCliente;
    Button mButtonConductor;
    SharedPreferences mPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPref=getApplicationContext().getSharedPreferences("TypeUser",MODE_PRIVATE);
        final SharedPreferences.Editor editor=mPref.edit();

        mButtonCliente=findViewById(R.id.btnCliente);
        mButtonConductor=findViewById(R.id.btnConductor);

        mButtonCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("user","cliente");
                editor.apply();
                goToSelectAuth();
            }
        });
        mButtonConductor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("user","conductor");
                editor.apply();
                goToSelectAuth();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            String user=mPref.getString("user","");
            if(user.equals("cliente")){
                Intent intent=new Intent(MainActivity.this , MapClienteActivity.class);
                intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            else{
                Intent intent=new Intent(MainActivity.this , MapConductorActivity.class);
                intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }

    private void goToSelectAuth()
    {
        Intent intent=new Intent(MainActivity.this, SelectOptionAuthActivity.class);
        startActivity(intent);
    }
}