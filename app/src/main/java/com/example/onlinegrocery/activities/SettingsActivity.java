package com.example.onlinegrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.onlinegrocery.Constants;
import com.example.onlinegrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton backBtn;
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
    }
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