package com.example.oisin.surfsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class GalleryFragment extends Fragment {

    private String locationName;
    private FloatingActionButton addImage;
    private FirebaseUser currentUser;
    private ArrayList<String> imageIds;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private Uri imagePath;
    private int PICK_IMAGE_REQUEST = 71;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View view =  inflater.inflate(R.layout.fragment_gallery, parent, false);
        Bundle args = getArguments();
        locationName = args.get("locationName").toString();
        getImageUrls();
        recyclerView = view.findViewById(R.id.gallery_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),3));
        imageIds = new ArrayList<>();
        adapter = new GalleryAdapter(getContext(), imageIds, locationName);
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        addImage = view.findViewById(R.id.add_image);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        TextView title = getActivity().findViewById(R.id.toolbarTitle);
        title.setText(locationName + " - Gallery");

        if(currentUser != null){
            addImage.setVisibility(View.VISIBLE);
        }

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
        getImageUrls();
    }

    public void getImageUrls(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("locations").child(locationName).child("images");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                imageIds.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String imageId = ds.getValue().toString();
                    imageIds.add(imageId);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError e){

            }
        });
    }

    public void uploadImage(){
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select a profile picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() !=null){
            imagePath = data.getData();
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            final String imgId = UUID.randomUUID().toString();
            StorageReference ref = FirebaseStorage.getInstance().getReference().child("images/reviews/" + imgId);
            ref.putFile(imagePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            FirebaseDatabase.getInstance().getReference().child("locations").child(locationName)
                                    .child("images").push().setValue(imgId);
                            Toast.makeText(getContext(), "Image uploaded.", Toast.LENGTH_SHORT).show();
                            getImageUrls();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Upload failed.", Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }
}
