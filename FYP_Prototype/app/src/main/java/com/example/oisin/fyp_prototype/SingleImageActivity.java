package com.example.oisin.fyp_prototype;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

public class SingleImage extends AppCompatActivity {
    private String loc;
    private String id;
    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_image);
        img = findViewById(R.id.single_image);
        id = getIntent().getStringExtra("imageUrl");
        loc = getIntent().getStringExtra("locationName");

        FirebaseStorage.getInstance().getReference().child("images/reviews/"+id)
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(SingleImage.this).load(uri).into(img);
            }
        });
    }
}
