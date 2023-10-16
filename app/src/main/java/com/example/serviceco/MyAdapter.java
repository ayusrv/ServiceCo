package com.example.serviceco;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private final List<MyItems> items;
    private final Context context;

    public MyAdapter(List<MyItems> items, Context context) {
        this.items = items;
        this.context = context;
    }


    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layoutss, null));
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {

        MyItems myItems = items.get(position);


        // setting user details to TextViews
        holder.name.setText(myItems.getName());
        holder.role.setText(myItems.getRole());
        holder.rating.setText(myItems.getRating());
        holder.location.setText(myItems.getLocation());
        Glide.with(holder.img.getContext())
                .load(myItems.getImg())
                .placeholder(com.google.android.gms.base.R.drawable.common_google_signin_btn_icon_dark)
                .error(com.google.firebase.database.collection.R.drawable.common_google_signin_btn_icon_dark)
                .into(holder.img);


        holder.phone.setText(myItems.getPhone());
        holder.about.setText(myItems.getAbout());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{
        private final TextView name, role, rating, location, phone, about;
        private final ImageView img;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            // getting TextViews from recyclerview_adapter_layout.xml file
            name = itemView.findViewById(R.id.name);
            role = itemView.findViewById(R.id.role);
            rating = itemView.findViewById(R.id.rating);
            location = itemView.findViewById(R.id.location);
            img = itemView.findViewById(R.id.img);
            phone = itemView.findViewById(R.id.phone);
            about = itemView.findViewById(R.id.about);
        }
    }
}
