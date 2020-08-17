package com.example.proyectosig.view.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.proyectosig.R;
import com.example.proyectosig.models.Cliente;
import com.example.proyectosig.view.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;

import dmax.dialog.SpotsDialog;
import com.example.proyectosig.includes.MyToolbar;
import com.example.proyectosig.providers.AuthProvider;
import com.example.proyectosig.providers.ClienteProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {


    AuthProvider mAuthPorvider;
    ClienteProvider mClienteProvider;

    Button mButtonRegister;
    TextInputEditText mTextInputEmail;
    TextInputEditText mTextInputName;
    TextInputEditText mTextInputPassword;
    TextInputEditText mTextConfirmePassword;

    AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        MyToolbar.show(this,"Registro de usuario",true);

        mAuthPorvider=new AuthProvider();
        mClienteProvider=new ClienteProvider();

        mDialog=new SpotsDialog.Builder().setContext(RegisterActivity.this).setMessage("Espere un momemento").build();

        mButtonRegister=findViewById(R.id.btnRegister);
        mTextInputEmail=findViewById(R.id.textInputEmail);
        mTextInputPassword=findViewById(R.id.textInputPassword);
        mTextInputName=findViewById(R.id.textInputName);
        mTextConfirmePassword=findViewById(R.id.textConfirmePassword);

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
        final String password=mTextInputPassword.getText().toString();
        final String confirmepassword=mTextConfirmePassword.getText().toString();

        if(!name.isEmpty() && !email.isEmpty() && !password.isEmpty()){
            if(password.length()>=6){
                if(confirmepassword.equals(password))
                {
                    //mDialog.show();
                    register(name,email,password);
                }
                else{
                    Toast.makeText(this, "Verifique la contraseña", Toast.LENGTH_SHORT).show();
                }

            }
            else{
                Toast.makeText(this, "la contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Ingrese todos los campos", Toast.LENGTH_SHORT).show();
        }
    }
    void register(final String name,final String email,String password)
    {
        mAuthPorvider.register(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialog.hide();
                if(task.isSuccessful()){
                    String id= FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Cliente cliente=new Cliente(id,name,email);
                    create(cliente);
                    mAuthPorvider.sendVerificatioEmail();
                    Toast.makeText(RegisterActivity.this, "Confirme su correo", Toast.LENGTH_SHORT).show();
                    mAuthPorvider.logout();
                }
                else{
                    Toast.makeText(RegisterActivity.this, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void create(Cliente cliente){
        mClienteProvider.create(cliente).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Intent intent=new Intent(RegisterActivity.this , LoginActivity.class);
                    intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(RegisterActivity.this, "No se pudo crear el usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}