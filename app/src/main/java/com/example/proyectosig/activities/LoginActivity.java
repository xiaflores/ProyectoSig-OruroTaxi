package com.example.proyectosig.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.proyectosig.R;
import com.example.proyectosig.activities.cliente.MapClienteActivity;
import com.example.proyectosig.activities.conductor.MapConductorActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dmax.dialog.SpotsDialog;
import com.example.proyectosig.includes.MyToolbar;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText mTextInputEmail;
    TextInputEditText mTextInputPassword;
    Button mButtonLogin;


    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    AlertDialog mDialog;

    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MyToolbar.show(this,"Login de usuario",true);

        mTextInputEmail=findViewById(R.id.textInputEmail);
        mTextInputPassword=findViewById(R.id.textInputPassword);
        mButtonLogin=findViewById(R.id.btnLogin);

        mAuth= FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference();

        mPref=getApplicationContext().getSharedPreferences("TypeUser",MODE_PRIVATE);

        mDialog=new SpotsDialog.Builder().setContext(LoginActivity.this).setMessage("Espere un momemnto").build();

        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }
    private void login(){
        String email= mTextInputEmail.getText().toString();
        String password= mTextInputPassword.getText().toString();
        if(!email.isEmpty() && !password.isEmpty()){

            if(password.length()>=6){
                mDialog.show();
                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            String user=mPref.getString("user","");
                            if(user.equals("cliente")){
                                Intent intent=new Intent(LoginActivity.this , MapClienteActivity.class);
                                intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                            else{
                                Intent intent=new Intent(LoginActivity.this , MapConductorActivity.class);
                                intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                            }

                            Toast.makeText(LoginActivity.this, "El login se realizo exitosamente", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(LoginActivity.this, "La contraseña o el password son incorrectos", Toast.LENGTH_SHORT).show();
                        }
                        mDialog.dismiss();
                    }
                });
            }
            else{
                Toast.makeText(this, "La contraseña debe tener mas de 6 caracteres", Toast.LENGTH_SHORT).show();
            }

        }
        else{
            Toast.makeText(this, "la contraseña y el correo son obligatorios", Toast.LENGTH_SHORT).show();
        }
    }

}