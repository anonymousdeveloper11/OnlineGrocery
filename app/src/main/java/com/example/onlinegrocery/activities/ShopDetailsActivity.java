package com.example.onlinegrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.onlinegrocery.Constants;
import com.example.onlinegrocery.R;
import com.example.onlinegrocery.adapters.AdapterCartItem;
import com.example.onlinegrocery.adapters.AdapterProductUser;
import com.example.onlinegrocery.adapters.AdapterReview;
import com.example.onlinegrocery.models.ModelCartItem;
import com.example.onlinegrocery.models.ModelProduct;
import com.example.onlinegrocery.models.ModelReview;
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

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import p32929.androideasysql_library.EasyDB;

//import static java.lang.Double.parseDouble;

public class ShopDetailsActivity extends AppCompatActivity {


    //declare ui views
    private ImageView shopIv;
    private TextView shopNameTv, phoneTv,emailTv, openCloseTv, deliveryFeeTv,
    addressTv, filterProductsTv, cartCountTv;
    private ImageButton callBtn, mapBtn, backBtn, cartBtn, filterBtn, reviewBtn;
    private EditText searchEt;
    private RecyclerView productsRv;
    private String shopUid;
    private FirebaseAuth firebaseAuth;
    private String myLatitude, myLongitude, myPhone;
    private String shopLatitude, shopLongitude;
    private String shopName, shopEmail, shopAddress, shopPhone;
    private ArrayList<ModelProduct> productsList;

    private AdapterProductUser adapterProductUser;
    //cart
    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;


    public String deliveryFee;
  // public double allTotalPrice = 0.00;

   private ProgressDialog progressDialog;
   private EasyDB easyDB;

