package com.example.serviceco;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserData extends AppCompatActivity {

    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private TextView textView;

    private final List<MyItems> myItemsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data);

        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        textView = findViewById(R.id.textview_login_head);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserData.this, Home.class);
                startActivity(intent);
                finish();
            }
        });

        recyclerView.setHasFixedSize(true);

        // setting layout manager to the recyclerview. Ex. LinearLayoutManager (vertical mode)
        recyclerView.setLayoutManager(new LinearLayoutManager(UserData.this));

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myItemsList.clear();

                for (DataSnapshot users : snapshot.child("users").getChildren()) {

                    // to prevent app crash check if the user has all the details in Firebase Database
                    if (users.hasChild("name") && users.hasChild("role") && users.hasChild("ratings") && users.hasChild("location")  && users.hasChild("phone") && users.hasChild("about")) {

                        // getting users details from Firebase Database and store into the List one by one
                        final String getName = users.child("name").getValue(String.class);
                        final String getRole = users.child("role").getValue(String.class);
                        final String getRating = users.child("ratings").getValue(String.class);
                        final String getLocation = users.child("location").getValue(String.class);
                        final String getImg = users.child("img").getValue(String.class);
                        final String getPhone = users.child("phone").getValue(String.class);
                        final String getAbout = users.child("about").getValue(String.class);

                        // creating user item with user details
                        MyItems myItems = new MyItems(getName, getRole, getRating, getLocation, getImg, getPhone, getAbout);

                        // adding this user item to List
                        myItemsList.add(myItems);
                    }
                }
                recyclerView.setAdapter(new MyAdapter(myItemsList, UserData.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}