package com.example.proyectosig.providers;

import com.example.proyectosig.models.Cliente;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ClienteProvider {
    DatabaseReference mDatabase;
    public ClienteProvider(){
        mDatabase= FirebaseDatabase.getInstance().getReference().child("User").child("cliente");
    }
    public Task<Void> create(Cliente cliente){
        Map<String,Object> map=new HashMap<>();
        map.put("name",cliente.getName());
        map.put("email",cliente.getEmail());
        return mDatabase.child(cliente.getId()).setValue(map);

    }
    public DatabaseReference getClient(String idClient){
        return mDatabase.child(idClient);
    }

}
