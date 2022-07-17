package com.example.onlinegrocery.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinegrocery.FilterProduct;
import com.example.onlinegrocery.FilterProductUser;
import com.example.onlinegrocery.R;
import com.example.onlinegrocery.activities.ShopDetailsActivity;
import com.example.onlinegrocery.models.ModelProduct;
import com.squareup.picasso.Picasso;



import java.util.ArrayList;

import p32929.androideasysql_library.EasyDB;

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProduct> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productsList, filterList;
    private FilterProductUser filter;

    public AdapterProductUser(Context context, ArrayList<ModelProduct> productsList) {
        this.context = context;
        this.productsList = productsList;
        this.filterList = filterList;
    }

    @NonNull
    @Override
    public HolderProduct onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_user,parent,false);
        return new HolderProduct(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProduct holder, int position) {
        //get data
        final ModelProduct modelProduct = productsList.get(position);
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String originalPrice = modelProduct.getOriginalPrice();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String productTitle = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String productId = modelProduct.getProductId();
        String timestamp = modelProduct.getTimestamp();
        String productIcon = modelProduct.getProductIcon();

        //set data
        holder.titleTv.setText(productTitle);
<<<<<<< HEAD
        holder.discountNoteTv.setText(discountNote+"% OFF");
        holder.descriptionTv.setText(productDescription);
        holder.originalPriceTv.setText("Rs "+originalPrice);
        int dis = Integer.parseInt(originalPrice) - Integer.parseInt(discountPrice);

        holder.discountPriceTv.setText("Rs "+dis);

        if(discountAvailable.equals("true") && !discountNote.isEmpty()){
=======
        holder.discountNoteTv.setText(discountNote);
        holder.descriptionTv.setText(productDescription);
        holder.originalPriceTv.setText("$"+originalPrice);
        holder.discountPriceTv.setText("$"+discountPrice);

        if(discountAvailable.equals("true")){
>>>>>>> 2cf4e41d3aba7a84cc3c318166c75e0ec659342a
            //product is on discount
            holder.discountPriceTv.setVisibility(View.VISIBLE);
            holder.discountNoteTv.setVisibility(View.VISIBLE);
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//add strike through on original price
        }
        else{
            //product is not on discount
            holder.discountPriceTv.setVisibility(View.GONE);
            holder.discountNoteTv.setVisibility(View.GONE);
            holder.originalPriceTv.setPaintFlags(0);
        }
        try{
            Picasso.get().load(productIcon).placeholder(R.drawable.ic_shopping_primary).into(holder.productIconIv);

        }catch (Exception e){
            holder.productIconIv.setImageResource(R.drawable.ic_shopping_primary);
        }

        holder.addToCartTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add product to cart
                showQuantityDialog(modelProduct);

            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show product details

            }
        });

    }

    private double cost =0;
    private double finalCost=0;
    private int quantity =0;
    private void showQuantityDialog(ModelProduct modelProduct) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity, null);
        //init layout views
        ImageView productIv = view.findViewById(R.id.productIv);
        final TextView titleTv = view.findViewById(R.id.titleTv);
        TextView pQuantityTv = view.findViewById(R.id.pQuantity);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView discountNoteTv = view.findViewById(R.id.discountNoteTv);
        TextView priceDiscountTv = view.findViewById(R.id.priceDiscountTv);
        final TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);
        final TextView finalPrice = view.findViewById(R.id.finalPriceTv);
        ImageButton decrementBtn = view.findViewById(R.id.decrementBtn);
        final TextView quantityTv = view.findViewById(R.id.quantityTv);
        ImageButton incrementBtn = view.findViewById(R.id.incrementBtn);
        Button continueBtn = view.findViewById(R.id.continueBtn);

        //get data from model
        final String productId = modelProduct.getProductId();
        String title = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String description = modelProduct.getProductDescription();
        String discountNote = modelProduct.getDiscountNote();
        String image = modelProduct.getProductIcon();

        final String price;
        if(modelProduct.getDiscountAvailable().equals("true")){
            //product have discount
<<<<<<< HEAD
            price = String.valueOf(Integer.parseInt(modelProduct.getOriginalPrice())- Integer.parseInt(modelProduct.getDiscountPrice()));
=======
            price = modelProduct.getDiscountPrice();
>>>>>>> 2cf4e41d3aba7a84cc3c318166c75e0ec659342a
            discountNoteTv.setVisibility(view.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else{
            //product don't have discount
            discountNoteTv.setVisibility(view.GONE);
            priceDiscountTv.setVisibility(view.GONE);
            price = modelProduct.getOriginalPrice();
        }
<<<<<<< HEAD
        cost = Double.parseDouble(price.replaceAll("Rs " , ""));
        finalCost= Double.parseDouble(price.replaceAll("Rs ",""));
=======
        cost = Double.parseDouble(price.replaceAll("$", ""));
        finalCost= Double.parseDouble(price.replaceAll("$",""));
>>>>>>> 2cf4e41d3aba7a84cc3c318166c75e0ec659342a
        quantity=1;

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        //set data
        try{
            Picasso.get().load(image).placeholder(R.drawable.ic_cart).into(productIv);

        }catch (Exception e){
            productIv.setImageResource(R.drawable.ic_cart);

        }
        titleTv.setText(""+title);
        pQuantityTv.setText(""+productQuantity);
        descriptionTv.setText(""+description);
        discountNoteTv.setText(""+discountNote);
        quantityTv.setText(""+quantity);
<<<<<<< HEAD
        int  dis = Integer.parseInt(modelProduct.getOriginalPrice()) - Integer.parseInt(modelProduct.getDiscountPrice());
        originalPriceTv.setText("Rs"+modelProduct.getOriginalPrice());
        priceDiscountTv.setText("Rs"+dis);
        finalPrice.setText("Rs "+dis);
=======
        originalPriceTv.setText("$"+modelProduct.getOriginalPrice());
        priceDiscountTv.setText("$"+modelProduct.getDiscountPrice());
        finalPrice.setText("$"+finalCost);
>>>>>>> 2cf4e41d3aba7a84cc3c318166c75e0ec659342a

        final AlertDialog dialog = builder.create();
        dialog.show();

        //increase quantity of the product

        incrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalCost = finalCost + cost;
                quantity ++;
<<<<<<< HEAD
                finalPrice.setText("Rs "+finalCost);
=======
                finalPrice.setText("$"+finalCost);
>>>>>>> 2cf4e41d3aba7a84cc3c318166c75e0ec659342a
                quantityTv.setText(""+quantity);

            }

        });
        //decrement quantity of product only if quantity>1

        decrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(quantity>1){
                    finalCost = finalCost-cost;
                    quantity --;
<<<<<<< HEAD
                    finalPrice.setText("Rs"+finalCost);
=======
                    finalPrice.setText("$"+finalCost);
>>>>>>> 2cf4e41d3aba7a84cc3c318166c75e0ec659342a
                    quantityTv.setText(""+quantity);
                }
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
<<<<<<< HEAD
//                String title = titleTv.getText().toString().trim();
//                String priceEach = price;
//                String totalPrice = finalPrice.getText().toString().trim().replace("Rs","");
//                String quantity = quantityTv.getText().toString().trim();

                //add to db (SQLite)
                addToCart(productId, title, price, String.valueOf(finalCost),String.valueOf(quantity));
