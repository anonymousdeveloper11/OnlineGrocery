package com.example.onlinegrocery.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onlinegrocery.Constants;
import com.example.onlinegrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddProductActivity extends AppCompatActivity {

   private ImageButton backBtn;
   private ImageView productIconIv;
   private EditText titleEt, descriptionEt, quantityEt,priceEt, discountPriceEt,
    discountNoteEt;
   private TextView categoryTv;
   private Button addProductBtn;
   private SwitchCompat discountSwitch;

   //permission constant
    private static final int CAMERA_REQUEST_CODE=200;
    private static final int  STORAGE_REQUEST_CODE=300;

    //image pick constant
    private static  final int IMAGE_PICK_GALLERY_CODE=400;
    private static final int IMAGE_PICK_CAMERA_CODE=500;

    //permission arrays
    private String[] cameraPermission;
    private String[] storagePermission;

    //image pick uri
    private Uri image_uri;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        backBtn = findViewById(R.id.backBtn);
        productIconIv = findViewById(R.id.productIconIv);
        titleEt = findViewById(R.id.titleEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        quantityEt = findViewById(R.id.quantityEt);
        priceEt = findViewById(R.id.priceEt);
        discountPriceEt = findViewById(R.id.discountPriceEt);
        discountNoteEt = findViewById(R.id.discountNoteEt);
        categoryTv = findViewById(R.id.categoryTv);
        addProductBtn = findViewById(R.id.addProductBtn);
        discountSwitch = findViewById(R.id.discountSwitch);
        firebaseAuth = FirebaseAuth.getInstance();
        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //init permission array
        cameraPermission= new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission= new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //on start is unchecked ,so hide discountPriceEt,discountNoteEt;
        discountPriceEt.setVisibility(View.GONE);
        discountNoteEt.setVisibility(View.GONE);

        //if discountSwitch is checked: show discountPriceEt and discountNoteEt,|if discountSwitch is not checked;hide discountNoteEd, discountPriceEt
        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //checked, show discountPriceEt, discountNoteEt
                    discountPriceEt.setVisibility(View.VISIBLE);
                    discountNoteEt.setVisibility(View.VISIBLE);

                }
                else{
                    //unchecked, hide discountPriceEt, discountNoteEt
                    discountPriceEt.setVisibility(View.GONE);
                    discountNoteEt.setVisibility(View.GONE);
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //.Flow
                //1.Input Data
                //2.Validate data
                //3.add data to db
                inputData();
            }
        });

        categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryDialog();


            }
        });

        productIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show Image dialog to pickup
                showImagePickDialog();
            }
        });


    }
    private String productTitle, productDescription, productCategory, productQuantity, originalPrice, discountPrice, discountNote;
    private boolean discountAvailable = false;
    private void inputData() {
        productTitle = titleEt.getText().toString().trim();
        productDescription = descriptionEt.getText().toString().trim();
        productCategory = categoryTv.getText().toString().trim();
        productQuantity = quantityEt.getText().toString().trim();
        originalPrice = priceEt.getText().toString().trim();
        discountAvailable = discountSwitch.isChecked();


        //validate data
        if (TextUtils.isEmpty(productTitle)) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(productDescription)) {
            Toast.makeText(this, "Description is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(productCategory)) {
            Toast.makeText(this, "Category is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(originalPrice)) {
            Toast.makeText(this, "Price is required", Toast.LENGTH_SHORT).show();
            return;

        }

        if (discountAvailable) {
            //product is with discount
            discountPrice = discountPriceEt.getText().toString().trim();
            discountNote = discountNoteEt.getText().toString().trim();


            if (TextUtils.isEmpty(discountPrice)) {
                Toast.makeText(this, "Discount price is required", Toast.LENGTH_SHORT).show();
                return;
            }
        }
            else{
                //product without discount
                discountPrice="0";
                discountNote="";
            }
            addProduct();

        }


    private void addProduct() {
        //add to db
        progressDialog.setMessage("Adding product..");
        progressDialog.show();
        final String timestamp = ""+System.currentTimeMillis();
        if(image_uri==null){
            //upload without image
            //setup data to upload
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("productId",""+timestamp);
            hashMap.put("productTitle",""+productTitle);
            hashMap.put("productDescription",""+productDescription);
            hashMap.put("productCategory",""+productCategory);
            hashMap.put("productQuantity",""+productQuantity);
            hashMap.put("productIcon","");// no image, set empty
            hashMap.put("originalPrice",""+originalPrice);
            hashMap.put("discountPrice",""+discountPrice);
            hashMap.put("discountNote",""+discountNote);
            hashMap.put("discountAvailable",""+discountAvailable);
            hashMap.put("timestamp",""+timestamp);
            hashMap.put("uid",""+firebaseAuth.getUid());
            //add to db

             DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
               //add to db
               progressDialog.dismiss();
                    Toast.makeText(AddProductActivity.this, "Product added", Toast.LENGTH_SHORT).show();

                    //clear data after uploading
                    clearData();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed to adding in db
                    progressDialog.dismiss();
                    Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });


        }
        else{
//upload product with image
            //first upload image to storage
            //name and path of image to uploaded
            String filePathAndName ="product_images/" + ""+ timestamp;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //image uploaded
                    //get url of upload image
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("productId",""+timestamp);
                            hashMap.put("productTitle",""+productTitle);
                            hashMap.put("productDescription",""+productDescription);
                            hashMap.put("productCategory",""+productCategory);
                            hashMap.put("productQuantity",""+productQuantity);
                            hashMap.put("productIcon",""+uri);
                            hashMap.put("originalPrice",""+originalPrice);
                            hashMap.put("discountPrice",""+discountPrice);
                            hashMap.put("discountNote",""+discountNote);
                            hashMap.put("discountAvailable",""+discountAvailable);
                            hashMap.put("timestamp",""+timestamp);
                            hashMap.put("uid",""+firebaseAuth.getUid());
                            //add to db

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                            reference.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //add to db
                                    progressDialog.dismiss();
                                    Toast.makeText(AddProductActivity.this, "Product added", Toast.LENGTH_SHORT).show();

                                    clearData();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //failed to adding in db
                                    progressDialog.dismiss();
                                    Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                    });
