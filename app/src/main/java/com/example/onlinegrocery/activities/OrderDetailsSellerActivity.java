package com.example.onlinegrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.onlinegrocery.Constants;
import com.example.onlinegrocery.R;
import com.example.onlinegrocery.adapters.AdapterOrderedItem;
import com.example.onlinegrocery.models.ModelOrderedItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderDetailsSellerActivity extends AppCompatActivity {

    String orderId, orderBy;
    private ImageButton backBtn, editBtn,mapBtn;
    private TextView orderIdTv,dateTv,orderStatusTv,emailTv,phoneTv,totalItemsTv,amountTv,addressTv;
    private RecyclerView itemsRv;
    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderedItem> orderedItemList;
    private AdapterOrderedItem adapterOrderedItem;

    //to open destination in map
    private String sourceLatitude, sourceLongitude, destinationLatitude, destinationLongitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_seller);
    //get data from intent
        orderId = getIntent().getStringExtra("orderId");
        orderBy = getIntent().getStringExtra("orderBy");

        backBtn =findViewById(R.id.backBtn);
        editBtn = findViewById(R.id.editBtn);
        mapBtn = findViewById(R.id.mapBtn);
        orderIdTv = findViewById(R.id.orderIdTv);
        dateTv = findViewById(R.id.dateTv);
        orderStatusTv = findViewById(R.id.orderStatusTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        totalItemsTv = findViewById(R.id.totalItemsTv);
        amountTv = findViewById(R.id.amountTv);
        addressTv = findViewById(R.id.addressTv);
        itemsRv = findViewById(R.id.itemsRv);

        firebaseAuth = FirebaseAuth.getInstance();

        loadMyInfo();
        loadBuyerInfo();
        loadOrderDetails();
        loadOrderedItem();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();

            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           //edit order status :In progress, completed, cancelled
           editOrderStatusDialog();
            }
        });

    }

    private void editOrderStatusDialog() {

        //options to display
        final String [] options ={"In Progress","Completed","Cancelled"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Order Status")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item click
                        String selectedOption = options[which];
                        editOrderStatus(selectedOption);

                    }
                }).show();

    }

    private void editOrderStatus(final String selectedOption) {
        //setup data to put in firebase db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus", ""+selectedOption);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        String message = "Order is now"+selectedOption;
                        //status update
                        Toast.makeText(OrderDetailsSellerActivity.this, message, Toast.LENGTH_SHORT).show();
                        prepareNotificationMessage(orderId, message);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
           //failed to update status
                Toast.makeText(OrderDetailsSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrderDetails() {
        //load detailed info of the order based on orderId
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   //get order data
                   String orderId = ""+dataSnapshot.child("orderId").getValue();
                   String orderBy = ""+dataSnapshot.child("orderBy").getValue();
                   String orderCost =""+dataSnapshot.child("orderCost").getValue();
                   String orderStatus =""+dataSnapshot.child("orderStatus").getValue();
                   String orderTime =""+dataSnapshot.child("orderTime").getValue();
                   String orderTo =""+dataSnapshot.child("orderTo").getValue();
                   String deliveryFee =""+dataSnapshot.child("deliveryFee").getValue();
                   String latitude =""+dataSnapshot.child("latitude").getValue();
                   String longitude =""+dataSnapshot.child("longitude").getValue();

                        String discount =""+dataSnapshot.child("discount").getValue();//in previous order this will be null

                        if(discount.equals("null") || discount.equals("0")){
                            //value is either null or 0
                            discount ="& Discount $0";
                        } else {
                            discount = "& Discount $"+discount;
                        }
                   //convert timestamp
                        Calendar  calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String dateFormat = DateFormat.format("dd/MM/yyyy",calendar).toString();

                        //orderStatus
                        if(orderStatus.equals("In Progress")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if(orderStatus.equals("Completed")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorGreen));
                        }else if(orderStatus.equals("Cancelled")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorRed));
                        }
                        //set data
                        orderIdTv.setText(orderId);
                        orderStatusTv.setText(orderStatus);
                        dateTv.setText(dateFormat);
                        amountTv.setText("$"+orderCost+"[Including delivery Fee $"+deliveryFee+ "" +discount+"]");

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

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses =geocoder.getFromLocation(lat, lon, 1);
            //complete address
            String address = addresses.get(0).getAddressLine(0);
            addressTv.setText(address);
        }catch(Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    private void loadOrderedItem(){
        //load products/items of order
        //init list
        orderedItemList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        orderedItemList.clear();//before adding data clear list
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelOrderedItem modelOrderedItem = ds.getValue(ModelOrderedItem.class);
                            //add to list
                            orderedItemList.add(modelOrderedItem);
                        }
                        //setup adapter
                        adapterOrderedItem = new AdapterOrderedItem(OrderDetailsSellerActivity.this, orderedItemList);
                        //set adapter to our recycler View
                        itemsRv.setAdapter(adapterOrderedItem);
                        //set total no of its products in order
                        totalItemsTv.setText(""+dataSnapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        sourceLatitude = ""+dataSnapshot.child("latitude").getValue();
                        sourceLongitude =""+dataSnapshot.child("longitude").getValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadBuyerInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //get buyer Info
                        destinationLatitude = ""+dataSnapshot.child("latitude").getValue();
                        destinationLongitude =""+dataSnapshot.child("longitude").getValue();
                    String email = ""+dataSnapshot.child("email").getValue();
                    String phone =""+dataSnapshot.child("phone").getValue();

                    //set info
                        emailTv.setText(email);
                        phoneTv.setText(phone);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void openMap() {
        String address ="https://maps.google.com/maps?safddr=" + sourceLatitude + "," + sourceLongitude + "&daddr=" + destinationLatitude + "," +destinationLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void prepareNotificationMessage(String orderId, String message){

        //when user seller changes order status  send notification to buyer
        //prepare data for notification
        String NOTIFICATION_TOPIC ="/topics/"+ Constants.FCM_TOPIC;//must be same as subscribed by user
        String NOTIFICATION_TITLE ="New Order"+ orderId;
        String NOTIFICATION_MESSAGE =""+message;
        String NOTIFICATION_TYPE ="OrderStatusChanged";

        //prepare json (what to send and where to send)
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo =new JSONObject();

        try {
            //what to send
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid", orderBy);
            notificationBodyJo.put("sellerUid", firebaseAuth.getUid());//since we are logged in  as seller to change order status so current user uid is seller uid
            notificationBodyJo.put("orderId", orderId);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);

            //where to send
            notificationJo.put("to", NOTIFICATION_TOPIC);//to all who subscribed to this topic
            notificationJo.put("data", notificationBodyJo);

        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendFcmNotification(notificationJo);

    }

    private void sendFcmNotification(JSONObject notificationJo) {
        //send volley request
        JsonObjectRequest jsonObjectRequest =new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //notification sent
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //notification failed
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key="+Constants.FCM_KEY);
                return headers;
            }

        };
        //queue the volley request

        Volley.newRequestQueue(this).add(jsonObjectRequest);
        }


}