   private RatingBar ratingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);
        //init ui views

        shopIv = findViewById(R.id.shopIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        phoneTv = findViewById(R.id.phoneTv);
        emailTv = findViewById(R.id.emailTv);
        openCloseTv = findViewById(R.id.openCloseTv);
        deliveryFeeTv = findViewById(R.id.deliveryFeeTv);
        addressTv = findViewById(R.id.addressTv);
        filterProductsTv = findViewById(R.id.filterProductsTv);
        callBtn = findViewById(R.id.callBtn);
        mapBtn = findViewById(R.id.mapBtn);
        backBtn = findViewById(R.id.backBtn);
        cartBtn = findViewById(R.id.cartBtn);
        filterBtn = findViewById(R.id.filterBtn);
        searchEt = findViewById(R.id.searchEt);
        productsRv = findViewById(R.id.productsRv);
        cartCountTv = findViewById(R.id.cartCountTv);
        reviewBtn = findViewById(R.id.reviewBtn);

        ratingBar = findViewById(R.id.ratingBar);
        //get uid of shop from intent
        shopUid = getIntent().getStringExtra("shopUid");
        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait..");
        progressDialog.setCanceledOnTouchOutside(false);
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();

        //loadReviews();//avg rating, set on rating bar

         easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn("Item_Id", new String[]{"text", "unique"})
                .addColumn("Item_PID", new String[]{"text", "not null"})
                .addColumn("Item_Name", new String[]{"text", "not null"})
                .addColumn("Item_Price_Each", new String[]{"text", "not null"})
                .addColumn("Item_Price", new String[]{"text","not null"})
                .addColumn("Item_Quantity", new String[]{"text", "not null"})
                .doneTableColumn();
        //each shop have its own products and orders so if user add items to cart and go back and open cart in differnt shop then cart should be different
        //so delete cart data whenever user open this activity
        deleteCartData();
        cartCount();



        //handle reviewBtn click, open review activity

        reviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //pass shop uid to show its review
                Intent intent = new Intent(ShopDetailsActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", shopUid);
                startActivity(intent);
            }
        });
        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductUser.getFilter().filter(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dailPhone();
            }
        });
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openMap();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show cart dialog
                showCartDialog();

            }
        });

        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Choose category:");
                builder.setItems(Constants.productCategory1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get selected item
                        String selected = Constants.productCategory1[which];;
                        filterProductsTv.setText(selected);
                        if(selected.equals("All")){
                            //loadAll
                            loadShopProducts();
                        }
                        else {
                           adapterProductUser.getFilter().filter(selected);
                        }
                    }
                }).show();
            }
        });

    }

    private void prepareNotificationMessage(String orderId){

        //when user places order send notification to seller
        //prepare data for notification
        String NOTIFICATION_TOPIC ="/topics/"+Constants.FCM_TOPIC;//must be same as subscribed by user
        String NOTIFICATION_TITLE ="New Order"+ orderId;
        String NOTIFICATION_MESSAGE ="Congratulation..! You have new Order.";
        String NOTIFICATION_TYPE ="NewOrder";

        //prepare json (what to send and where to send)
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo =new JSONObject();

        try {
            //what to send
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid", firebaseAuth.getUid());//since we are logged in  as buyer to placed order so current user uid is buyer uid
            notificationBodyJo.put("sellerUid", shopUid);
            notificationBodyJo.put("orderId", orderId);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);

            //where to send
            notificationJo.put("to", NOTIFICATION_TOPIC);//to all who subscribed to this topic
            notificationJo.put("data", notificationBodyJo);

        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendFcmNotification(notificationJo, orderId);

    }

    private void sendFcmNotification(JSONObject notificationJo, final String orderId) {
        //send volley request
        JsonObjectRequest jsonObjectRequest =new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                //after sending fcm start order details activity
                //after placing order open order details page
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                intent.putExtra("OrderTo", shopUid);
                intent.putExtra("orderId", orderId);
                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //it failed sending fcm, still start order detail activity

                //after placing order open order details page
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                intent.putExtra("OrderTo", shopUid);
                intent.putExtra("orderId", orderId);
                startActivity(intent);

            }
        }){
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

    private float ratingSum=0;
    private void loadReviews() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                //clear list before adding data into it


                ratingSum=0;
                for(DataSnapshot ds: dataSnapshot.getChildren());
                float rating = Float.parseFloat(""+dataSnapshot.child("rating").getValue());
                ratingSum = ratingSum+rating;//for avg rating add(additionalof)all ratings , laate will divide it by number of reviews
                ModelReview modelReview = dataSnapshot.getValue(ModelReview.class);

                //setup adapter




                Long numberOfReviews = dataSnapshot.getChildrenCount();
                float avgRating = ratingSum/numberOfReviews;
                ratingBar.setRating(avgRating);

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deleteCartData() {

        easyDB.deleteAllDataFromTable();//delete all records from cart
    }


    public void cartCount(){
        //keep it public so we can access in adapter

        //get cart count
        int count = easyDB.getAllData().getCount();
        if(count<=0){
            //no item in cart hide cart textView
            cartCountTv.setVisibility(View.GONE);
        }else{
            //have item cart ,show cart count textView and set count
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText(""+count);//concatenate with string because we can set integer in textView
        }
    }

    //need to access these views in adapter so making public
    public double allTotalPrice = 0.00;
    public TextView sTotalTv, dFeeTv, allTotalPriceTv,promoDescriptionTv, discountTv;
    public EditText promoCodeEt;
    public Button applyBtn;
    @SuppressLint("SetTextI18n")
    private void showCartDialog() {
        //init list
        cartItemList = new ArrayList<>();
        //inflate cart layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart,null);
        //init views
        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        RecyclerView cartItemRv = view.findViewById(R.id.cartItemRv);
         sTotalTv = view.findViewById(R.id.sTotalTv);
         dFeeTv = view.findViewById(R.id.dFeeTv);
         allTotalPriceTv = view.findViewById(R.id.totalTv);
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);
        promoCodeEt = view.findViewById(R.id.promoCodeEt);
        promoDescriptionTv = view.findViewById(R.id.promoDescriptionTv);
        discountTv = view.findViewById(R.id.discountTv);
        applyBtn = view.findViewById(R.id.applyBtn);
        FloatingActionButton validateBtn = view.findViewById(R.id.validateBtn);

        //whenever cart dialog show check if promo code is applied or not
        if(isPromoCodeApplied){
            //applied
            promoDescriptionTv.setVisibility(View.VISIBLE);
            applyBtn.setVisibility(View.VISIBLE);
            applyBtn.setText("Applied");
            promoCodeEt.setText(promoCode);
            promoDescriptionTv.setText(promoDescription);
        } else {
            //not applied
            promoDescriptionTv.setVisibility(View.GONE);
            applyBtn.setVisibility(View.GONE);
            applyBtn.setText("Apply");

        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set view to dialog
        builder.setView(view);
        shopNameTv.setText(shopName);

        EasyDB easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn("Item_Id", new String[]{"text", "unique"})
                .addColumn("Item_PID", new String[]{"text", "not null"})
                .addColumn("Item_Name", new String[]{"text", "not null"})
                .addColumn("Item_Price_Each", new String[]{"text", "not null"})
                .addColumn("Item_Price", new String[]{"text","not null"})
                .addColumn("Item_Quantity", new String[]{"text", "not null"})
                .doneTableColumn();

        //get all records from Db
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()){
            String id =res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);
            ModelCartItem modelCartItem = new ModelCartItem(
                    ""+id,
                    ""+pId,
                    ""+name,
                    ""+price,
                    ""+cost,
                    ""+quantity);
            cartItemList.add(modelCartItem);
        }
        //setup adapter
        adapterCartItem = new AdapterCartItem(this, cartItemList);

        //set to recycler view
        cartItemRv.setAdapter(adapterCartItem);
        if(isPromoCodeApplied){
            priceWithDiscount();
        }else {
            priceWithoutDiscount();
        }


        //show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        //reset total price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice =0.00;
            }
        });

        //place order
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //first Validate delivery address and phone
                if(myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null")){
                    //user didn't enter address in profile
                    Toast.makeText(ShopDetailsActivity.this, "Please Enter your Address in your profile before placing order..", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(myPhone.equals("") || myPhone.equals("null")){
                    //user didn't enter address in profile
                    Toast.makeText(ShopDetailsActivity.this, "Please Enter your Phone Number in your profile before placing order..", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if(cartItemList.size()==0){
                    //cart list is empty
                    Toast.makeText(ShopDetailsActivity.this, "No Item in Cart", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                submitOrder();
            }
        });

        //start validating promo code when validate button is pressed
        validateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String promotionCode = promoCodeEt.getText().toString().trim();
                if(TextUtils.isEmpty(promotionCode)){
                    Toast.makeText(ShopDetailsActivity.this, "Please enter promo Code", Toast.LENGTH_SHORT).show();
                } else {
                    checkCodeAvailability(promotionCode);
                }
            }
        });

        //apply code if valid , no need to check if valid or not, because this button will be visible only if code is valid
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPromoCodeApplied = true;
                applyBtn.setText("Applied");
                priceWithDiscount();
            }
        });

    }
    public boolean isPromoCodeApplied = false;
    public String promoId, promoTimestamp,promoCode,promoDescription, promoExpDate,promoMinimumOrderPrice, promoPrice;
    private  void checkCodeAvailability(String promotionCode){//promotionCode is promo COde enter by user
        //progress Bar
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait..");
        progressDialog.setMessage("Checking promo Code..");
        progressDialog.setCanceledOnTouchOutside(false);

        //promo is not applied
        isPromoCodeApplied = false;
        applyBtn.setText("Apply");
        priceWithoutDiscount();

        //check promoCode Availability
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Promotions").orderByChild("promoCode")
                .equalTo(promotionCode)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                   //check if promo Code exists
                   if(snapshot.exists()){
                       //promo Code exists
                       progressDialog.dismiss();
                       for (DataSnapshot ds:snapshot.getChildren()){
                           promoId =""+ds.child("id").getValue();
                           promoTimestamp =""+ds.child("timestamp").getValue();
                           promoCode = ""+ds.child("promoCode").getValue();
                           promoDescription=""+ds.child("description").getValue();
                           promoExpDate =""+ds.child("expireDate").getValue();
                           promoMinimumOrderPrice =""+ds.child("minimumOrderPrice").getValue();
                           promoPrice =""+ds.child("promoPrice").getValue();
                           //noe check if code is expire or not
                           checkCodeExpireDate();
                       }
                   }else {
                       //entered from code doesn't exists
                       progressDialog.dismiss();
                       Toast.makeText(ShopDetailsActivity.this, "Invalid promo Code", Toast.LENGTH_SHORT).show();
                   applyBtn.setVisibility(View.GONE);
                   promoDescriptionTv.setVisibility(View.GONE);
                   promoDescriptionTv.setText("");
                   }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkCodeExpireDate() {
        //get Current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;//its starts from 0 instead of 1 that's why did +1
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        //concatenate date
        String todayDate = day + "/" + month + "/" + year;
        //check for expire date
        try {
            SimpleDateFormat sdFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date currentDate = sdFormat.parse(todayDate);
            Date expireDate = sdFormat.parse(promoExpDate);

            //compose Date
            if(expireDate.compareTo(currentDate)>0){
                //date 1 occur after date 2(i.e not expire date)
                checkMinimumOrderPrice();
            }else if(expireDate.compareTo(currentDate)<0){
                //date 1 occurs before date 2 i.e not epired
                Toast.makeText(this, "The promotion Code is expired on"+promoExpDate, Toast.LENGTH_SHORT).show();
            applyBtn.setVisibility(View.GONE);
            promoDescriptionTv.setVisibility(View.GONE);
            promoDescriptionTv.setText("");
            }else if(expireDate.compareTo(currentDate)==0){
                //both date are equals(i.e not expire)
                checkMinimumOrderPrice();
            }


        } catch (Exception e) {
            //if anything wrong causing exception while comparing current date and expiry date
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            applyBtn.setVisibility(View.GONE);
            promoDescriptionTv.setVisibility(View.GONE);
            promoDescriptionTv.setText("");

        }
    }

    private void checkMinimumOrderPrice() {
        //each promo Code have minimum Order price requirement, if order price is less than required then don't allow to apply code
        if(Double.parseDouble(String.format("%.2f", allTotalPrice))< Double.parseDouble(promoMinimumOrderPrice)){
            Toast.makeText(this, "The code is valid for order with minimum amount: Rs" +promoMinimumOrderPrice, Toast.LENGTH_SHORT).show();
        applyBtn.setVisibility(View.GONE);
        promoDescriptionTv.setVisibility(View.GONE);
        promoDescriptionTv.setText("");
        }
        else {
            //current price is equal to or greater than minimumOrderPrice promoCode is allow to apply code
            applyBtn.setVisibility(View.VISIBLE);
            promoDescriptionTv.setVisibility(View.VISIBLE);
            promoDescriptionTv.setText(promoDescription);
        }
    }
    private void priceWithDiscount(){
        discountTv.setText("Rs "+ promoPrice);
        dFeeTv.setText("Rs "+dFeeTv);
        sTotalTv.setText("Rs " +String.format("%.2f", allTotalPrice));
        allTotalPriceTv.setText("$"+(allTotalPrice + Double.parseDouble(deliveryFee. replace("Rs", "")) - Double.parseDouble(promoPrice)));
    }

    private void priceWithoutDiscount() {
        discountTv.setText("Rs 0");
        dFeeTv.setText("Rs "+deliveryFee);
        sTotalTv.setText("Rs " +String.format("%.2f", allTotalPrice));
        if(allTotalPrice != 0) {
            allTotalPriceTv.setText("Rs" + (allTotalPrice + Double.parseDouble(deliveryFee.replace("Rs", ""))));
        }
    }

    private void submitOrder() {
        progressDialog.setMessage("Placing order..");
        progressDialog.show();
        //for order id and order Time

        final String timestamp =""+System.currentTimeMillis();
        String cost = allTotalPriceTv.getText().toString().trim().replace("Rs ","");//remove $ if contains

        //setup order data
        final HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", ""+timestamp);
        hashMap.put("orderTime", ""+timestamp);
        hashMap.put("orderStatus", "In Progress");
        hashMap.put("orderCost", ""+cost);
        hashMap.put("orderBy", ""+firebaseAuth.getUid());
        hashMap.put("orderTo", ""+shopUid);
        hashMap.put("latitude", ""+myLatitude);
        hashMap.put("longitude", ""+myLongitude);
        if(isPromoCodeApplied){
            //promo applied
            hashMap.put("discount", ""+promoPrice);//include promo price
        }else{
            //promo not applied , include price o
            hashMap.put("discount", "0");
        }

        //add to db
        final DatabaseReference ref =  FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //order info added now order items
                for(int i=0; i<cartItemList.size(); i++){
                    String pId = cartItemList.get(i).getpId();
                    String id = cartItemList.get(i).getId();
                    String cost = cartItemList.get(i).getCost();
                    String price = cartItemList.get(i).getPrice();
                    String name = cartItemList.get(i).getName();
                    String quantity = cartItemList.get(i).getQuantity();

                    HashMap<String, String > hashMap1 = new HashMap<>();
                    hashMap1.put("pId", pId);
                    hashMap1.put("name", name);
                    hashMap1.put("cost", cost);
                    hashMap1.put("price", price);
                    hashMap1.put("quantity", quantity);

                    ref.child(timestamp).child("Items").child(pId).setValue(hashMap1);
                }

                progressDialog.dismiss();
                Toast.makeText(ShopDetailsActivity.this, "Order place successfully..", Toast.LENGTH_SHORT).show();

                prepareNotificationMessage(timestamp);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                //failed placing order
                progressDialog.dismiss();
                Toast.makeText(ShopDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openMap() {
        String address ="https://maps.google.com/maps?safddr=" + myLatitude + "," + myLongitude + "&daddr=" + shopLatitude + "," +shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void dailPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this, ""+shopPhone, Toast.LENGTH_SHORT).show();
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
                    myPhone = "" +ds.child("phone").getValue();
                    String profileImage ="" +ds.child("profileImage").getValue();
                    String city ="" +ds.child("city").getValue();
                    String accountType =""+ds.child("accountType").getValue();
                    myLatitude =""+ds.child("latitude").getValue();
                    myLongitude=""+ds.child("longitude").getValue();



                }
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
                //get shop data
                String name =""+dataSnapshot.child("name").getValue();
                shopName = ""+dataSnapshot.child("shopName").getValue();
                shopEmail =""+dataSnapshot.child("email").getValue();
                shopPhone =""+dataSnapshot.child("phone").getValue();
                shopAddress =""+dataSnapshot.child("address").getValue();
                shopLatitude =""+dataSnapshot.child("latitude").getValue();
                shopLongitude =""+dataSnapshot.child("longitude").getValue();
                deliveryFee =""+dataSnapshot.child("deliveryFee").getValue();
                String profileImage =""+dataSnapshot.child("profileImage").getValue();
                String shopOpen =""+dataSnapshot.child("shopOpen").getValue();

                //set data
                shopNameTv.setText(shopName);
                emailTv.setText(shopEmail);
                if(deliveryFee.contains("null")){
                    deliveryFeeTv.setText("Rs 0");
                }else {
                    deliveryFeeTv.setText("Rs "+deliveryFee);

                }
                addressTv.setText(shopAddress);
                phoneTv.setText(shopPhone);
                if(shopOpen.equals("true")){
                    openCloseTv.setText("Open");
                }else{
                    openCloseTv.setText("Closed");
                }
                try{
                    Picasso.get().load(profileImage).into(shopIv);

                }catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadShopProducts() {

        //init list
        productsList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //clear list before adding
                productsList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                    productsList.add(modelProduct);

                }
                //setup adapter
                adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this,productsList);
                //set adapter
                productsRv.setAdapter(adapterProductUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}