package com.example.proyectosig.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.proyectosig.R;
import com.example.proyectosig.activities.cliente.RegisterActivity;
import com.example.proyectosig.activities.conductor.RegisterConductorActivity;

public class SelectOptionAuthActivity extends AppCompatActivity {
    Toolbar mToolbar;
    Button mButtonGoToLogin;
    Button mButtonGoToRegister;

    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_option_auth);
        mToolbar= findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Seleccionar opcion");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPref=getApplicationContext().getSharedPreferences("TypeUser",MODE_PRIVATE);

        mButtonGoToLogin = findViewById(R.id.btnToLogin);
        mButtonGoToRegister=findViewById(R.id.btnGoToRegister);
        mButtonGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogin();
            }
        });
        mButtonGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegister();
            }
        });


    }
    public void goToLogin(){
        Intent intent=new Intent(SelectOptionAuthActivity.this, LoginActivity.class);
        startActivity(intent);
    }
    public void goToRegister(){
        String typeUser=mPref.getString("user","");
        if(typeUser.equals(("cliente"))){
            Intent intent=new Intent(SelectOptionAuthActivity.this,RegisterActivity.class);
            startActivity(intent);
        }
        else{
            Intent intent=new Intent(SelectOptionAuthActivity.this, RegisterConductorActivity.class);
            startActivity(intent);
        }


    }
}