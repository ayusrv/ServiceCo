package com.example.serviceco;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private EditText editTextPwdCurr, editTextPwdNew, editTextPwdConfirmNew;
    private TextView textViewAuthenticated;
    private Button buttonChangePwd, buttonReAuthenticate;
    private ProgressBar progressBar;
    private String userPwdCurr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        getSupportActionBar().setTitle("Change Password");
        editTextPwdNew = findViewById(R.id.edittext_change_pwd_current);
        editTextPwdCurr = findViewById(R.id.edittext_change_pwd_new);
        editTextPwdConfirmNew = findViewById(R.id.edittext_change_pwd_new_confirm);
        textViewAuthenticated = findViewById(R.id.textview_change_pwd_authenticated);
        progressBar = findViewById(R.id.progress_bar);
        buttonReAuthenticate = findViewById(R.id.button_change_pwd_authenticate);
        buttonChangePwd = findViewById(R.id.button_change_pwd);

        //Disable editText for New Password, Confirm new Password, and ake Change password button
        editTextPwdNew.setEnabled(false);
        editTextPwdConfirmNew.setEnabled(false);
        buttonChangePwd.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if(firebaseUser.equals("")){
            Toast.makeText(ChangePasswordActivity.this, "Something went wrong! User's details are not available", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChangePasswordActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            reAuthenticateUser(firebaseUser);
        }
    }

    //ReAuthenticate user before changing the password
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPwdCurr = editTextPwdCurr.getText().toString();

                if(TextUtils.isEmpty(userPwdCurr)){
                    Toast.makeText(ChangePasswordActivity.this, "Please enter the password", Toast.LENGTH_SHORT).show();
                    editTextPwdCurr.setError("Please enter the password");
                    editTextPwdCurr.requestFocus();
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);

                    //ReAuthenticate User now
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPwdCurr);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);

                                //Disable editText for current password. Enable for new Password and confirm password
                                editTextPwdCurr.setEnabled(false);
                                editTextPwdNew.setEnabled(true);
                                editTextPwdConfirmNew.setEnabled(true);

                                //Disable authenticate button. Enable change password button
                                buttonReAuthenticate.setEnabled(false);
                                buttonChangePwd.setEnabled(true);

                                //Set TextView to show user is authenticated/ verified
                                textViewAuthenticated.setText("You are authenticated/ verified"+
                                        "you can change password now!");
                                Toast.makeText(ChangePasswordActivity.this, "Password has been verified "+"Change password now", Toast.LENGTH_SHORT).show();
                                
                                buttonChangePwd.setBackgroundTintList(ContextCompat.getColorStateList(
                                        ChangePasswordActivity.this, R.color.green));

                                buttonChangePwd.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        changePwd(firebaseUser);
                                    }
                                });
                            }
                            else{
                                try{
                                    throw task.getException();
                                }
                                catch (Exception e){
                                    Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private void changePwd(FirebaseUser firebaseUser) {
        String userPwdNew = editTextPwdNew.getText().toString();
        String userPwdConfirmNew = editTextPwdConfirmNew.getText().toString();

        if(TextUtils.isEmpty(userPwdNew)){
            Toast.makeText(ChangePasswordActivity.this, "New Password is needed", Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("Please enter the new password");
            editTextPwdNew.requestFocus();
        }
        else if(TextUtils.isEmpty(userPwdConfirmNew)){
            Toast.makeText(ChangePasswordActivity.this, "Confirm Password is needed", Toast.LENGTH_SHORT).show();
            editTextPwdConfirmNew.setError("Please enter the confirm password");
            editTextPwdConfirmNew.requestFocus();
        }
        else if(!userPwdNew.matches(userPwdConfirmNew)){
            Toast.makeText(ChangePasswordActivity.this, "Confirm Password and Password are different", Toast.LENGTH_SHORT).show();
            editTextPwdConfirmNew.setError("Please re-enter the same password");
            editTextPwdConfirmNew.requestFocus();
        }
        else if(userPwdCurr.matches(userPwdNew)){
            Toast.makeText(ChangePasswordActivity.this, "New Password cannot be same as old", Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("Please re-enter the new password");
            editTextPwdNew.requestFocus();
        }
        else{
            progressBar.setVisibility(View.VISIBLE);
            firebaseUser.updatePassword(userPwdNew).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChangePasswordActivity.this, "Password has been changed", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ChangePasswordActivity.this, UserProfileActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        try {
                            throw task.getException();
                        }
                        catch (Exception e){
                            Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
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
            Intent intent = new Intent(ChangePasswordActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        }
        else if(id==R.id.menu_update_email){
            Intent intent = new Intent(ChangePasswordActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        }
        else if(id==R.id.menu_settings){
            Toast.makeText(ChangePasswordActivity.this,"User Setting",Toast.LENGTH_SHORT).show();
        }
        else if(id==R.id.menu_change_password){
            Intent intent = new Intent(ChangePasswordActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        }
        else if(id==R.id.menu_delete_profile){
            Intent intent = new Intent(ChangePasswordActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
            finish();
        }
        else if(id==R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(ChangePasswordActivity.this,"Logged Out",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChangePasswordActivity.this, MainActivity.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        else{
            Toast.makeText(ChangePasswordActivity.this,"Something went wrong!",Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}