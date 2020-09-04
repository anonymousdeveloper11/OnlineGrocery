package com.example.onlinegrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onlinegrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddPromotionCodeActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private EditText promoCodeEt, promoDescriptionEt,promoPriceEt,minimumOrderPriceEt;
    private TextView expireDateTv,titleTv;
    private Button addBtn;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private String promoId;
    private boolean isUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_promotion_code);
        backBtn = findViewById(R.id.backBtn);
        promoCodeEt = findViewById(R.id.promoCodeEt);
        promoDescriptionEt = findViewById(R.id.promoDescriptionEt);
        promoPriceEt = findViewById(R.id.promoPriceEt);
        minimumOrderPriceEt = findViewById(R.id.minimumOrderPriceEt);
        expireDateTv = findViewById(R.id.expireDateTv);
        addBtn = findViewById(R.id.addBtn);
        titleTv = findViewById(R.id.titleTv);

        firebaseAuth = FirebaseAuth.getInstance();

        //init progressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);


        //get promo id from intent
        Intent intent = getIntent();
        if(intent.getStringExtra("promoId")!=null){
            //come here from adapter to update record
            promoId =intent.getStringExtra("promoId");
            titleTv.setText("Update Promotion Code");
            addBtn.setText("Update");

            isUpdating= true;
            loadPromoInfo();//load promotion code info to set in our views, so we can also update single value
        } else{
            //come here from promo code list activity to add new promo code
            titleTv.setText("Add Promotion Code");
            addBtn.setText("Add");

            isUpdating= false;
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //handle click, pick Date
        expireDateTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickDialog();
            }
        });

        //handle click, add Promotion code to firebase db
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();

            }
        });
    }

    private void loadPromoInfo() {
        //db path to promo code
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions").child(promoId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                   //get info of promo Code
                   String id = ""+snapshot.child("id").getValue();
                   String timestamp =""+snapshot.child("timestamp").getValue();
                   String description =""+snapshot.child("description").getValue();
                   String promoCode =""+snapshot.child("promoCode").getValue();
                   String promoPrice =""+snapshot.child("promoPrice").getValue();
                   String minimumOrderPrice =""+snapshot.child("minimumOrderPrice").getValue();
                   String expireDate =""+snapshot.child("expireDate").getValue();

                   //set data
                        promoCodeEt.setText(promoCode);
                        promoDescriptionEt.setText(description);
                        promoPriceEt.setText(promoPrice);
                        minimumOrderPriceEt.setText(minimumOrderPrice);
                        expireDateTv.setText(expireDate);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void datePickDialog() {
        //get current date to set on calender
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        //date pick dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                DecimalFormat mFormat = new DecimalFormat("00");
                String pDay =mFormat.format(dayOfMonth);
                String pMonth = mFormat.format(monthOfYear);
                String pYear = ""+year;
                String pDate =pDay + "/" + pMonth + "/" + pYear;// eg dd/mm/yy
                expireDateTv.setText(pDate);
            }
        },mYear, mMonth, mDay);
        //show dialog
        datePickerDialog.show();
        //disable past date section on calender
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
    }
    private String description, promoCode, promoPrice, minimumOrderPrice, expireDate;
    private void inputData(){
        //input data
        promoCode=promoCodeEt.getText().toString().trim();
        description = promoDescriptionEt.getText().toString().trim();
        promoPrice = promoPriceEt.getText().toString().trim();
        minimumOrderPrice = minimumOrderPriceEt.getText().toString().trim();
        expireDate =expireDateTv.getText().toString().trim();

        //validate from data
        if(TextUtils.isEmpty(promoCode)){
            Toast.makeText(this, "Enter Promo Code..", Toast.LENGTH_SHORT).show();
            return;//don't further proceed
        }
        if(TextUtils.isEmpty(description)){
            Toast.makeText(this, "Enter promo Description..", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(promoPrice)){
            Toast.makeText(this, "Enter promo Price...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(minimumOrderPrice)){
            Toast.makeText(this, "Enter minimum Order Price", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(expireDate)){
            Toast.makeText(this, "Choose expire Date", Toast.LENGTH_SHORT).show();
        }


        //all field entered ,add/update
        //to db
        if(isUpdating){
            //update data
            updateDataToDb();
        }else{
            addDataToDb();
        }


    }

    private void updateDataToDb() {
        progressDialog.setMessage("Updating Promotion code..");
        progressDialog.show();


        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("description", ""+description);
        hashMap.put("promoCode",""+promoCode);
        hashMap.put("promoPrice",""+promoPrice);
        hashMap.put("minimumOrderPrice", ""+minimumOrderPrice);
        hashMap.put("expireDate",""+expireDate);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions").child(promoId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
           //update
                        progressDialog.dismiss();
                        Toast.makeText(AddPromotionCodeActivity.this, "Updated..", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//failed to update
                progressDialog.dismiss();
                Toast.makeText(AddPromotionCodeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addDataToDb() {
        progressDialog.setMessage("Adding Promotion code..");
        progressDialog.show();

        String timestamp =""+System.currentTimeMillis();
        //setup data to db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("timestamp", ""+timestamp);
        hashMap.put("description", ""+description);
        hashMap.put("promoCode",""+promoCode);
        hashMap.put("promoPrice",""+promoPrice);
        hashMap.put("minimumOrderPrice", ""+minimumOrderPrice);
        hashMap.put("expireDate",""+expireDate);

        //init db refrence Users>currentUser> Promotions>PromoId>Promo data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //code added
                        progressDialog.dismiss();
                        Toast.makeText(AddPromotionCodeActivity.this, "Promotion Code added..", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed to adding code
                progressDialog.dismiss();
                Toast.makeText(AddPromotionCodeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}