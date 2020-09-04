package com.example.onlinegrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onlinegrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class WriteReviewActivity extends AppCompatActivity {
    private  String shopUid;

    private ImageButton backBtn;
    private ImageView profileIv;
    private TextView shopNameTv, labelTv;
    private RatingBar ratingBar;
    private EditText reviewEt;
    private FloatingActionButton submitBtn;

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);
        //get shop uid from intent
        shopUid = getIntent().getStringExtra("shopUid");

        backBtn = findViewById(R.id.backBtn);
        profileIv = findViewById(R.id.profileIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        labelTv = findViewById(R.id.labelTv);
        ratingBar = findViewById(R.id.ratingBar);
        reviewEt = findViewById(R.id.reviewEt);
        submitBtn = findViewById(R.id.submitBtn);

        firebaseAuth =FirebaseAuth.getInstance();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //input data
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });

        //if user has written review to this shop ,load it
        loadMyReview();

        //load shop info: shopName, shopImage
        loadShopInfo();
    }

    private void loadShopInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //get shop info
                String shopName = ""+dataSnapshot.child("shopName").getValue();
                String shopImage =""+dataSnapshot.child("profileImage").getValue();

                //set shop info to ui
                shopNameTv.setText(shopName);

                try {
                    Picasso.get().load(shopImage).placeholder(R.drawable.ic_store).into(profileIv);

                }catch (Exception e){

                    profileIv.setImageResource(R.drawable.ic_store);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMyReview() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    //my review is available in this shop
                    //get review
                    String uid = ""+dataSnapshot.child("uid").getValue();
                    String rating =""+dataSnapshot.child("rating").getValue();
                    String review =""+dataSnapshot.child("review").getValue();
                    String timeStamp =""+dataSnapshot.child("timestamp").getValue();

                    //set review details to our ui
                    float myRating = Float.parseFloat(rating);
                    ratingBar.setRating(myRating);
                    reviewEt.setText(review);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void inputData() {

        String rating =""+ratingBar.getRating();
        String review =""+reviewEt.getText().toString().trim();
        //for time of review
        String timestamp =""+System.currentTimeMillis();

        //setup data in hashMap
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+firebaseAuth.getUid());
        hashMap.put("rating", ""+ rating);
        hashMap.put("review", ""+ review);
        hashMap.put("timestamp", ""+ timestamp);

        //put to db:DB>Users>shopUid>Rating
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //review added to db
                        Toast.makeText(WriteReviewActivity.this, "Review publish successfully", Toast.LENGTH_SHORT).show();


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                //failed to add
                Toast.makeText(WriteReviewActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }
}