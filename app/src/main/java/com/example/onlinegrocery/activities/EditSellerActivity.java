package com.example.onlinegrocery.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.onlinegrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class EditSellerActivity extends AppCompatActivity implements LocationListener {

    private ImageButton imgBtn, gpsBtn;
   private ImageView imageIv;
    private EditText fullNameEt, phoneEt, countryEt, stateEt,cityEt, addressEt,
            shopNameEt, deliveryFeeEt;
    private Button updateBtn;
    SwitchCompat shopOpenSwitch;

    //permission constant
    private  static  final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE=300;
    //img pick constant
    private static  final int IMAGE_PICK_FROM_GALLERY_CODE =400;
    private static  final int IMAGE_PICK_FROM_CAMERA_CODE = 500;
    //permission array
    private  String[] locationPermission;
    private String[] cameraPermission;
    private String[] storagePermission;

    private Uri img_uri;
    private LocationManager locationManager;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private double latitude =0.0;
    private double longitude = 0.0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_seller);

        imgBtn = findViewById(R.id.edit_seller_bckbtn);
        gpsBtn = findViewById(R.id.seller_edit_gpsbtn);
        imageIv = findViewById(R.id.profile_img);
        fullNameEt = findViewById(R.id.full_name);
        phoneEt = findViewById(R.id.phone);
        countryEt = findViewById(R.id.country);
        stateEt = findViewById(R.id.state);
        cityEt = findViewById(R.id.city);
        addressEt = findViewById(R.id.address);
        updateBtn = findViewById(R.id.editBtn);
        shopOpenSwitch = findViewById(R.id.swi_open);
        shopNameEt = findViewById(R.id.shopName);
        deliveryFeeEt = findViewById(R.id.deliveryFee);

        firebaseAuth = FirebaseAuth.getInstance();
        //init permission array
        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};



        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth =FirebaseAuth.getInstance();
        checkUser();


        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });

        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //detect location
                //detect location
                if(checkLocationPermission()){

                    //already allowed
                    detectLocation();

                }
                else{
                    //not allowed request
                    requestLocationPermission();

                }
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();

            }
        });

        imageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });
    }

    String fullName, shopName, phone, deliveryFee, country, state, city, address;
    private boolean shopOpen;

    private void inputData() {
        fullName = fullNameEt.getText().toString().trim();
        shopName = shopNameEt.getText().toString().trim();
        phone = phoneEt.getText().toString().trim();
        deliveryFee = deliveryFeeEt.getText().toString().trim();
        country = countryEt.getText().toString().trim();
        state = stateEt.getText().toString().trim();
        city= cityEt.getText().toString().trim();
        address =addressEt.getText().toString().trim();
        shopOpen =shopOpenSwitch.isChecked(); //true or false

        updateProfile();


    }

    private void updateProfile() {
        progressDialog.setMessage("Updating profile");
        progressDialog.show();
        if(img_uri==null){
            //update without image
            //setup data to update
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("name",""+fullName);
            hashMap.put("shopName", ""+shopName);
            hashMap.put("deliveryFee", ""+deliveryFee);
            hashMap.put("phone", ""+phone);
            hashMap.put("country",""+country);
            hashMap.put("state",""+state);
            hashMap.put("city",""+city);
            hashMap.put("address",""+address);
            hashMap.put("latitude",""+latitude);
            hashMap.put("longitude",""+longitude);
            hashMap.put("shopOpen",""+shopOpen);

            //update to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    //update
                    progressDialog.dismiss();
                    Toast.makeText(EditSellerActivity.this, "Profile update", Toast.LENGTH_SHORT).show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed to update
                    progressDialog.dismiss();
                    Toast.makeText(EditSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }

        else{
            //update with image
            /*.....upload image.....*/
            String filePathAndName ="profile_images/"+""+firebaseAuth.getUid();
            //get storage reference

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(img_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    //img uploaded ,get url of upload img
                    Task<Uri> uriTask =taskSnapshot.getStorage().getDownloadUrl();
                    while(!uriTask.isSuccessful());

                    Uri downloadImageUri = uriTask.getResult();
                    if(uriTask.isSuccessful()){
                        //img url received , now update db
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("name",""+fullName);
                        hashMap.put("shopName", ""+shopName);
                        hashMap.put("deliveryFee", ""+deliveryFee);
                        hashMap.put("phone", ""+phone);
                        hashMap.put("country",""+country);
                        hashMap.put("state",""+state);
                        hashMap.put("city",""+city);
                        hashMap.put("address",""+address);
                        hashMap.put("latitude",""+latitude);
                        hashMap.put("longitude",""+longitude);
                        hashMap.put("shopOpen",""+shopOpen);
                        hashMap.put("profileImage",""+downloadImageUri);

                        //update to db
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(firebaseAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                //update
                                progressDialog.dismiss();
                                Toast.makeText(EditSellerActivity.this, "Profile update", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //failed to update
                                progressDialog.dismiss();
                                Toast.makeText(EditSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    progressDialog.dismiss();
                    Toast.makeText(EditSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });

        }



    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user==null){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }
        else{
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        //load user info and set to views
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    String accountType =""+ds.child("accountType");
                    String address = ""+ds.child("address").getValue();
                    String city =""+ds.child("city").getValue();
                    String country= ""+ds.child("country").getValue();
                    String state = ""+ds.child("state").getValue();
                    String phone =""+ds.child("phone").getValue();
                    String delivery =""+ds.child("delivery").getValue();
                    String email = ""+ds.child("email").getValue();
                    String fullName =""+ds.child("fullName").getValue();
                    String shopName =""+ds.child("shopName").getValue();
                    String shopOpen =""+ds.child("shopOpen").getValue();
                    String uid  =""+ds.child("uid").getValue();
                    String profileImage =""+ds.child("profileImage").getValue();
                    String timestamp =""+ds.child("uid").getValue();
                    String online = ""+ds.child("online").getValue();
                    longitude = Double.parseDouble(""+ds.child("longitude").getValue());
                    latitude = Double.parseDouble(""+ds.child("latitude").getValue());


                    //set data
                    fullNameEt.setText(fullName);
                    phoneEt.setText(phone);
                    shopNameEt.setText(shopName);
                    deliveryFeeEt.setText(delivery);
                    countryEt.setText(country);
                    stateEt.setText(state);
                    cityEt.setText(city);
                    addressEt.setText(address);

                    if(shopOpen.equals("true")){
                        shopOpenSwitch.setChecked(true);
                    }
                    else{
                        shopOpenSwitch.setChecked(false);
                    }
                    try{
                        Picasso.get().load(profileImage).placeholder(R.drawable.ic_store).into(imageIv);

                    }
                    catch (Exception e){
                        imageIv.setImageResource(R.drawable.ic_person);

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void showImagePickDialog() {
        //option to display dialog
        String[] option ={"Camera" , "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image").setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle item click
                if ((which==0)){
                    //camera clicked
                    if(checkCameraPermission()){
                        //allowed open camera
                        pickFromCamera();

                    }
                    else{
                        //not allowed request
                        requestCameraPermission();
                    }
                }
                else{
                    //gallery clicked
                    if(checkStoragePermission()){
                        pickFromGallery();

                    }
                    else{
                        requestStoragePermission();
                    }

                }

            }
        }).show();

    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }
    private boolean checkStoragePermission() {

        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return result;
    }
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission,CAMERA_REQUEST_CODE);
    }
    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_FROM_GALLERY_CODE);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,locationPermission,LOCATION_REQUEST_CODE);
    }
    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void pickFromCamera() {


        //intent to pick image from camera
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Image Description");
        img_uri= getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        Intent intent = new Intent(MediaStore.EXTRA_OUTPUT,img_uri);
        startActivityForResult(intent, IMAGE_PICK_FROM_CAMERA_CODE);

    }





    private void detectLocation() {
        Toast.makeText(this, "Please wait..", Toast.LENGTH_SHORT).show();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
    }


    private void findAddress() {
        //find address,country , city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try{
            addresses = geocoder.getFromLocation(latitude,longitude,1);
            String address = addresses.get(0).getAddressLine(0);
            String country = addresses.get(0).getCountryName();
            String state = addresses.get(0).getAdminArea();
            String city = addresses.get(0).getLocality();

            //set address
            countryEt.setText(country);
            stateEt.setText(state);
            cityEt.setText(city);
            addressEt.setText(address);




        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }
    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude= location.getLongitude();
        findAddress();

    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Location disable", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean locationAccepted = grantResults[0]== PackageManager.PERMISSION_GRANTED;
                    if(locationAccepted){
                        //permission allowed
                        detectLocation();
                    }
                    else{
                        //permission denied
                        Toast.makeText(this, "Location permission allowed", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0]== PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        //permission allowed
                        pickFromCamera();
                    }
                    else{
                        //permission denied
                        Toast.makeText(this, "Camera Permission is necessary", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        pickFromGallery();

                    }
                    else{
                        Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        //handle the image pick
        if(resultCode==RESULT_OK){
            if(requestCode==IMAGE_PICK_FROM_CAMERA_CODE){
                img_uri = data.getData();
                //set to img view
                imageIv.setImageURI(img_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}