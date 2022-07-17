package com.example.onlinegrocery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinegrocery.R;
import com.example.onlinegrocery.activities.ShopDetailsActivity;
import com.example.onlinegrocery.models.ModelCartItem;

import java.util.ArrayList;
import java.util.Date;

import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem>{

    private Context context;
    private ArrayList<ModelCartItem> cartItems;

    public AdapterCartItem(Context context, ArrayList<ModelCartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_cartitems,parent,false);

        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, final int position) {

        //get data
        ModelCartItem modelCartItem = cartItems.get(position);
        final String id = modelCartItem.getId();
        String pId = modelCartItem.getpId();
        String title = modelCartItem.getName();
        final String cost = modelCartItem.getCost();
        String price = modelCartItem.getPrice();
        String quantity = modelCartItem.getQuantity();

        //set data
        holder.itemTitleTv.setText(""+title);
        holder.itemPriceTv.setText(""+cost);
        holder.itemQuantityTv.setText("["+quantity+"]");//eg[3]
        holder.itemPriceEachTv.setText(""+price);

        //handle remove click listener ,delete item from cart
        holder.itemRemoveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //will create table if not exists, but in that case will must exists
                EasyDB easyDB = EasyDB.init(context,"ITEMS_DB")
                        .setTableName("ITEMS_TABLE")
                        .addColumn("Item_Id", new String[]{"text", "unique"})
                        .addColumn("Item_PID", new String[]{"text", "not null"})
                        .addColumn("Item_Name", new String[]{"text", "not null"})
                        .addColumn("Item_Price_Each", new String[]{"text", "not null"})
                        .addColumn("Item_Price", new String[]{"text","not null"})
                        .addColumn("Item_Quantity", new String[]{"text", "not null"})
                        .doneTableColumn();

                easyDB.deleteRow(1, id);
                Toast.makeText(context, "Remove from Cart..", Toast.LENGTH_SHORT).show();
                //refresh list
                cartItems.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();
                //adjust the subtotal after product remove

                double subTotalWithoutDiscount =((ShopDetailsActivity)context).allTotalPrice;
<<<<<<< HEAD
                double subTotalAfterProductRemove =subTotalWithoutDiscount - Double.parseDouble(cost.replace("Rs ",""));
                ((ShopDetailsActivity)context).allTotalPrice = subTotalAfterProductRemove;

                ((ShopDetailsActivity)context).sTotalTv.setText("Rs "+String.format("%.2f",((ShopDetailsActivity)context).allTotalPrice));
                //once subTotal updated check minimumOrderPrice of promoCode
                double promoPrice = Double.parseDouble(((ShopDetailsActivity)context).promoPrice);
                double deliveryFee = Double.parseDouble(((ShopDetailsActivity)context).deliveryFee.replace("Rs ",""));
=======
                double subTotalAfterProductRemove =subTotalWithoutDiscount - Double.parseDouble(cost.replace("$",""));
                ((ShopDetailsActivity)context).allTotalPrice = subTotalAfterProductRemove;

                ((ShopDetailsActivity)context).sTotalTv.setText("$"+String.format("%.2f",((ShopDetailsActivity)context).allTotalPrice));
                //once subTotal updated check minimumOrderPrice of promoCode
                double promoPrice = Double.parseDouble(((ShopDetailsActivity)context).promoPrice);
                double deliveryFee = Double.parseDouble(((ShopDetailsActivity)context).deliveryFee.replace("$",""));
>>>>>>> 2cf4e41d3aba7a84cc3c318166c75e0ec659342a
                //check if promo code applied
                if(((ShopDetailsActivity)context).isPromoCodeApplied){
                    //applied
                    if(subTotalAfterProductRemove<Double.parseDouble(((ShopDetailsActivity)context).promoMinimumOrderPrice)){
                        //current order price is less than minimum required price
<<<<<<< HEAD
                        Toast.makeText(context, "This code is valid for order with minimum Amount: Rs "+((ShopDetailsActivity)context).promoMinimumOrderPrice
=======
                        Toast.makeText(context, "This code is valid for order with minimum Amount: $"+((ShopDetailsActivity)context).promoMinimumOrderPrice
>>>>>>> 2cf4e41d3aba7a84cc3c318166c75e0ec659342a
                                , Toast.LENGTH_SHORT).show();

                        ((ShopDetailsActivity)context).applyBtn.setVisibility(View.GONE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setVisibility(View.GONE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setText("");
<<<<<<< HEAD
                        ((ShopDetailsActivity)context).discountTv.setText("Rs 0");
                        ((ShopDetailsActivity)context).isPromoCodeApplied= false;
                        //show new net total after delivery fee
                        ((ShopDetailsActivity)context).allTotalPriceTv.setText("Rs "+String.format("%.2f",Double.parseDouble(String.format("%.2f", subTotalAfterProductRemove + deliveryFee))));
=======
                        ((ShopDetailsActivity)context).discountTv.setText("$0");
                        ((ShopDetailsActivity)context).isPromoCodeApplied= false;
                        //show new net total after delivery fee
                        ((ShopDetailsActivity)context).allTotalPriceTv.setText("$"+String.format("%.2f",Double.parseDouble(String.format("%.2f", subTotalAfterProductRemove + deliveryFee))));
>>>>>>> 2cf4e41d3aba7a84cc3c318166c75e0ec659342a


                    } else {
                        ((ShopDetailsActivity)context).applyBtn.setVisibility(View.VISIBLE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setVisibility(View.VISIBLE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setText(((ShopDetailsActivity)context).promoDescription);
                        //so new total price after adding deliveryFee and Subtracting with promoPrice
                        ((ShopDetailsActivity)context).isPromoCodeApplied = true;
<<<<<<< HEAD
                        ((ShopDetailsActivity)context).allTotalPriceTv.setText("Rs " +String.format("%.2f",Double.parseDouble(String.format("%.2f",subTotalAfterProductRemove + deliveryFee - promoPrice))));
=======
                        ((ShopDetailsActivity)context).allTotalPriceTv.setText("$" +String.format("%.2f",Double.parseDouble(String.format("%.2f",subTotalAfterProductRemove + deliveryFee - promoPrice))));
>>>>>>> 2cf4e41d3aba7a84cc3c318166c75e0ec659342a

                    }

                } else {
                    //not applied
<<<<<<< HEAD
                    ((ShopDetailsActivity)context).allTotalPriceTv.setText("Rs " +String.format("%.2f",Double.parseDouble(String.format("%.2f", subTotalAfterProductRemove + deliveryFee))));
=======
                    ((ShopDetailsActivity)context).allTotalPriceTv.setText("$" +String.format("%.2f",Double.parseDouble(String.format("%.2f", subTotalAfterProductRemove + deliveryFee))));
>>>>>>> 2cf4e41d3aba7a84cc3c318166c75e0ec659342a
                }

                //after removing item from cart , update cart count
                ((ShopDetailsActivity)context).cartCount();


            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItems.size();//return no of records
    }

    //view Holder class
    class HolderCartItem extends RecyclerView.ViewHolder{


        //ui views of row_cartItems.xml
        private TextView itemTitleTv, itemPriceTv, itemPriceEachTv,itemQuantityTv,itemRemoveTv;

        public HolderCartItem(@NonNull View itemView) {
            super(itemView);
            //init views
            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
            itemRemoveTv = itemView.findViewById(R.id.itemRemoveTv);

        }
    }
}
