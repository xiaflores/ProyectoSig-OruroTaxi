package com.example.proyectosig.providers;

import com.example.proyectosig.models.ClientService;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ClientServiceProvider {
    private DatabaseReference mDatabase;
    public ClientServiceProvider(){
        mDatabase= FirebaseDatabase.getInstance().getReference().child("ClienteService");

    }
    public Task<Void> create(ClientService clientService){
        return mDatabase.child(clientService.getIdClient()).setValue(clientService);
    }
    public Task<Void> updateStatus(String idClientService,String status){
        Map<String,Object> map=new HashMap<>();
        map.put("status",status);
        return mDatabase.child(idClientService).updateChildren(map);
    }
    public DatabaseReference getStatus(String idClientService){
        return mDatabase.child(idClientService).child("status");

    }
    public DatabaseReference getClientService(String idClientService){
        return mDatabase.child(idClientService);

    }
}
