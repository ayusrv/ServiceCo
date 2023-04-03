package com.example.serviceco;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class UploadProfilePicActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ImageView imageViewUploadPic;
    private FirebaseAuth authProfile;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private static final int PICK_IMAGE_REQUEST=1;
    private Uri uriImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_pic);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Upload Profile Pic");

        Button buttonUploadPicChoose = findViewById(R.id.upload_pic_choose_button);
        Button buttonUploadPic = findViewById(R.id.upload_pic_button);
        progressBar = findViewById(R.id.progress_bar);
        imageViewUploadPic = findViewById(R.id.imageView_profile_dp);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics");

        //URI -> Help to get data in a content provider such as image, video etc
        Uri uri = firebaseUser.getPhotoUrl();

        //Set User's current DP in ImageView(if
        //Here Picasso is an image library. It simplify the process of displaying and loading the Image
        Picasso.get().load(uri).into(imageViewUploadPic);

        //Choosing image to upload
        buttonUploadPicChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        //Upload Image
        buttonUploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                UploadPic();
            }
        });
    }

    public void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            uriImage = data.getData();
            imageViewUploadPic.setImageURI(uriImage);
        }
    }

    private void UploadPic(){
        if(uriImage!=null){
            //Save the image with uid of the current logged user
            StorageReference fileReference = storageReference.child(authProfile.getCurrentUser().getUid() + "/displaypic." + getFileExtension(uriImage));

            //Upload this picture into storage
            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri = uri;
                            firebaseUser = authProfile.getCurrentUser();

                            //Finally set the display image of the user after upload
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(downloadUri).build();

                            firebaseUser.updateProfile(profileUpdates);
                        }
                    });

                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(UploadProfilePicActivity.this, "Upload Successfull",Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(UploadProfilePicActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadProfilePicActivity.this, e.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }
        else{
            progressBar.setVisibility(View.GONE);
            Toast.makeText(UploadProfilePicActivity.this, "No file was selected",Toast.LENGTH_LONG).show();
        }
    }

    //Obtain File Extension of the image
    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();  //A content provider manages access to the central repository of the data.
        // A provider is part of an Android application, which often provides it's own ui fro working with data
        //The content Resolver method provides the basic "CRUD" (create, retrieve, update and delete) functions of persistent storage
        MimeTypeMap mime = MimeTypeMap.getSingleton();      //Indicates the nature and format of a document file. Two way map which is used to mime type to file extension
        return mime.getExtensionFromMimeType(cR.getType(uri));
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
            Intent intent = new Intent(UploadProfilePicActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
            
        }
//        else if(id==R.id.menu_update_email){
//            Intent intent = new Intent(UserProfileActivity.this, UpdateEmailActivity.class);
//            startActivity(intent);
//        }
        else if(id==R.id.menu_settings){
            Toast.makeText(UploadProfilePicActivity.this,"User Setting",Toast.LENGTH_SHORT).show();
        }
//        else if(id==R.id.menu_change_password){
//            Intent intent = new Intent(UserProfileActivity.this, ChangePasswordActivity.class);
//            startActivity(intent);
//        }
//        else if(id==R.id.menu_delete_profile){
//            Intent intent = new Intent(UserProfileActivity.this, DeleteProfileActivity.class);
//            startActivity(intent);
//        }
        else if(id==R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(UploadProfilePicActivity.this,"Logged Out",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UploadProfilePicActivity.this, MainActivity.class);

            //Clear stack to prevent user coming back to UserProfileActivity on pressing back button after logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        else{
            Toast.makeText(UploadProfilePicActivity.this,"Something went wrong!",Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}