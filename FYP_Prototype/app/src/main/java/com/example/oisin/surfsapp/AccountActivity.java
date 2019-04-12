/*
 -----------------------------------------------
 Oisin Redmond - C15492202 - DT228/4
 Final Year Project - SurfsApp
 -----------------------------------------------
*/

package com.example.oisin.surfsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class AccountActivity extends FragmentActivity {

    private String s_email;
    private TextView email;
    private ImageView profileImg;
    private String s_imageId;
    private final int PICK_IMAGE_REQUEST = 71;
    private DatabaseReference db;
    private FragmentManager fm = getSupportFragmentManager();
    private FragmentTransaction ft;
    private ReviewsFragment reviewsFragment;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseDatabase.getInstance().getReference().child("users")
                .child(currentUser.getUid());
        storageReference = FirebaseStorage.getInstance().getReference();

        FloatingActionButton upload = findViewById(R.id.edit_profile_pic);
        CardView logout = findViewById(R.id.logout);
        final CardView friends  = findViewById(R.id.friends_card);
        CardView reviews = findViewById(R.id.reviews_card);
        email = findViewById(R.id.email);
        profileImg = findViewById(R.id.profile_pic);

        getData(db);

        reviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putBoolean("singleUser",true);
                args.putString("friendId", null);
                args.putString("friendName","");
                reviewsFragment = new ReviewsFragment();
                reviewsFragment.setArguments(args);
                ft = fm.beginTransaction();
                ft.replace(R.id.fragmentPlaceholder, reviewsFragment);
                ft.addToBackStack("reviewsFragment");
                ft.commit();
            }
        });

        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(AccountActivity.this, FriendsActivity.class);
                startActivity(i);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                AccountActivity.this.finish();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
    }

    public void getData(DatabaseReference db){
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                s_email = dataSnapshot.child("email").getValue().toString();
                email.setText(s_email);
                if(dataSnapshot.child("profileImage").getValue() != null){
                    s_imageId = dataSnapshot.child("profileImage").getValue().toString();
                    setProfileImage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void uploadImage(){
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select a profile picture"), PICK_IMAGE_REQUEST);
    }

    public void setProfileImage(){
        storageReference.child("images/profiles/" + s_imageId).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    Glide.with(AccountActivity.this).load(uri).into(profileImg);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() !=null){
            Uri imagePath = data.getData();
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final String imgId = UUID.randomUUID().toString();
            StorageReference ref = storageReference.child("images/profiles/" + imgId);
            ref.putFile(imagePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            db.child("profileImage").setValue(imgId);
                            s_imageId = imgId;
                            Toast.makeText(AccountActivity.this, "Image uploaded.", Toast.LENGTH_SHORT).show();
                            setProfileImage();
                        }
                    })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AccountActivity.this, "Upload failed.", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}

