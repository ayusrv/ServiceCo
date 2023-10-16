package com.example.serviceco;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextLoginEmail, editTextLoginPassword;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private static final String TAG = "LoginActivity";
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.green)));
        editTextLoginEmail = findViewById(R.id.edittext_login_email);
        editTextLoginPassword = findViewById(R.id.edittext_login_password);
        progressBar = findViewById(R.id.progress_bar);

        authProfile = FirebaseAuth.getInstance();

        Button buttonForgotPassword = findViewById(R.id.button_forgot_password);
        buttonForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this,"You can change your password now",Toast.LENGTH_LONG).show();
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });

        //Show hide password using eye icon
//        ImageView imageViewShowHidePass = findViewById(R.id.imageview_show_hide_pwd);
//        imageViewShowHidePass.setImageResource(R.drawable.ic_hide_pwd);
//        imageViewShowHidePass.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(editTextLoginPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
//                    //If password is visible then hide it
//                    editTextLoginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
//                    //change icon
//                    imageViewShowHidePass.setImageResource(R.drawable.ic_hide_pwd);
//                }else{
//                    editTextLoginPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
//                    imageViewShowHidePass.setImageResource(R.drawable.ic_show_pwd);
//                }
//            }
//        });

        //Login Button
        Button buttonLogin = findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textEmail = editTextLoginEmail.getText().toString();
                String textPass = editTextLoginPassword.getText().toString();

                if(TextUtils.isEmpty(textEmail)){
                    Toast.makeText(LoginActivity.this, "Please enter your Email",Toast.LENGTH_SHORT).show();
                    editTextLoginEmail.setError("Email is required");
                    editTextLoginEmail.requestFocus();
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()){
                    Toast.makeText(LoginActivity.this, "Please re-enter your email",Toast.LENGTH_SHORT).show();
                    editTextLoginEmail.setError("Valid Email is required");
                    editTextLoginEmail.requestFocus();
                }
                else if(TextUtils.isEmpty(textPass)){
                    Toast.makeText(LoginActivity.this, "Please enter your Password",Toast.LENGTH_SHORT).show();
                    editTextLoginPassword.setError("Password is required");
                    editTextLoginPassword.requestFocus();
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    loginUser(textEmail,textPass);
                }
            }
        });
    }

    private void loginUser(String email, String pass) {
        authProfile.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    firebaseUser = authProfile.getCurrentUser();
                    if(firebaseUser.isEmailVerified()){
                        Toast.makeText(LoginActivity.this, "You logged in",Toast.LENGTH_SHORT).show();

                        //Start the UserProfileActivity
                        startActivity(new Intent(LoginActivity.this, Home.class));
                        finish();
                    }
                    else{
                        firebaseUser.sendEmailVerification();
                        authProfile.signOut();
                        showAlertDialog();
                    }
                }
                else{
                    try {
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        editTextLoginEmail.setError("User does not exist");
                        editTextLoginEmail.requestFocus();
                    }
                    catch (FirebaseAuthInvalidCredentialsException e){
                        editTextLoginEmail.setError("Invalid Credentials");
                        editTextLoginEmail.requestFocus();
                    }catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(LoginActivity.this, e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showAlertDialog() {

        //Setup alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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
    
    //Check if user is already logged in or not

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = authProfile.getCurrentUser();
        if(authProfile.getCurrentUser()!=null && firebaseUser.isEmailVerified()){
            Toast.makeText(LoginActivity.this, "Already Logged in", Toast.LENGTH_SHORT).show();
            //Start the UserProfileActivity
            startActivity(new Intent(LoginActivity.this, Home.class));
        }
        else{
            Toast.makeText(LoginActivity.this, "You can now login", Toast.LENGTH_SHORT).show();
        }
    }
}