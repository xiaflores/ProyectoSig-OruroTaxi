package com.example.proyectosig.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyectosig.R;
import com.example.proyectosig.view.cliente.MapClienteActivity;
import com.example.proyectosig.view.cliente.RegisterActivity;
import com.example.proyectosig.view.conductor.MapConductorActivity;
import com.example.proyectosig.view.conductor.RegisterConductorActivity;
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

    ImageButton mImageButtonLogin;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    AlertDialog mDialog;

    TextView mRegisterUser;
    TextView mForgotPassword;
    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MyToolbar.show(this,"Iniciar sesion",true);

        mTextInputEmail=findViewById(R.id.textInputEmail);
        mTextInputPassword=findViewById(R.id.textInputPassword);
        mImageButtonLogin=findViewById(R.id.btnLogin);
        mRegisterUser=findViewById(R.id.txtRegisterUser);
        mForgotPassword=findViewById(R.id.txtForgotPassword);
        mAuth= FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference();

        mPref=getApplicationContext().getSharedPreferences("TypeUser",MODE_PRIVATE);
        mDialog=new SpotsDialog.Builder().setContext(LoginActivity.this).setMessage("Espere un momemnto").build();

        mImageButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    login();
            }
        });
        mRegisterUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRegister();
            }
        });
        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotPassword();
            }
        });
    }
    private void login(){
        String email = mTextInputEmail.getText().toString();
        String password = mTextInputPassword.getText().toString();
        if(!email.isEmpty() && !password.isEmpty()){
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "El email o password son incorrectos", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            checkIfEmailVerified();
                        }
                    }
                });
        }
        else{
            Toast.makeText(this, "la contraseña y el correo son obligatorios", Toast.LENGTH_SHORT).show();
        }
    }
    private void checkIfEmailVerified()
    {
        String user = mPref.getString("user", "");
        if (mAuth.getCurrentUser().isEmailVerified())
        {
            mDialog.show();
            if (user.equals("cliente")) {
                Intent intent = new Intent(LoginActivity.this, MapClienteActivity.class);
                intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            else {
                Intent intent = new Intent(LoginActivity.this, MapConductorActivity.class);
                intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            // user is verified, so you can finish this activity or send user to activity which you want.
            finish();
            Toast.makeText(LoginActivity.this, "El login se realizo exitosamente", Toast.LENGTH_SHORT).show();
        }
        else
        {
            // email is not verified, so just prompt the message to the user and restart this activity.
            // NOTE: don't forget to log out the user.
            Toast.makeText(LoginActivity.this, "verifique su correo electronico en su bandeja de entrada", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            //restart this activity
        }
    }
    public void goToRegister(){
        String typeUser=mPref.getString("user","");
        if(typeUser.equals(("cliente"))){
            Intent intent=new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
        else{
            Intent intent=new Intent(LoginActivity.this, RegisterConductorActivity.class);
            startActivity(intent);
        }
    }
    public void forgotPassword(){
        Intent intent=new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }
}