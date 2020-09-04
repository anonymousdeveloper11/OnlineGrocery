package com.example.onlinegrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.onlinegrocery.R;
import com.example.onlinegrocery.adapters.AdapterReview;
import com.example.onlinegrocery.models.ModelReview;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopReviewsActivity extends AppCompatActivity {

    private String shopUid;

    private ImageButton backBtn;
    private ImageView profileIv;
    private TextView shopNameTv, ratingsTv;
    private RatingBar ratingBar;
    private RecyclerView reviewsRv;
    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelReview> reviewList;
    private AdapterReview adapterReview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_reviews);

        //get shopUid from intent
        shopUid = getIntent().getStringExtra("shopUid");


        backBtn = findViewById(R.id.backBtn);
        profileIv = findViewById(R.id.profileIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        ratingsTv =findViewById(R.id.ratingsTv);
        ratingBar = findViewById(R.id.ratingBar);
        reviewsRv = findViewById(R.id.reviewsRv);

        firebaseAuth = FirebaseAuth.getInstance();
        loadShopDetails();//for shopName, image and avg rating

        loadReviews();//for reviews list , avg rating

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private float ratingSum=0;
    private void loadReviews() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                reviewList = new ArrayList<>();
                //clear list before adding data into it
                reviewList.clear();

                ratingSum=0;
                for(DataSnapshot ds: dataSnapshot.getChildren());
                float rating = Float.parseFloat(""+dataSnapshot.child("rating").getValue());
                ratingSum = ratingSum+rating;//for avg rating add(additionalof)all ratings , laate will divide it by number of reviews
                ModelReview modelReview = dataSnapshot.getValue(ModelReview.class);
                reviewList.add(modelReview);
                //setup adapter

                adapterReview = new AdapterReview(ShopReviewsActivity.this, reviewList);
                //set to recycleView

                reviewsRv.setAdapter(adapterReview);
                Long numberOfReviews = dataSnapshot.getChildrenCount();
                float avgRating = ratingSum/numberOfReviews;
                ratingsTv.setText(String.format("%.2f", avgRating)+ "["+numberOfReviews+"]");//eg.4.7[10]
                ratingBar.setRating(avgRating);

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void loadShopDetails() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String shopName = ""+dataSnapshot.child("shopName").getValue();
                String profileImage = ""+dataSnapshot.child("profileImage").getValue();

                shopNameTv.setText(shopName);
                try {
                    Picasso.get().load(profileImage).placeholder(R.drawable.ic_store).into(profileIv);
                }catch (Exception e){
                    profileIv.setImageResource(R.drawable.ic_store);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}