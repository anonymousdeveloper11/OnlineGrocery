package com.example.onlinegrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.onlinegrocery.R;
import com.example.onlinegrocery.adapters.AdapterOrderedItem;
import com.example.onlinegrocery.models.ModelOrderedItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OrderDetailsUsersActivity extends AppCompatActivity {

    private String orderTo, orderId;
    private ImageButton backBtn, writeReviewBtn;
    private TextView orderIdTv, dateTv,orderStatusTv,shopNameTv, totalItemTv,amountTv,addressTv;
    private RecyclerView itemsRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderedItem> orderedItemList;
    private AdapterOrderedItem adapterOrderedItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_users);

        backBtn =findViewById(R.id.backBtn);
        orderIdTv = findViewById(R.id.orderIdTv);
        dateTv = findViewById(R.id.dateTv);
        orderStatusTv = findViewById(R.id.orderStatusTv);
        shopNameTv = findViewById(R.id.shopNameTv);
        totalItemTv = findViewById(R.id.totalItemTv);
        amountTv = findViewById(R.id.amountTv);
        addressTv = findViewById(R.id.addressTv);
        itemsRv =findViewById(R.id.itemsRv);
        writeReviewBtn = findViewById(R.id.writeReviewBtn);
        firebaseAuth = FirebaseAuth.getInstance();

        //now got those values through intent on orderDetailsUsersActivity
        Intent intent = getIntent();
        orderTo = intent.getStringExtra("OrderTo");//orderTo  contains uid of the shop where we placed
        orderId = intent.getStringExtra("orderId");
        loadShopInfo();

        loadOrderDetails();
        loadOrderedItems();
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //handle write reviewBtn click, start write review activity
        writeReviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(OrderDetailsUsersActivity.this, WriteReviewActivity.class);
                intent1.putExtra("shopUid", orderTo);
                startActivity(intent1);//write review to shop we must most have shop uid

            }
        });
    }



    private void loadOrderedItems() {
        //init list
        orderedItemList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   orderedItemList.clear();//before loading items clear list
                        for(DataSnapshot ds:dataSnapshot.getChildren()){
                            ModelOrderedItem modelOrderedItem = ds.getValue(ModelOrderedItem.class);
                            //add to list
                            orderedItemList.add(modelOrderedItem);
                        }
                        //all item added to list
                        //setup adapter
                        adapterOrderedItem = new AdapterOrderedItem(OrderDetailsUsersActivity.this, orderedItemList);

                        //set adapter
                        itemsRv.setAdapter(adapterOrderedItem);

                        //set item count
                        totalItemTv.setText(""+dataSnapshot.getChildrenCount());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void loadOrderDetails() {

        //load order details
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo).child("Orders").child(orderId)
                //.child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //get data
                        String orderBy =""+dataSnapshot.child("orderBy").getValue();
                        String orderCost =""+dataSnapshot.child("orderCost").getValue();
                        String orderId =""+dataSnapshot.child("orderId").getValue();
                        String orderStatus =""+dataSnapshot.child("orderStatus").getValue();
                        String orderTime =""+dataSnapshot.child("orderTime").getValue();
                        String orderTo =""+dataSnapshot.child("orderTo").getValue();
                        String deliveryFee = ""+dataSnapshot.child("deliveryFee").getValue();
                        String latitude =""+dataSnapshot.child("latitude").getValue();
                        String longitude =""+dataSnapshot.child("longitude").getValue();
                        String discount =""+dataSnapshot.child("discount").getValue();//in previous order this will be null

                        if(discount.equals("null") || discount.equals("0")){
                            //value is either null or 0
                            discount ="& Discount Rs 0";
                        } else {
                            discount = "& Discount Rs "+discount;
                        }

                        //convert timestamp to proper format
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String  formatDate = DateFormat.format("dd/MM/yy hh:mm a", calendar).toString(); //eg 20/08/2020 12:12 pm
                        if(orderStatus.equals("In Progress")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }else if(orderStatus.equals("Completed")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorGreen));
                        }else if(orderStatus .equals("Cancelled")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorRed));
                        }

                        //set data
                        orderIdTv.setText(orderId);
                        orderStatusTv.setText(orderStatus);
                        amountTv.setText("Rs "+orderCost+"[Including delivery Fee Rs"+deliveryFee+ "" +discount+"]");
                        dateTv.setText(formatDate);
                        findAddress(latitude, longitude);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void findAddress(String latitude, String longitude) {

        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);

        //find address, country ,city,state
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);
            String address = addresses.get(0).getAddressLine(0);//complete address
            addressTv.setText(address);

        }catch(Exception e){

        }

    }

    private void loadShopInfo() {

        //get shop info
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(orderTo).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String shopName = ""+dataSnapshot.child("shopName").getValue();
                shopNameTv.setText(shopName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}