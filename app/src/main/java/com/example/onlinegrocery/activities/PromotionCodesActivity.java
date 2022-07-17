package com.example.onlinegrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.onlinegrocery.R;
import com.example.onlinegrocery.adapters.AdapterPromotionShop;
import com.example.onlinegrocery.models.ModelPromotion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PromotionCodesActivity extends AppCompatActivity {

    private ImageButton backBtn,addPromoBtn, filterBtn;
    private TextView filteredTv;
    private RecyclerView promoRv;

    private ArrayList<ModelPromotion> promotionList;
    private AdapterPromotionShop adapterPromotionShop;

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_codes);

        backBtn = findViewById(R.id.backBtn);
        addPromoBtn = findViewById(R.id.addPromoBtn);
        filterBtn = findViewById(R.id.filterBtn);
        filteredTv = findViewById(R.id.filteredTv);
        promoRv = findViewById(R.id.promoRv);
        firebaseAuth = FirebaseAuth.getInstance();

        loadAllPromoCode();
        //handle the filter btn click
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialog();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //handle click, open add promoCode activity
        addPromoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PromotionCodesActivity.this, AddPromotionCodeActivity.class));

            }
        });
    }

    private void filterDialog() {
        //option to display
        String [] options ={"All","Expired","Not Expired"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter Promotion Codes")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        //handle item click
                        if(i==0){
                            //all clicked
                            filteredTv.setText("All Promotion Codes");
                            loadAllPromoCode();
                        }else if(i==1){
                            //expired click
                            filteredTv.setText("Expired Promotion Codes");
                            loadExpiredPromoCode();
                        }else if(i==2){
                            //not expired click
                            filteredTv.setText("Not Expired Promotion Codes");
                            loadNotExpiredPromoCode();
                        }
                    }
                }).show();
    }

    private void loadAllPromoCode(){
        //init list
    promotionList = new ArrayList<>();

    //db references user>currentUser>promotions>codes data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data
                        promotionList.clear();

                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);
                            //add to lis
                            promotionList.add(modelPromotion);
                        }
                        //setup adapter
                        adapterPromotionShop =new AdapterPromotionShop(PromotionCodesActivity.this, promotionList);
                        //set adapter to recycler view
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
    private void loadExpiredPromoCode(){
        //get current date
        DecimalFormat mFormat = new DecimalFormat("00");
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        final String todayDate =day+ "/" + month +"/"+ year;

        promotionList = new ArrayList<>();

        //db references user>currentUser>promotions>codes data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data
                        promotionList.clear();

                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);
                            String expDate = modelPromotion.getExpireDate();
                            /*--check for  expired --*/
                            try{
                                SimpleDateFormat sdFormat = new SimpleDateFormat("dd/MM/yyyy");
                                Date currentDate = sdFormat.parse(todayDate);
                                Date expireDate = sdFormat.parse(expDate);
                                if(expireDate.compareTo(currentDate)>0){
                                    //date 1 occurs after date 2

                                }else if(expireDate.compareTo(currentDate)<0){
                                    //date 1 occur before date 2
                                    promotionList.add(modelPromotion);
                                }else if(expireDate.compareTo(currentDate)==0){
                                    //both date equals
                                }
                            }catch (Exception e){

                            }

                        }
                        //setup adapter
                        adapterPromotionShop =new AdapterPromotionShop(PromotionCodesActivity.this, promotionList);
                        //set adapter to recycler view
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadNotExpiredPromoCode(){

        DecimalFormat mFormat = new DecimalFormat("00");
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        final String todayDate =day+ "/" + month +"/"+ year;

        promotionList = new ArrayList<>();

        //db references user>currentUser>promotions>codes data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data
                        promotionList.clear();

                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);
                            String expDate = modelPromotion.getExpireDate();
                            /*--check for  expired --*/
                            try{
                                SimpleDateFormat sdFormat = new SimpleDateFormat("dd/MM/yyyy");
                                Date currentDate = sdFormat.parse(todayDate);
                                Date expireDate = sdFormat.parse(expDate);
                                if(expireDate.compareTo(currentDate)>0){
                                    //date 1 occurs after date 2
                                    promotionList.add(modelPromotion);

                                }else if(expireDate.compareTo(currentDate)<0){
                                    //date 1 occur before date 2
                                }else if(expireDate.compareTo(currentDate)==0){

                                    //both date equals
                                    promotionList.add(modelPromotion);

                                }
                            }catch (Exception e){

                            }

                        }
                        //setup adapter
                        adapterPromotionShop =new AdapterPromotionShop(PromotionCodesActivity.this, promotionList);
                        //set adapter to recycler view
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}