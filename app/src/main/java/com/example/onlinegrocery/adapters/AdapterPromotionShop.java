package com.example.onlinegrocery.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinegrocery.R;
import com.example.onlinegrocery.activities.AddPromotionCodeActivity;
import com.example.onlinegrocery.models.ModelPromotion;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterPromotionShop extends RecyclerView.Adapter<AdapterPromotionShop.HolderPromotionShop>{

    private Context context;
    private ArrayList<ModelPromotion> promotionList;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    public AdapterPromotionShop(Context context, ArrayList<ModelPromotion> promotionList) {
        this.context = context;
        this.promotionList = promotionList;

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth =FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderPromotionShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_promotion_shop,parent,false);
        return new HolderPromotionShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderPromotionShop holder, int position) {
        //get data
        final ModelPromotion modelPromotion = promotionList.get(position);

        String id = modelPromotion.getId();
        String timestamp = modelPromotion.getTimestamp();
        String description = modelPromotion.getDescription();
        String promoCode = modelPromotion.getPromoCode();
        String promoPrice = modelPromotion.getPromoPrice();
        String expireDate = modelPromotion.getExpireDate();
        String minimumOrderPrice = modelPromotion.getMinimumOrderPrice();

        //set data
        holder.descriptionTv.setText(description);
        holder.promoPriceTv.setText(promoPrice);
        holder.minimumOrderPriceTv.setText(minimumOrderPrice);
        holder.promoCodeTv.setText("Code: "+promoCode);
        holder.expireDateTv.setText("Expire Date: "+expireDate);


        //to handle click, show edit delete dialog
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDeleteDialog(modelPromotion,holder);
            }
        });
    }

    private void editDeleteDialog(final ModelPromotion modelPromotion, HolderPromotionShop holder) {
        //option to display in dialog
        String[] options = {"Edit", "Delete"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        //handle click
                        if(i==0){
                            //edit clicked

                            editPromoCode(modelPromotion);
                        }else if(i==1){
                            //delete clicks
                        deletePromoCode(modelPromotion);
                        }
                    }
                }).show();


    }

    private void editPromoCode(ModelPromotion modelPromotion) {
        //start and pass data to AddPromotionCodeActivity to edit
        Intent intent = new Intent(context, AddPromotionCodeActivity.class);
        intent.putExtra("promoId", modelPromotion.getId());//will use id to update promo code
        context.startActivity(intent);
    }

    private void deletePromoCode(ModelPromotion modelPromotion) {
        //show progress Bar
        progressDialog.setMessage("Deleting promotion Code..");
        progressDialog.show();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions").child(modelPromotion.getId())
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
           //deleted
                progressDialog.dismiss();
                Toast.makeText(context, "Delete..", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed to delete
                progressDialog.dismiss();
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return promotionList.size();
    }

    class HolderPromotionShop extends RecyclerView.ViewHolder{

        private ImageView iconIv, nextIv;
        private TextView promoCodeTv,promoPriceTv,minimumOrderPriceTv,expireDateTv, descriptionTv;

        public HolderPromotionShop(@NonNull View itemView) {
            super(itemView);
            iconIv = itemView.findViewById(R.id.iconIv);
            nextIv = itemView.findViewById(R.id.nextIv);
            promoCodeTv = itemView.findViewById(R.id.promoCodeTv);
            promoPriceTv = itemView.findViewById(R.id.promoPriceTv);
            minimumOrderPriceTv = itemView.findViewById(R.id.minimumOrderPriceTv);
            expireDateTv = itemView.findViewById(R.id.expireDateTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);

        }
    }
}
