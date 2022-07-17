package com.example.onlinegrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onlinegrocery.Constants;
import com.example.onlinegrocery.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton backBtn, ibDelete;
    private SwitchCompat fcmSwitch;
    private TextView notificationStatusTv;

    private static final String enabledMessage ="Notifications are enabled";
    private static  final String disabledMessage ="Notification are disabled";

    private FirebaseAuth firebaseAuth;
    private boolean isChecked = false;

    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        backBtn = findViewById(R.id.backBtn);
        fcmSwitch =findViewById(R.id.fcmSwitch);
        notificationStatusTv = findViewById(R.id.notificationStatusTv);
        ibDelete = findViewById(R.id.ib_delete);
        firebaseAuth =FirebaseAuth.getInstance();

        //init sharePreferences
        sp = getSharedPreferences("SETTINGS_SP",MODE_PRIVATE);
        //check last selected options,true/false

        isChecked= sp.getBoolean("FCM_ENABLED", false);
        fcmSwitch.setChecked(isChecked);

        if(isChecked){
            //was enabled
            notificationStatusTv.setText(enabledMessage);
        }else {
            //was disabled
            notificationStatusTv.setText(disabledMessage);
        }


        //add switch check change listner to enable disable notification
        fcmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //checked enable notification
                    subscribeToTopic();
                }else {
                    //unchecked, disable notification
                    unSubscribeToTopic();
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //AlertDialog.Builder builder = new AlertDialog.Builder(this);
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Delete")
                        .setMessage("Are you sure want to delete your Account ?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //delete
                                deleteAccount();

                            }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //cancel, dismiss dialog
                                dialog.dismiss();


                            }
                        }).show();
            }
        });
    }
    private void deleteAccount() {

        //delete product using its id

        FirebaseAuth firebaseAuth =  FirebaseAuth.getInstance();
       FirebaseUser user = firebaseAuth.getCurrentUser();
//       if(user != null) {
//           DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
//
//
//        reference.child(firebaseAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//               @Override
//               public void onSuccess(Void aVoid) {
//                   //product delete
//                   user.delete();
//                   Toast.makeText(SettingsActivity.this, "Your Account delete", Toast.LENGTH_SHORT).show();
//                   Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//                   startActivity(intent);
//
//               }
//           }).addOnFailureListener(new OnFailureListener() {
//               @Override
//               public void onFailure(@NonNull Exception e) {
//                   //failed delete product
//                   Toast.makeText(SettingsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
//
//               }
//           });
        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(SettingsActivity.this, "Your Account delete", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                    finish();
                }else {
                    Toast.makeText(SettingsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
       }
      //  firebaseAuth.auth
   // }

    private void subscribeToTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //subscribe successfully
                //save setting
                spEditor = sp.edit();
                spEditor.putBoolean("FCM_ENABLED", true);
                spEditor.apply();
                Toast.makeText(SettingsActivity.this, ""+enabledMessage, Toast.LENGTH_SHORT).show();
                notificationStatusTv.setText(enabledMessage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
           //failed to subscribe
                Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void unSubscribeToTopic(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
           //unsubscribe
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED", false);
                        spEditor.apply();
                        Toast.makeText(SettingsActivity.this, ""+disabledMessage, Toast.LENGTH_SHORT).show();
                        notificationStatusTv.setText(disabledMessage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed to unSubscribe
                Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}