package com.example.onlinegrocery.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
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
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onlinegrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterUserActivity extends AppCompatActivity implements LocationListener {

    private ImageButton backbtn , gpsbtn;
   private EditText nameEt, phoneEt,countryEt, stateEt, cityEt,addressEt, emailEt,passwordEt, conformEt;
   private TextView registerSeller;
   private Button registerbtn;
    private ImageView profileImg;
    private double latitude, longitude;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private LocationManager locationManager;
    //permission constants
    private static final int LOCATION_REQUEST_CODE=100;
    private  static  final int CAMERA_REQUEST_CODE=200;
    private static final int STORAGE_REQUEST_CODE=300;
    private static final int IMAGE_PICK_GALLERY_CODE=400;
    private static final int IMAGE_PICK_CAMERA_CODE=500;

    //permission array
    private String[] locationPermission;
    private String[] cameraPermission;
    private String[] storagePermission;

    //image picked uri
    private Uri image_uri;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        backbtn = findViewById(R.id.register_bckbtn);
        gpsbtn = findViewById(R.id.register_gpsbtn);
        nameEt = findViewById(R.id.register_name);
        phoneEt = findViewById(R.id.register_phone);
        countryEt = findViewById(R.id.register_country);
        stateEt = findViewById(R.id.register_state);
        cityEt = findViewById(R.id.register_city);
        addressEt = findViewById(R.id.register_address);
        emailEt = findViewById(R.id.register_email);
        passwordEt = findViewById(R.id.register_password);
        conformEt = findViewById(R.id.register_conpassword);
        registerSeller = findViewById(R.id.text_register);
        registerbtn = findViewById(R.id.btn_register);
        profileImg = findViewById(R.id.profile_circular);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //init permission array
        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        gpsbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //detect current location
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

        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pick image
                showImagePickDialog();
            }
        });

        registerSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterUserActivity.this, RegisterSellerActivity.class));
            }
        });


        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //register user
                inputData();
            }
        });


    }
    String fullName, phoneNumber, country, state, city, address, email, password, confirmPassword;
    private void inputData() {
        fullName = nameEt.getText().toString().trim();

        phoneNumber = phoneEt.getText().toString().trim();
        country = countryEt.getText().toString().trim();
        state = stateEt.getText().toString().trim();
        city = cityEt.getText().toString().trim();
        address = addressEt.getText().toString().trim();
        email = emailEt.getText().toString().trim();
        password = passwordEt.getText().toString().trim();
        confirmPassword = conformEt.getText().toString().trim();

        //validate data

        if(TextUtils.isEmpty(fullName)){
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(phoneNumber)){
            Toast.makeText(this, "Enter phone Number", Toast.LENGTH_SHORT).show();
            return;
        }


        if(longitude==0.0 || latitude==0.0){
            Toast.makeText(this, "Please tap GPS button to detect", Toast.LENGTH_SHORT).show();
            return;

        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid Email address", Toast.LENGTH_SHORT).show();
            return;

        }

        if(password.length()<6){
            Toast.makeText(this, "Password must be atleast 6 characters ", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!password.equals(confirmPassword)){
            Toast.makeText(this, "Password doesn't match... ", Toast.LENGTH_SHORT).show();
            return;
        }

        createAccount();









    }

    private void createAccount() {
        progressDialog.setMessage("Creating Account");
        progressDialog.show();
        //create Account
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //account created
                        saverFirebaseData();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed create account
                progressDialog.dismiss();
                Toast.makeText(RegisterUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void saverFirebaseData() {
        progressDialog.setMessage("Saving Account info");
        final String timestamp = "" +System.currentTimeMillis();
        if(image_uri==null){
            //save info without image
            //setup data to save
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid", "" +firebaseAuth.getUid());
            hashMap.put("email","" +email);
            hashMap.put("fullName", "" +fullName);

            hashMap.put("phone", "" +phoneNumber);
            hashMap.put("country", "" +country);
            hashMap.put("state", "" +state);
            hashMap.put("city", "" +city);
            hashMap.put("address", "" +address);


            hashMap.put("latitude", "" +latitude);
            hashMap.put("longitude", "" +longitude);
            hashMap.put("timestamp", "" +timestamp);
            hashMap.put("latitude", "" +latitude);
            hashMap.put("accountType", "User");
            hashMap.put("online", "true");

            hashMap.put("profileImage", "");

            //save to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //db updated
                    progressDialog.dismiss();
                    startActivity(new Intent(RegisterUserActivity.this, MainUserActivity.class));
                    finish();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed to updating db
                    progressDialog.dismiss();
                    startActivity(new Intent(RegisterUserActivity.this, MainUserActivity.class));
                    finish();

                }
            });


        }
        else{
            //save info with image
            //name and path of image
            String filePathAndName = "profile_image/" + ""+firebaseAuth.getUid();
            //upload image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //get url of upload image
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uriTask.isSuccessful());
                    Uri downloadImageUri = uriTask.getResult();
                    if(uriTask.isSuccessful()){

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("uid", "" +firebaseAuth.getUid());
                        hashMap.put("email","" +email);
                        hashMap.put("fullName", "" +fullName);

                        hashMap.put("phone", "" +phoneNumber);
                        hashMap.put("country", "" +country);
                        hashMap.put("state", "" +state);
                        hashMap.put("city", "" +city);
                        hashMap.put("address", "" +address);


                        hashMap.put("latitude", "" +latitude);
                        hashMap.put("longitude", "" +longitude);
                        hashMap.put("timestamp", "" +timestamp);
                        hashMap.put("latitude", "" +latitude);
                        hashMap.put("accountType", "User");
                        hashMap.put("online", "true");

                        hashMap.put("profileImage", ""+downloadImageUri);//url of upload image

                        //save to db
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(firebaseAuth.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //db updated
                                progressDialog.dismiss();
                                startActivity(new Intent(RegisterUserActivity.this, MainUserActivity.class));
                                finish();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //failed to updating db
                                progressDialog.dismiss();
                                startActivity(new Intent(RegisterUserActivity.this, MainUserActivity.class));
                                finish();

                            }
                        });
                    }


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    progressDialog.dismiss();
                    Toast.makeText(RegisterUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });

        }
    }

    private void showImagePickDialog() {
        //option to display dialog

        String[] options= {"Camera","Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle click
                if(which==0){
                    //camera clicked
                    if(checkCameraPermission()){
                        //camera permission allowed
                        pickFromCamera();
                    }
                    else{
                        //not allowed
                        requestCameraPermission();
                    }
                }
                else{
                    //gallery click

                    if(checkStoragePermission()){
                        //storage permission allowed
                        pickFromGallery();
                    }
                    else{
                        //not allowed
                        requestStoragePermission();
                    }

                }
            }
        }).show();

    }

    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private void detectLocation() {

        Toast.makeText(this, "Please wait", Toast.LENGTH_SHORT).show();
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);

    }
    private void findAddress() {
        //find country address state city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude,longitude,1);
            String address = addresses.get(0).getAddressLine(0);//complete address
            String state = addresses.get(0).getAdminArea();
            String city = addresses.get(0).getLocality();
            String country = addresses.get(0).getCountryName();

            //set addresses
            countryEt.setText(country);
            stateEt.setText(state);
            cityEt.setText(city);
            addressEt.setText(address);


        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }


    }

    private boolean checkLocationPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==
                (PackageManager.PERMISSION_GRANTED);
        return result;

    }
    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this,locationPermission,LOCATION_REQUEST_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);
        return result;

    }
    private  void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);


    }
    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);


        return result && result1;


    }

    private  void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);


    }

    @Override
    public void onLocationChanged(Location location) {

        //location detect
        latitude = location.getLatitude();
        longitude = location.getLongitude();

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
        Toast.makeText(this, "Please turn on location", Toast.LENGTH_SHORT).show();

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
                        Toast.makeText(this, "Location permission is necessary...", Toast.LENGTH_SHORT).show();

                    }
                }


            }
            break;

            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0]== PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1]== PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && storageAccepted){
                        //permission allowed
                        pickFromCamera();

                    }
                    else{
                        //permission denied
                        Toast.makeText(this, "Camera permission is necessary...", Toast.LENGTH_LONG).show();

                    }
                }


            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){

                    boolean storageAccepted = grantResults[0]== PackageManager.PERMISSION_GRANTED;

                    if(storageAccepted){
                        //permission allowed
                        pickFromCamera();

                    }
                    else{
                        //permission denied
                        Toast.makeText(this, "Storage permission is necessary...", Toast.LENGTH_LONG).show();

                    }
                }


            }
            break;



        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode==RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                //get picked image
                image_uri = data.getData();
                //set to imageView
                profileImg.setImageURI(image_uri);
            }
            else if(requestCode== IMAGE_PICK_CAMERA_CODE){
                //set imageView
                profileImg.setImageURI(image_uri);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}