package com.example.proyectosig.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyectosig.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import dmax.dialog.SpotsDialog;

public class ForgotPasswordActivity extends AppCompatActivity {
    TextView resest_email;
    Button btnResetPassword;
    AlertDialog mDialog;
    FirebaseAuth mAuth;
    String email="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        resest_email=findViewById(R.id.txtResetPassword);
        btnResetPassword=findViewById(R.id.btnReset);
        mAuth=FirebaseAuth.getInstance();
        mDialog=new SpotsDialog.Builder().setContext(ForgotPasswordActivity.this).setMessage("Espere un momemnto").build();
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = resest_email.getText().toString().trim();
                if(!email.isEmpty()) {
                resetPassword();
                }
                else{
                    Toast.makeText(ForgotPasswordActivity.this, "ingrese su correo electronico", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
    private void resetPassword() {
            mAuth.setLanguageCode("es");
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "se envio a su correo las instrucciones para reestablecer contraseña",
                                Toast.LENGTH_SHORT).show();

                    } else {
                        mDialog.dismiss();
                        Toast.makeText(getApplicationContext(),
                                "El correo enviado no existe", Toast.LENGTH_SHORT).show();
                    }

                }
            });

    }
}