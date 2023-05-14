package com.example.serviceco;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextFullName, editTextEmail, editTextLocation, editTextPhone, editTextPassword, editTextConfirmPassword;
    private ProgressBar progressBar;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().hide();
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.green)));

        //EditText view by id
        progressBar = findViewById(R.id.progress_bar);
        editTextFullName = findViewById(R.id.edittext_register_full_name);
        editTextEmail = findViewById(R.id.edittext_register_email);
        editTextLocation = findViewById(R.id.edittext_register_location);
        editTextPhone = findViewById(R.id.edittext_register_phone);
        editTextPassword = findViewById(R.id.edittext_register_password);
        editTextConfirmPassword = findViewById(R.id.edittext_register_confirm_password);
        Button register = findViewById(R.id.button_register);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Obtain the entered data
                String txtFullName = editTextFullName.getText().toString();
                String txtEmail = editTextEmail.getText().toString();
                String txtLocation = editTextLocation.getText().toString();
                String txtPhone = editTextPhone.getText().toString();
                String txtPassword = editTextPassword.getText().toString();
                String txtCnfPassword = editTextConfirmPassword.getText().toString();

                //Validate mobile number
                String mobileRegex = "[6-9][0-9]{9}";
                Matcher mobileMatcher;
                Pattern mobilePattern = Pattern.compile(mobileRegex);
                mobileMatcher = mobilePattern.matcher(txtPhone);
                if(TextUtils.isEmpty(txtFullName)){
                    Toast.makeText(RegisterActivity.this,"Please Enter Your Full Name",Toast.LENGTH_SHORT).show();
                    editTextFullName.setError("Full Name Required");
                    editTextFullName.requestFocus();
                }
                else if(TextUtils.isEmpty(txtEmail)){
                    Toast.makeText(RegisterActivity.this,"Please Enter Your Email",Toast.LENGTH_SHORT).show();
                    editTextEmail.setError("Email Required");
                    editTextEmail.requestFocus();
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(txtEmail).matches()){
                    Toast.makeText(RegisterActivity.this,"Please re-enter Your Email",Toast.LENGTH_SHORT).show();
                    editTextEmail.setError("Valid Email Required");
                    editTextEmail.requestFocus();
                }
                else if(TextUtils.isEmpty(txtLocation)){
                    Toast.makeText(RegisterActivity.this,"Please Enter Your Location",Toast.LENGTH_SHORT).show();
                    editTextLocation.setError("City Required");
                    editTextLocation.requestFocus();
                }
                else if(TextUtils.isEmpty(txtPhone)){
                    Toast.makeText(RegisterActivity.this,"Please Enter Your Phone Number",Toast.LENGTH_SHORT).show();
                    editTextPhone.setError("Phone Number Required");
                    editTextPhone.requestFocus();
                }
                else if(!mobileMatcher.find()){
                    Toast.makeText(RegisterActivity.this,"Please re-enter Your Phone Number",Toast.LENGTH_SHORT).show();
                    editTextPhone.setError("Valid Phone Number Required");
                    editTextPhone.requestFocus();
                }
                else if(txtPhone.length()!=10){
                    Toast.makeText(RegisterActivity.this,"Please re-enter Your Phone Number",Toast.LENGTH_SHORT).show();
                    editTextPhone.setError("Valid Phone Number Required");
                    editTextPhone.requestFocus();
                }
                else if(TextUtils.isEmpty(txtPassword)){
                    Toast.makeText(RegisterActivity.this,"Please Enter Your Password",Toast.LENGTH_SHORT).show();
                    editTextPassword.setError("Password Required");
                    editTextPassword.requestFocus();
                }
                else if(txtPassword.length()<6){
                    Toast.makeText(RegisterActivity.this,"Password should be at least 6 digits",Toast.LENGTH_SHORT).show();
                    editTextPassword.setError("Password too weak");
                    editTextPassword.requestFocus();
                }
                else if(TextUtils.isEmpty(txtCnfPassword)){
                    Toast.makeText(RegisterActivity.this,"Please Enter Your Confirm Password",Toast.LENGTH_SHORT).show();
                    editTextConfirmPassword.setError("Confirm Password Required");
                    editTextConfirmPassword.requestFocus();
                }
                else if(!txtPassword.equals(txtCnfPassword)){
                    Toast.makeText(RegisterActivity.this,"Please Enter Same Password",Toast.LENGTH_SHORT).show();
                    editTextConfirmPassword.setError("Password doesn't match");
                    editTextConfirmPassword.requestFocus();

                    //clear the entered passwords
                    editTextPassword.clearComposingText();
                    editTextConfirmPassword.clearComposingText();
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    registerUser(txtFullName, txtEmail, txtLocation, txtPhone, txtPassword);
                }
            }
        });
    }

    //Register User using the credentials given
    private void registerUser(String txtFullName, String txtEmail, String txtLocation, String txtPhone, String txtPassword) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(txtEmail, txtPassword).addOnCompleteListener(RegisterActivity
                .this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    //Email verification link
                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    //Update display name of the user
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(txtFullName).build();
                    assert firebaseUser != null;
                    firebaseUser.updateProfile(profileChangeRequest);

                    //Enter user data into firebase realtime database
                    ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(txtFullName, txtEmail, txtLocation, txtPhone);

                    //Extracting user reference from database for "Registered Users"
                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
                    referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()) {
                                //Send email verification
                                firebaseUser.sendEmailVerification();
                                Toast.makeText(RegisterActivity.this, "user registered successfully. Please check your email", Toast.LENGTH_SHORT).show();

                                //Open User profile after successful registration
                          Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);

//                                To  prevent users from returning back to Register Activity on pressing back buttonafter registration
                          intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                          startActivity(intent);
                            }
                            else{
                                Toast.makeText(RegisterActivity.this, "user registered failed. Please try again", Toast.LENGTH_SHORT).show();
                            }
                            //Hide progress bar
                            progressBar.setVisibility(View.GONE);
                        }
                    });


                }
                else{
                    try{
                        throw task.getException();
                    }
                    catch (FirebaseAuthWeakPasswordException e){
                        progressBar.setVisibility(View.GONE);
                        editTextPassword.setError("Password is too weak");
                        editTextPassword.requestFocus();
                    }
                    catch (FirebaseAuthInvalidCredentialsException e){
                        progressBar.setVisibility(View.GONE);
                        editTextEmail.setError("Email is already in use or invalid");
                        editTextEmail.requestFocus();
                    }
                    catch (FirebaseAuthUserCollisionException e){
                        progressBar.setVisibility(View.GONE);
                        editTextEmail.setError("User is already registered with this email");
                        editTextEmail.requestFocus();
                    }
                    catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

                    }
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}