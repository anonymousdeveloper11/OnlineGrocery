package com.example.onlinegrocery.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.text.style.IconMarginSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinegrocery.FilterOrderShop;
import com.example.onlinegrocery.R;
import com.example.onlinegrocery.activities.OrderDetailsSellerActivity;
import com.example.onlinegrocery.models.ModelOrderShop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderShop extends RecyclerView.Adapter<AdapterOrderShop.HolderOrderShop> implements Filterable {

    private Context context;
    public ArrayList<ModelOrderShop> orderShopList,filterList;
    private FilterOrderShop filter;

    public AdapterOrderShop(Context context, ArrayList<ModelOrderShop> orderShopList) {
        this.context = context;
        this.orderShopList = orderShopList;
        this.filterList = orderShopList;
    }

    @NonNull
    @Override
    public HolderOrderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_order_seller,parent,false);

        return new HolderOrderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderShop holder, int position) {

        //get data at position
        ModelOrderShop modelOrderShop = orderShopList.get(position);

        final String orderId = modelOrderShop.getOrderId();
        final String orderBy = modelOrderShop.getOrderBy();
        String orderTo = modelOrderShop.getOrderTo();
        String orderCost = modelOrderShop.getOrderCost();
        String orderStatus = modelOrderShop.getOrderStatus();
        String orderTime = modelOrderShop.getOrderTime();

        //get user info
        loadUserInfo(modelOrderShop, holder);

        //set data
        holder.amountTv.setText("Amount:$"+orderCost);
        holder.statusTv.setText(orderStatus);
        holder.orderIdTv.setText("OrderId:"+orderId);

        //change order status color
        if(orderStatus.equals("In Progress")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }else if(orderStatus.equals("Completed")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorGreen));
        }else if(orderStatus.equals("Cancelled")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorRed));
        }
        //convert Time to proper format

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatDate = DateFormat.format("dd/MM/yyyy", calendar).toString();
        holder.orderDateTv.setText(formatDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open order details

                Intent intent = new Intent(context, OrderDetailsSellerActivity.class);
                intent.putExtra("orderId", orderId);//load order info
                intent.putExtra("orderBy", orderBy);//load info of the user who placed oder
                context.startActivity(intent);
            }
        });
    }
//to load  email of the buyer/user: modelOrderShop.getOrderBy contains uid of that user/buyer
    private void loadUserInfo(ModelOrderShop modelOrderShop, final HolderOrderShop holder) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(modelOrderShop.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String email = ""+dataSnapshot.child("email").getValue();
                        holder.emailTv.setText(email);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    @Override
    public int getItemCount() {
        return orderShopList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            //init filter
            filter = new FilterOrderShop(this,filterList);
        }
        return filter;
    }

    class HolderOrderShop extends RecyclerView.ViewHolder{


        private TextView orderIdTv,orderDateTv,emailTv,amountTv,statusTv;
        private ImageView nextIv;
        public HolderOrderShop(@NonNull View itemView) {
            super(itemView);
            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            orderDateTv = itemView.findViewById(R.id.orderDateTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            amountTv = itemView.findViewById(R.id.amountTv);
            statusTv =itemView.findViewById(R.id.statusTv);
            nextIv = itemView.findViewById(R.id.nextIv);        }
    }
}
