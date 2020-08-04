package com.example.proyectosig.view.conductor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.proyectosig.R;
import com.example.proyectosig.includes.MyToolbar;
import com.example.proyectosig.models.Conductor;
import com.example.proyectosig.providers.AuthProvider;
import com.example.proyectosig.providers.ConductorProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import dmax.dialog.SpotsDialog;

public class RegisterConductorActivity extends AppCompatActivity {

    AuthProvider mAuthPorvider;
    ConductorProvider mConductorProvider;

    Button mButtonRegister;
    TextInputEditText mTextInputEmail;
    TextInputEditText mTextInputName;
    TextInputEditText mTextInputMarca;
    TextInputEditText mTextInputPlaca;
    TextInputEditText mTextInputPassword;


    AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_conductor);

        MyToolbar.show(this,"Registro de Conductor",true);

        mAuthPorvider=new AuthProvider();
        mConductorProvider=new ConductorProvider();

        mDialog=new SpotsDialog.Builder().setContext(RegisterConductorActivity.this).setMessage("Espere un momemnto").build();

        mButtonRegister=findViewById(R.id.btnRegister);
        mTextInputName=findViewById(R.id.textInputName);
        mTextInputEmail=findViewById(R.id.textInputEmail);
        mTextInputMarca=findViewById(R.id.textInputMarca);
        mTextInputPlaca=findViewById(R.id.textInputPlaca);
        mTextInputPassword=findViewById(R.id.textInputPassword);


        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickRegister();
            }
        });
    }
    void ClickRegister(){
        final String name=mTextInputName.getText().toString();
        final String email=mTextInputEmail.getText().toString();
        final String marca=mTextInputMarca.getText().toString();
        final String placa=mTextInputPlaca.getText().toString();
        final String password=mTextInputPassword.getText().toString();
        if(!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !marca.isEmpty() && !placa.isEmpty() ){
            if(password.length()>=6){
                mDialog.show();
                register(name,email,password,marca,placa);
            }
            else{
                Toast.makeText(this, "la contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Ingrese todos los campos", Toast.LENGTH_SHORT).show();
        }
    }
    void register(final String name,final String email, String password,final String marca,final String placa)
    {
        mAuthPorvider.register(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialog.hide();
                if(task.isSuccessful()){
                    String id= FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Conductor conductor=new Conductor(id,name,email,marca,placa);
                    create(conductor);

                }
                else{
                    Toast.makeText(RegisterConductorActivity.this, "No se pudo registrar la usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    void create(Conductor conductor){
        mConductorProvider.create(conductor).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Intent intent=new Intent(RegisterConductorActivity.this , MapConductorActivity.class);
                    intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(RegisterConductorActivity.this, "No se pudo crear el usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}