//                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
//                    String downloadUri= taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
//
//
//                    while (!uriTask.isSuccessful()){
//                        //url of image received ,upload to db
//                        HashMap<String, Object> hashMap = new HashMap<>();
//                        hashMap.put("productId",""+timestamp);
//                        hashMap.put("productTitle",""+productTitle);
//                        hashMap.put("productDescription",""+productDescription);
//                        hashMap.put("productCategory",""+productCategory);
//                        hashMap.put("productQuantity",""+productQuantity);
//                        hashMap.put("productIcon",""+downloadUri);
//                        hashMap.put("originalPrice",""+originalPrice);
//                        hashMap.put("discountPrice",""+discountPrice);
//                        hashMap.put("discountNote",""+discountNote);
//                        hashMap.put("discountAvailable",""+discountAvailable);
//                        hashMap.put("timestamp",""+timestamp);
//                        hashMap.put("uid",""+firebaseAuth.getUid());
//                        //add to db
//
//                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
//                        reference.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                //add to db
//                                progressDialog.dismiss();
//                                Toast.makeText(AddProductActivity.this, "Product added", Toast.LENGTH_SHORT).show();
//
//                                clearData();
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                //failed to adding in db
//                                progressDialog.dismiss();
//                                Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//
//                            }
//                        });

                  //  }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed uploading image
                    progressDialog.dismiss();
                    Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

    private void categoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product category").setItems(Constants.productCategory, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //get picked category
                String category = Constants.productCategory[which];
                //set picked category
                categoryTv.setText(category);

            }
        }).show();
    }

    private void clearData(){
        //clear data after uploading product
        titleEt.setText("");
        descriptionEt.setText("");
        categoryTv.setText("");
        quantityEt.setText("");
        priceEt.setText("");
        discountPriceEt.setText("");
        discountNoteEt.setText("");
        productIconIv.setImageResource(R.drawable.ic_shopping_primary);
        image_uri=null;
    }

    private void showImagePickDialog() {
        //option to display in dialog
        String[] options ={"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                   if(which==0){
                       //camera click
                       if(checkCameraPermission()){
                           //permission granted
                           pickFromCamera();
                       }
                       else {
                           //permission not granted ,request
                           requestCameraPermission();
                       }
                   }
                   else{
                       //gallery click
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

    private void pickFromGallery(){
        //intent topic image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);

    }

    private void pickFromCamera(){
        //using media store to pick high quality image

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_Image_Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Image_Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);

    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);
        return result; //return true false


    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==
                (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;

    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermission,CAMERA_REQUEST_CODE);
    }

    //handle permission result


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Camera and storage permission are necessary", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        //permission granted
                        pickFromGallery();
                    }
                    else{
                        //permission denied
                        Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    //handle image pick result

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode==RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_CODE){

                //image pick from gallery
                //saved pick image uri
                image_uri = data.getData();
                productIconIv.setImageURI(image_uri);

            }
            else if(requestCode==IMAGE_PICK_CAMERA_CODE){
                //image pick from camera
                productIconIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}