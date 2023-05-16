package com.example.serviceco;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class Home extends AppCompatActivity {

    SwipeRefreshLayout swipeRefreshLayout;

    private TextView textViewWelcome;
    private ProgressBar progressBar;
    private String fullName;
    private CardView cook, maid, delivery, labour, babysitter;
    private ImageView imageView;
    private FirebaseAuth authProfile;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getSupportActionBar().hide();
        swipetoLoad();
        textViewWelcome = findViewById(R.id.textView_show_welcome);
        progressBar = findViewById(R.id.progress_bar);

        //Set on click listener on ImageView to open upload profile picture
        imageView = findViewById(R.id.imageView_profile_dp);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, UserProfileActivity.class);
                startActivity(intent);
            }
        });

        cook = findViewById(R.id.cook);
        cook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Cook.class);
                startActivity(intent);
            }
        });

        delivery = findViewById(R.id.delivery);
        delivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Delivery.class);
                startActivity(intent);
            }
        });

        maid = findViewById(R.id.maid);
        maid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Maid.class);
                startActivity(intent);
            }
        });

        babysitter = findViewById(R.id.babysitter);
        babysitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, BabySitter.class);
                startActivity(intent);
            }
        });

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if(firebaseUser==null){
            Toast.makeText(Home.this, "Something went wrong! User's details are not available at the moment",Toast.LENGTH_LONG).show();
        }
        else{
            checkifEmailVerification(firebaseUser);
            progressBar.setVisibility(View.VISIBLE);
            showUserProfile(firebaseUser);
        }
        
    }

    private void swipetoLoad() {
        swipeContainer = findViewById(R.id.swipeContainer);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startActivity(getIntent());
                finish();
                overridePendingTransition(0,0);
                swipeContainer.setRefreshing(false);
            }
        });

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
    }

    //User coming to profile after successful registration
    private void checkifEmailVerification(FirebaseUser firebaseUser) {
        if(!firebaseUser.isEmailVerified()){
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        //Setup alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
        builder.setTitle("Email not Verified");
        builder.setMessage("Please verify your email now. You can not login without verification");

        //Open email if user clicks/tap Continue button
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);         //To email app in new window and not within our app
                startActivity(intent);
            }
        });

        //Create the AlertDialog
        AlertDialog alertDialog = builder.create();

        //Show the dialog
        alertDialog.show();
    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userUID = firebaseUser.getUid();         //To get the unique id of the user

        //Extracting User Reference from Database from "Registered User"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        referenceProfile.child(userUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if(readUserDetails!=null){
                    fullName = firebaseUser.getDisplayName();
                    StringBuilder sb=new StringBuilder();
                    int unicode = 0x1F60A;
                    String s = String.valueOf(Character.toChars(unicode));
                    int i=0;
                    while(sb.length() <9 && fullName.charAt(i)!=' ')
                    {
                        sb.append(fullName.charAt(i));
                        i++;
                    }
                    if(sb.length()==9)
                    {
                        textViewWelcome.setText(sb.toString());
                    }
                    else
                    {
                        for(i=sb.length();i<10;i++)
                        {
                            sb.append(" ");
                        }
                        textViewWelcome.setText(sb.toString());
                    }

                    //Show User DP(After user has uploaded)
                    Uri uri = firebaseUser.getPhotoUrl();
                    //Image Viewer setImageURI() should not be used with regular URIs. So we are using Picasso here
                    Picasso.get().load(uri).into(imageView);

                }
                else{
                    Toast.makeText(Home.this, "Something went wrong!",Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Home.this, "Something went wrong!",Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });

    }
}