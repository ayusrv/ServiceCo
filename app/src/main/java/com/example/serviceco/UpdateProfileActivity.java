package com.example.serviceco;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfileActivity extends AppCompatActivity {

    private EditText editTextUpdateName, editTextUpdateLocation, editTextUpdateMobile;
    private String textFullName, textLocation, textMobile;
    private FirebaseAuth authProfile;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        getSupportActionBar().setTitle("Update Profile Data");
        progressBar = findViewById(R.id.progress_bar);
        editTextUpdateName = findViewById(R.id.edittext_update_full_name);
        editTextUpdateLocation = findViewById(R.id.edittext_update_location);
        editTextUpdateMobile = findViewById(R.id.edittext_update_phone);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        //Show profile method
        showProfile(firebaseUser);

        //Upload profile pic
        Button buttonUploadProfilePic = findViewById(R.id.button_upload_profile_pic);
        buttonUploadProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfileActivity.this,UploadProfilePicActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Update email
        Button buttonUpdateEmail = findViewById(R.id.button_update_email);
        buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfileActivity.this,UpdateEmailActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Update profile
        Button buttonUpdateProfile = findViewById(R.id.button_updateProfile);
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(firebaseUser);
            }
        });
    }

    //Update Profile
    private void updateProfile(FirebaseUser firebaseUser){
        //Validate mobile number
        String mobileRegex = "[6-9][0-9]{9}";
        Matcher mobileMatcher;
        Pattern mobilePattern = Pattern.compile(mobileRegex);
        mobileMatcher = mobilePattern.matcher(textMobile);
        if(TextUtils.isEmpty(textFullName)){
            Toast.makeText(UpdateProfileActivity.this,"Please Enter Your Full Name",Toast.LENGTH_SHORT).show();
            editTextUpdateName.setError("Full Name Required");
            editTextUpdateName.requestFocus();
        }
        else if(TextUtils.isEmpty(textLocation)){
            Toast.makeText(UpdateProfileActivity.this,"Please Enter Your Location",Toast.LENGTH_SHORT).show();
            editTextUpdateLocation.setError("City Required");
            editTextUpdateLocation.requestFocus();
        }
        else if(TextUtils.isEmpty(textMobile)){
            Toast.makeText(UpdateProfileActivity.this,"Please Enter Your Phone Number",Toast.LENGTH_SHORT).show();
            editTextUpdateMobile.setError("Phone Number Required");
            editTextUpdateMobile.requestFocus();
        }
        else if(!mobileMatcher.find()){
            Toast.makeText(UpdateProfileActivity.this,"Please re-enter Your Phone Number",Toast.LENGTH_SHORT).show();
            editTextUpdateMobile.setError("Valid Phone Number Required");
            editTextUpdateMobile.requestFocus();
        }
        else if(textMobile.length()!=10){
            Toast.makeText(UpdateProfileActivity.this,"Please re-enter Your Phone Number",Toast.LENGTH_SHORT).show();
            editTextUpdateMobile.setError("Valid Phone Number Required");
            editTextUpdateMobile.requestFocus();
        }
        else{
            //Obtain the entered data by user
            textFullName = editTextUpdateName.getText().toString();
            textLocation = editTextUpdateLocation.getText().toString();
            textMobile = editTextUpdateMobile.getText().toString();

            //Enter user data into the firebase realtime database. Set up dependencies
            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textLocation, textMobile);

            //Extract User reference from database for "Registered User"
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

            String userUID = firebaseUser.getUid();
            progressBar.setVisibility(View.VISIBLE);

            referenceProfile.child(userUID).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){

                        //Setting new Display Name
                        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(textFullName).build();
                        firebaseUser.updateProfile(profileChangeRequest);

                        Toast.makeText(UpdateProfileActivity.this, "Update Successful!", Toast.LENGTH_SHORT).show();

                        //Stop user for returning to UpdateProfileActivity on pressing back button and close activity
                        Intent intent = new Intent(UpdateProfileActivity.this, UserProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        try{
                            throw task.getException();
                        }catch (Exception e ){
                            Toast.makeText(UpdateProfileActivity.this, e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    //Fetch data from firebase and display accordingly
    private void showProfile(FirebaseUser firebaseUser){
        String UID = firebaseUser.getUid();

        //Extracting User reference from database for "Registered User"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        progressBar.setVisibility(View.VISIBLE);

        referenceProfile.child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                 ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                 if(readUserDetails!=null){
                     textFullName = firebaseUser.getDisplayName();
                     textLocation = readUserDetails.location;
                     textMobile = readUserDetails.mobile;

                     editTextUpdateName.setText(textFullName);
                     editTextUpdateLocation.setText(textLocation);
                     editTextUpdateMobile.setText(textMobile);
                 }
                 else{
                     Toast.makeText(UpdateProfileActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                 }
                 progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Creating ActionBar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu items
        getMenuInflater().inflate(R.menu.common_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //When any menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id==R.id.menu_refresh){
            //Refresh the page
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        }
        else if(id==R.id.menu_update_profile){
            Intent intent = new Intent(UpdateProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        }
        else if(id==R.id.menu_update_email){
            Intent intent = new Intent(UpdateProfileActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        }
        else if(id==R.id.menu_settings){
            Toast.makeText(UpdateProfileActivity.this,"User Setting",Toast.LENGTH_SHORT).show();
        }
        else if(id==R.id.menu_change_password){
            Intent intent = new Intent(UpdateProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        }
        else if(id==R.id.menu_delete_profile){
            Intent intent = new Intent(UpdateProfileActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
            finish();
        }
        else if(id==R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(UpdateProfileActivity.this,"Logged Out",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UpdateProfileActivity.this, MainActivity.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        else{
            Toast.makeText(UpdateProfileActivity.this,"Something went wrong!",Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}