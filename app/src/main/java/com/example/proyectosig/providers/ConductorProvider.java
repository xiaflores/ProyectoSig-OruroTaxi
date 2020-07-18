package com.example.proyectosig.providers;

import com.example.proyectosig.models.Conductor;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConductorProvider {
    DatabaseReference mDatabase;
    public ConductorProvider(){
        mDatabase= FirebaseDatabase.getInstance().getReference().child("User").child("conductor");
    }
    public Task<Void> create(Conductor conductor){

        return mDatabase.child(conductor.getId()).setValue(conductor);
    }
}
