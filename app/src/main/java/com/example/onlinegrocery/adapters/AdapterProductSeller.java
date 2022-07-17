package com.example.onlinegrocery.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
<<<<<<< HEAD
import android.net.Uri;
=======
>>>>>>> 2cf4e41d3aba7a84cc3c318166c75e0ec659342a
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinegrocery.FilterProduct;
import com.example.onlinegrocery.R;
import com.example.onlinegrocery.activities.EditProductActivity;
import com.example.onlinegrocery.models.ModelProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {
    private Context context;
    public ArrayList<ModelProduct> productList, filterList;
    private FilterProduct filter;

    public AdapterProductSeller(Context context, ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
        
    }



    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_seller,parent,false);

        return new HolderProductSeller(view);

    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {
        //get data
        final ModelProduct modelProduct = productList.get(position);
        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String originalPrice = modelProduct.getOriginalPrice();
        String title = modelProduct.getProductTitle();
        String quality = modelProduct.getProductQuantity();
        String description = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        String timestamp = modelProduct.getTimestamp();

        //set data
        holder.titleTv.setText(title);
        holder.quantityTv.setText(quality);
<<<<<<< HEAD
        int dis = Integer.parseInt(originalPrice) - Integer.parseInt(discountPrice);
        holder.discountPriceTv.setText("Rs" +dis);
        holder.originalTv.setText("Rs" +originalPrice);
        holder.discountNoteTv.setText(discountNote);
=======
        holder.discountPriceTv.setText("$" +discountPrice);
        holder.originalTv.setText("$" +originalPrice);
>>>>>>> 2cf4e41d3aba7a84cc3c318166c75e0ec659342a

        if(discountAvailable.equals("true")){
            //product is on discount
            holder.discountPriceTv.setVisibility(View.VISIBLE);
            holder.discountNoteTv.setVisibility(View.VISIBLE);
            holder.originalTv.setPaintFlags(holder.originalTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//add strike through on original price
        }
        else{
            //product is not on discount
            holder.discountPriceTv.setVisibility(View.GONE);
            holder.discountNoteTv.setVisibility(View.GONE);
        }
        try{
<<<<<<< HEAD
            //holder.productIconIv.setImageURI(Uri.parse(icon));
=======
>>>>>>> 2cf4e41d3aba7a84cc3c318166c75e0ec659342a
            Picasso.get().load(icon).placeholder(R.drawable.ic_shopping_primary).into(holder.productIconIv);

        }catch (Exception e){
            holder.productIconIv.setImageResource(R.drawable.ic_shopping_primary);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //handle item clicks , show item details(in bottom sheet)
                detailsBottomSheet(modelProduct);//here model product contains details of clicked product
            }
        });
    }

    private void detailsBottomSheet(ModelProduct modelProduct) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);

        //inflate view for bottomSheet
        View view = LayoutInflater.from(context).inflate(R.layout.bs_product_details_seller,null);
        //set view to bottom sheet
        bottomSheetDialog.setContentView(view);
        //init views of bottom sheet

        ImageButton backBtn = view. findViewById(R.id.backBtn);
        ImageButton deleteBtn = view.findViewById(R.id.deleteBtn);
        ImageButton editBtn = view.findViewById(R.id.editBtn);
        TextView nameTv = view.findViewById(R.id.nameTv);
        ImageView productIconIv = view.findViewById(R.id.productIconIv);
        TextView discountNoteTv = view.findViewById(R.id.discountNoteTv);
        TextView titleTv = view.findViewById(R.id.titleTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView categoryTv = view.findViewById(R.id.categoryTv);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        TextView discountPriceTv = view.findViewById(R.id.discountPriceTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);

        //get data

        final String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String originalPrice = modelProduct.getOriginalPrice();
        final String title = modelProduct.getProductTitle();
        String quality = modelProduct.getProductQuantity();
        String description = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        String timestamp = modelProduct.getTimestamp();
        String category = modelProduct.getProductCategory();

        //set data
        titleTv.setText(title);
        descriptionTv.setText(description);
        categoryTv.setText(category);
        quantityTv.setText(quality);
        discountNoteTv.setText(discountNote);
<<<<<<< HEAD
        discountNoteTv.setText("Rs" +discountNote);
        discountPriceTv.setText("Rs" +discountPrice);
=======
        discountNoteTv.setText("$" +discountNote);
        discountPriceTv.setText("$" +discountPrice);
>>>>>>> 2cf4e41d3aba7a84cc3c318166c75e0ec659342a

        if(discountAvailable.equals("true")){
            //product is on discount
           discountPriceTv.setVisibility(View.VISIBLE);
           discountNoteTv.setVisibility(View.VISIBLE);
<<<<<<< HEAD
           discountPriceTv.setPaintFlags(discountPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//add strike through on original price
=======
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//add strike through on original price
>>>>>>> 2cf4e41d3aba7a84cc3c318166c75e0ec659342a
        }
        else{
            //product is not on discount
           discountPriceTv.setVisibility(View.GONE);
            discountNoteTv.setVisibility(View.GONE);
        }
        try{
            Picasso.get().load(icon).placeholder(R.drawable.ic_shopping_primary).into(productIconIv);

        }catch (Exception e){
           productIconIv.setImageResource(R.drawable.ic_shopping_primary);
        }
//show dialog
        bottomSheetDialog.show();

        //click edit
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit product activity,pass id of product
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra("productId", id);
                context.startActivity(intent);


            }
        });

        //click delete
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show delete confirm dialog
                bottomSheetDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure want to delete product" +title+"?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //delete
                                deleteProduct(id);//id is product id

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

backBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        //dismiss bottomSheet
        bottomSheetDialog.dismiss();

    }
});


    }

    private void deleteProduct(String id) {

        //delete product using its id

        FirebaseAuth firebaseAuth =  FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //product delete
                Toast.makeText(context, "Product delete", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed delete product
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            filter = new FilterProduct(this, filterList);
        }
        return filter;
    }

    class HolderProductSeller extends RecyclerView.ViewHolder{

        /* holds view recycler */
        private ImageView productIconIv;
        private TextView discountNoteTv, titleTv, quantityTv, discountPriceTv,originalTv;


        public HolderProductSeller(@NonNull View itemView) {
            super(itemView);

            productIconIv = itemView.findViewById(R.id.productIconIv);
            discountNoteTv = itemView.findViewById(R.id.discountNoteTv);
            titleTv = itemView.findViewById(R.id.titleTv);
            quantityTv = itemView.findViewById(R.id.quantityTv);
            discountPriceTv = itemView.findViewById(R.id.discountPriceTv);
            originalTv = itemView.findViewById(R.id.originalPriceTv);
        }
    }
}
