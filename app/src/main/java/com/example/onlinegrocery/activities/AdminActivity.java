package com.example.onlinegrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.onlinegrocery.R;
import com.example.onlinegrocery.adapters.AdapterUser;
import com.example.onlinegrocery.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {

    private ImageButton ibLogout;
    private RecyclerView rvUser;
    private ArrayList<ModelUser> userList;
    private FirebaseAuth firebaseAuth;
    private AdapterUser adapterUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        ibLogout = findViewById(R.id.ib_logout);
        rvUser = findViewById(R.id.rv_user);

        firebaseAuth =FirebaseAuth.getInstance();
        loadUser();
        ibLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AdminActivity.this, "Logout", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loadUser() {
        userList = new ArrayList<>();
//        FirebaseUser currentUser =  firebaseAuth.getCurrentUser();
//        String userUid = currentUser.getUid();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

       // String user = currentUser.toString();
        //String currentUser = firebaseAuth.getCurrentUser().toString();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);
                    //St
                    userList.add(modelUser);

                }
                adapterUser = new AdapterUser(AdminActivity.this,userList);
                rvUser.setAdapter(adapterUser);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}