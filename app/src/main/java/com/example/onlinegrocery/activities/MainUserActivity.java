package com.example.onlinegrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onlinegrocery.R;
import com.example.onlinegrocery.adapters.AdapterOrderUser;
import com.example.onlinegrocery.adapters.AdapterShop;
import com.example.onlinegrocery.models.ModelOrderUser;
import com.example.onlinegrocery.models.ModelShop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainUserActivity extends AppCompatActivity {

    private TextView nameTv, emailTv, phoneTv, tabShopsTv, tabOrdersTv;
    private ImageButton logoutBtn, settingBtn;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private ImageButton imgBtn;
    private ImageView profileIv;
    private RelativeLayout shopsRl, ordersRl;
    private RecyclerView shopsRv, ordersRv;
    private ArrayList<ModelShop> shopsList;
    private AdapterShop adapterShop;

    private ArrayList<ModelOrderUser> ordersList;
    private AdapterOrderUser adapterOrderUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        nameTv= findViewById(R.id.user_name);
        logoutBtn = findViewById(R.id.user_logoutbtn);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        imgBtn = findViewById(R.id.user_editBtn);
        profileIv = findViewById(R.id.profile);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        tabShopsTv = findViewById(R.id.tabShopsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        shopsRl = findViewById(R.id.shopsRl);
        ordersRl = findViewById(R.id.ordersRl);
        shopsRv = findViewById(R.id.shopsRv);
        ordersRv = findViewById(R.id.ordersRv);
        settingBtn = findViewById(R.id.settingBtn);

        //at start show shops ui
        showShopsUI();

        //start setting screen
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainUserActivity.this, SettingsActivity.class));
            }
        });

        tabShopsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShopsUI();
            }
        });

        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrdersUI();
            }
        });
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainUserActivity.this, EditUserActivity.class));
            }
        });

        checkUser();


        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                makeMeOffline();
            }
        });




    }

    private void showShopsUI() {
        //show shopsUI and hide ordersUI
        shopsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabShopsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }

    private void showOrdersUI(){
        //show ordersUI and hide shopsUI
        ordersRl.setVisibility(View.VISIBLE);
        shopsRl.setVisibility(View.GONE);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }

    private void makeMeOffline() {
        progressDialog.setMessage("Logging out User..");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

        //update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //update successful
                firebaseAuth.signOut();
                checkUser();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed updating
                progressDialog.dismiss();
                Toast.makeText(MainUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


}

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user==null){
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
            finish();

        }
        else{
            loadMyInfo();

        }
    }

    private void loadMyInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    //get user data
                    String fullName = "" +ds.child("fullName").getValue();
                    String email ="" +ds.child("email").getValue();
                    String phone = "" +ds.child("phone").getValue();
                    String profileImage ="" +ds.child("profileImage").getValue();
                    String city ="" +ds.child("city").getValue();
                    String accountType =""+ds.child("accountType").getValue();

                    //set userdata
                    nameTv.setText(fullName);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try{
                        Picasso.get().load(profileImage).placeholder(R.drawable.ic_person).into(profileIv);

                    }catch(Exception e){
                        profileIv.setImageResource(R.drawable.ic_person);

                    }
                    //load only those shops that are in the city of user
                    loadShops(city);

                    loadOrders();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadOrders() {
        //init order list
        ordersList = new ArrayList<>();
        //get orders
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ordersList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    String uid =""+ds.getRef().getKey();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    ref.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                               if(dataSnapshot.exists()){
                                   for(DataSnapshot ds: dataSnapshot.getChildren()){
                                       ModelOrderUser modelOrderUser = ds.getValue(ModelOrderUser.class);
                                       //add to list
                                       ordersList.add(modelOrderUser);
                                   }
                                   //setup adapter
                                   adapterOrderUser = new AdapterOrderUser(MainUserActivity.this, ordersList);
                                   //set to recycler view
                                   ordersRv.setAdapter(adapterOrderUser);

                               }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadShops(final String myCity) {
        //init list
        shopsList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo("Seller").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //clear list before adding
                shopsList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelShop modelShop = ds.getValue(ModelShop.class);
                    String shopCity ="" +ds.child("city").getValue();

                    //show only user city shops
                    if(shopCity.equals(myCity)){
                        shopsList.add(modelShop);
                    }
                    //if you want to display all shops skip the if statement and add this
                   // shopsList.add(modelShop);
                }
                //setup adapter
                adapterShop = new AdapterShop(MainUserActivity.this, shopsList);
                //set adapter to recyclerView
                shopsRv.setAdapter(adapterShop);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}