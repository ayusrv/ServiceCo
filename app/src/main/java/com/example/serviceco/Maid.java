package com.example.serviceco;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Maid extends AppCompatActivity {

    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private TextView textView, phone;

    private final List<MyItems> myItemsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baby_sitter);

        getSupportActionBar().hide();
        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        textView = findViewById(R.id.textview_login_head);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Maid.this, Home.class);
                startActivity(intent);
                finish();
            }
        });

        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(Maid.this));
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myItemsList.clear();

                for(DataSnapshot delivery: snapshot.child("maid").getChildren()){
                    if(delivery.hasChild("name") && delivery.hasChild("role") && delivery.hasChild("ratings") && delivery.hasChild("location")  && delivery.hasChild("phone") && delivery.hasChild("about")){
                        final String getName = delivery.child("name").getValue(String.class);
                        final String getRole = delivery.child("role").getValue(String.class);
                        final String getRating = delivery.child("ratings").getValue(String.class);
                        final String getLocation = delivery.child("location").getValue(String.class);
                        final String getImg = delivery.child("img").getValue(String.class);
                        final String getPhone = delivery.child("phone").getValue(String.class);
                        final String getAbout = delivery.child("about").getValue(String.class);

                        MyItems myItems = new MyItems(getName, getRole, getRating, getLocation, getImg, getPhone, getAbout);

                        // adding this user item to List
                        myItemsList.add(myItems);
                    }
                }
                recyclerView.setAdapter(new MyAdapter(myItemsList, Maid.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}