package com.example.onlinegrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onlinegrocery.adapters.AdapterOrderShop;
import com.example.onlinegrocery.adapters.AdapterProductSeller;
import com.example.onlinegrocery.Constants;
import com.example.onlinegrocery.models.ModelOrderShop;
import com.example.onlinegrocery.models.ModelProduct;
import com.example.onlinegrocery.R;
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

public class MainSellerActivity extends AppCompatActivity {

    private TextView nameTv, shopNameTv, emailTv, tabProductTv, tabOrderTv, filterProductTv,
    filteredOrdersTv;
    private ImageButton logoutBtn,addProductBtn, filterOrderBtn, reviewBtn, moreBtn;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private ImageButton imgBtn, settingBtn;
    private ImageView profileIv;
    private RelativeLayout productsRl, ordersRl;
    private EditText searchEt;
    private ImageButton filterProductBtn;
    private RecyclerView productRv, ordersRv;

    private ArrayList<ModelProduct>  productList;
   private AdapterProductSeller adapterProductSeller;

   private ArrayList<ModelOrderShop> orderShopList;
   private AdapterOrderShop adapterOrderShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);
        logoutBtn = findViewById(R.id.seller_logoutbtn);
        imgBtn = findViewById(R.id.seller_editbtn);
        shopNameTv = findViewById(R.id.shopNameTv);
        emailTv = findViewById(R.id.emailTv);
        addProductBtn = findViewById(R.id.product_add_btn);
        profileIv = findViewById(R.id.profile);
        tabProductTv= findViewById(R.id.tabProductsTv);
        tabOrderTv = findViewById(R.id.tabOrdersTv);
        productsRl = findViewById(R.id.productsRl);
        ordersRl = findViewById(R.id.ordersRl);
        filterProductTv = findViewById(R.id.filterProductTv);
        searchEt = findViewById(R.id.searchEt);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        productRv = findViewById(R.id.productRv);

        filteredOrdersTv = findViewById(R.id.filteredOrdersTv);
        filterOrderBtn = findViewById(R.id.filterOrderBtn);
        ordersRv = findViewById(R.id.ordersRv);

        settingBtn = findViewById(R.id.settingBtn);

        reviewBtn = findViewById(R.id.reviewBtn);
        moreBtn = findViewById(R.id.moreBtn);

        //popupMenu
        final PopupMenu popupMenu = new PopupMenu(MainSellerActivity.this, moreBtn);
        //add menu items to our menu
        popupMenu.getMenu().add("Settings");
        popupMenu.getMenu().add("Review");
        popupMenu.getMenu().add("Promotion Codes");
        //handle menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getTitle().equals("Settings")){
                    //start setting activity
                    startActivity(new Intent(MainSellerActivity.this, SettingsActivity.class));
                }
                else if(item.getTitle().equals("Review")){

                    Intent intent = new Intent(MainSellerActivity.this,ShopReviewsActivity.class);
                    intent.putExtra("shopUid",""+firebaseAuth.getUid());
                    startActivity(intent);

                } else if(item.getTitle().equals("Promotion Codes")){
                    //start promotion list screen
                }
                return true;
            }
        });
        //show more option:settings, review,addPromotionsCode
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//show popup menu
                popupMenu.show();
            }
        });

        //start setting screen

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Choose category:");
                builder.setItems(Constants.productCategory1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                   //get selected item
                   String selected = Constants.productCategory1[which];;
                   filterProductTv.setText(selected);
                   if(selected.equals("All")){
                       //loadAll
                       loadAllProducts();
                   }
                   else {
                       loadFilteredProducts(selected);
                   }
                    }
                }).show();
            }
        });
        tabProductTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load products
                //show product
                showProductUI();

            }
        });

        tabOrderTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load orders

                showOrderUI();
            }
        });
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainSellerActivity.this, EditSellerActivity.class));
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //make offline
                //sign out
                //go to login
               // firebaseAuth.signOut();
                makeMeOffline();
            }
        });

        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit and product activity
                startActivity(new Intent(MainSellerActivity.this, AddProductActivity.class));
            }
        });

        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductSeller.getFilter().filter(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        filterOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //option to display in dialog
                final String[] options ={"All", "In Progress", "Completed", "Cancelled"};
                //dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Filter Orders:")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //handle item click
                                if(which==0){
                                    //all clicked

                                    filteredOrdersTv.setText("Showing All Orders");
                                    adapterOrderShop.getFilter().filter("");//show all orders
                                }else {
                                    String optionClicked = options[which];
                                    filteredOrdersTv.setText("Showing"+optionClicked+"Orders");
                                    adapterOrderShop.getFilter().filter(optionClicked);
                                }
                            }
                        }).show();
            }
        });



        nameTv= findViewById(R.id.text_seller);
        //logoutBtn = findViewById(R.id.seller_logoutbtn);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        checkUser();
        showProductUI();
        loadAllProducts();

        loadAllOrders();

    }

    private void loadAllOrders() {
//init arrayList
        orderShopList = new ArrayList<>();
        //load order of shops
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   //clear list before adding new data
                   orderShopList.clear();

                   for(DataSnapshot ds: dataSnapshot.getChildren()){
                       ModelOrderShop modelOrderShop = ds.getValue(ModelOrderShop.class);

                       //add to list

                       orderShopList.add(modelOrderShop);

                   }
                   //setup adapter
                        adapterOrderShop = new AdapterOrderShop(MainSellerActivity.this, orderShopList);

                   //set adapter to recyclerView
                        ordersRv.setAdapter(adapterOrderShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadFilteredProducts(final String selected) {
        productList = new ArrayList<>();
        //get all data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //before getting reset list
                productList.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String productCategory = ""+ds.child("productCategory").getValue();
                    //select category , matches product category then and in list
                    if(selected.equals(productCategory)){
                        ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                        productList.add(modelProduct);
                    }

                }
                //setup adapter
                adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);
                //set adapter
                productRv.setAdapter(adapterProductSeller);
            }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

    }

    private void loadAllProducts() {
        productList = new ArrayList<>();
        //get all data
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //before getting reset list
                productList.clear();

                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                    productList.add(modelProduct);
                }
                //setup adapter
       adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);
                //set adapter
                productRv.setAdapter(adapterProductSeller);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showOrderUI() {
        ordersRl.setVisibility(View.VISIBLE);
        productsRl.setVisibility(View.GONE);

        tabOrderTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrderTv.setBackgroundResource(R.drawable.shape_rect04);

        tabProductTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabProductTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showProductUI() {
        productsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabProductTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabProductTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrderTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrderTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }

    private void makeMeOffline() {
        //after log in make user online
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
                Toast.makeText(MainSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user==null){
            startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));
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
                    //get data from db
                    String fullName = ""+ds.child("fullName").getValue();
                    String accountType =""+ds.child("accountType").getValue();
                    String shopName =""+ds.child("shopName").getValue();
                    String email =""+ds.child("email").getValue();
                    String profileImage =""+ds.child("profileImage");

                    nameTv.setText(fullName);
                    shopNameTv.setText(shopName);
                    emailTv.setText(email);

                    //for image set
                    try {
                        Picasso.get().load(profileImage).placeholder(R.drawable.ic_store).into(profileIv);
                    }


                    catch (Exception e){
                        profileIv.setImageResource(R.drawable.ic_store);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




}