package com.example.onlinegrocery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinegrocery.R;
import com.example.onlinegrocery.models.ModelUser;

import java.util.ArrayList;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.HolderUser>{
    private Context context;
    private ArrayList<ModelUser> userList;

    public AdapterUser(Context context, ArrayList<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public HolderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user,parent,false);
        return new HolderUser(view);    }

    @Override
    public void onBindViewHolder(@NonNull HolderUser holder, int position) {

        ModelUser modelUser = userList.get(position);
        String name = modelUser.getName();
        String email = modelUser.getEmail();
        String address= modelUser.getAddress();
        String phone = modelUser.getPhone();
        String userType = modelUser.getUserType();

        holder.nameTv.setText(name);
        holder.emailTv.setText(email);
        holder.addressTv.setText(address);
        holder.phoneTv.setText(phone);
        holder.userType.setText(userType);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class HolderUser extends RecyclerView.ViewHolder{

        private TextView nameTv, emailTv,addressTv,phoneTv,userType;
        private ImageButton ibDelete;

        public HolderUser(@NonNull View itemView) {
            super(itemView);

            nameTv = itemView.findViewById(R.id.tvName);
            emailTv = itemView.findViewById(R.id.tvEmail);
            addressTv = itemView.findViewById(R.id.tvAddress);
            phoneTv = itemView.findViewById(R.id.tvPhone);
            userType = itemView.findViewById(R.id.tvUserType);
        }
    }
}