=======
                String title = titleTv.getText().toString().trim();
                String priceEach = price;
                String totalPrice = finalPrice.getText().toString().trim().replace("$","");
                String quantity = quantityTv.getText().toString().trim();

                //add to db (SQLite)
                addToCart(productId, title, priceEach, totalPrice,quantity);
>>>>>>> 2cf4e41d3aba7a84cc3c318166c75e0ec659342a
                dialog.dismiss();
            }
        });
    }

    private int itemId =1;
    private void addToCart(String productId, String title, String priceEach, String price, String quantity) {
        itemId ++;

        EasyDB easyDB = EasyDB.init(context,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                        .addColumn("Item_Id", new String[]{"text", "unique"})
                                .addColumn("Item_PID", new String[]{"text", "not null"})
                                .addColumn("Item_Name", new String[]{"text", "not null"})
                               .addColumn("Item_Price_Each", new String[]{"text", "not null"})
                                .addColumn("Item_Price", new String[]{"text","not null"})
                                .addColumn("Item_Quantity", new String[]{"text", "not null"})

                        .doneTableColumn();
<<<<<<< HEAD
        Boolean b = easyDB.addData("Item_Id", itemId)
=======
        Boolean b = easyDB.addData("ItemId", itemId)
>>>>>>> 2cf4e41d3aba7a84cc3c318166c75e0ec659342a
                .addData("Item_PID", productId)
                .addData("Item_Name", title)
                .addData("Item_Price_Each", priceEach)
                .addData("Item_Price",price)
                .addData("Item_Quantity",quantity)
                .doneDataAdding();
        Toast.makeText(context, "Added to cart..", Toast.LENGTH_SHORT).show();

        //update cartCount
        ((ShopDetailsActivity)context).cartCount();

    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            filter = new FilterProductUser(this, filterList);
        }
        return filter;
    }

    class HolderProduct extends RecyclerView.ViewHolder{
        //ui views
        private ImageView productIconIv;
        private TextView discountNoteTv, titleTv,descriptionTv,addToCartTv,
        discountPriceTv,originalPriceTv;

        public HolderProduct(@NonNull View itemView) {
            super(itemView);
            //ints views
            productIconIv = itemView.findViewById(R.id.productIconIv);
            discountNoteTv = itemView.findViewById(R.id.discountNoteTv);
            titleTv = itemView.findViewById(R.id.titleTv);
            descriptionTv= itemView.findViewById(R.id.descriptionTv);
            addToCartTv = itemView.findViewById(R.id.addToCartTv);
            discountPriceTv = itemView.findViewById(R.id.discountPriceTv);
            originalPriceTv = itemView.findViewById(R.id.originalPriceTv);

        }
    }
}
