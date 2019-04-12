package com.example.oisin.surfsapp;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class ReviewsFragment extends Fragment {

    private String locationName;
    private FirebaseUser currentUser;
    private Dialog reviewFormDialog;
    private FirebaseDatabase db;
    private DatabaseReference dbUser;
    private boolean singleUsersReviews;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    private ImageView upload;
    private boolean imageChosen;
    StorageReference storage;
    private RecyclerView.Adapter adapter;
    private ArrayList<Review> reviews;
    private String userId;
    private String fName;
    private FloatingActionButton addReview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_review, parent, false);
        Bundle args = getArguments();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentUser.getUid();
        singleUsersReviews = args.getBoolean("singleUser");
        addReview = view.findViewById(R.id.add_review);

        if (!singleUsersReviews) {
            locationName = args.get("locationName").toString();
            addReview.setVisibility(View.VISIBLE);
            addReview.setClickable(true);
            getReviews();
        } else if(args.getString("friendId")!=null){
            userId = args.getString("friendId");
            fName = args.getString("friendName");
            addReview.setVisibility(View.GONE);
            addReview.setClickable(false);
            getSingleUsersReviews();
        }
        else
        {
            addReview.setVisibility(View.GONE);
            addReview.setClickable(false);
            getSingleUsersReviews();
        }

        RecyclerView recyclerView = view.findViewById(R.id.review_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reviews = new ArrayList<>();
        adapter = new ReviewListAdapter(getContext(), reviews);
        recyclerView.setAdapter(adapter);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {

        db = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        TextView title = getActivity().findViewById(R.id.toolbarTitle);

        if (!singleUsersReviews) {
            imageChosen = false;
            title.setText(locationName + " - Reviews");

            addReview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (currentUser != null) {
                        reviewForm();
                    }
                }
            });
        } else if (getActivity() instanceof FriendsActivity) {
            title.setText(fName + " - Reviews");
        }
    }

    public void reviewForm() {
        reviewFormDialog = new Dialog(getActivity());
        reviewFormDialog.setContentView(R.layout.dialog_review);
        reviewFormDialog.show();

        final AppCompatRatingBar ratingBar = reviewFormDialog.findViewById(R.id.reviewRating);
        final TextView reviewSummary = reviewFormDialog.findViewById(R.id.reviewSummary);
        final TextView reviewBody = reviewFormDialog.findViewById(R.id.reviewBody);
        Button submit = reviewFormDialog.findViewById(R.id.submit);
        upload = reviewFormDialog.findViewById(R.id.image_upload);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbUser = db.getReference().child("reviews").child(locationName).child(currentUser.getUid());
                String summary = reviewSummary.getText().toString();
                String body = reviewBody.getText().toString();
                Date d = Calendar.getInstance().getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
                String date = dateFormat.format(d);
                float rating = ratingBar.getRating();
                if (!TextUtils.isEmpty(summary) || !TextUtils.isEmpty(body) || rating > 0 && imageChosen) {
                    dbUser.child("summary").setValue(summary);
                    dbUser.child("body").setValue(body);
                    dbUser.child("rating").setValue(rating);
                    dbUser.child("date").setValue(date);
                    Toast.makeText(getActivity(), "Review Submitted", Toast.LENGTH_SHORT).show();
                    reviewFormDialog.dismiss();
                    uploadImage();
                } else {
                    Toast.makeText(getActivity(), "Please fill out all fields.", Toast.LENGTH_SHORT).show();

                    if (TextUtils.isEmpty(summary)) {
                        reviewSummary.setBackgroundResource(R.drawable.edit_text_error);
                    }
                    if (TextUtils.isEmpty(body)) {
                        reviewBody.setBackgroundResource(R.drawable.edit_text_error);
                    }
                }
            }
        });
    }

    private void chooseImage() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select an image."), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            imageChosen = true;
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                upload.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final String imgId = UUID.randomUUID().toString();
            StorageReference ref = storage.child("images/reviews/" + imgId);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            dbUser.child("image").setValue(imgId);
                            db.getReference().child("locations").child(locationName).child("images").push().setValue(imgId);
                            Toast.makeText(getActivity(), "Upload Successful.", Toast.LENGTH_SHORT).show();
                            getReviews();
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

    private void getReviews() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("reviews").child(locationName);;

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reviews.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String loc = locationName;
                    String userId = ds.getKey();
                    String summary = ds.child("summary").getValue().toString();
                    String body = ds.child("body").getValue().toString();
                    String date = ds.child("date").getValue().toString();
                    String imgId = null;
                    if (ds.child("image").getValue() != null) {
                        imgId = ds.child("image").getValue().toString();
                    }
                    float rating = Float.parseFloat(ds.child("rating").getValue().toString());
                    Review r = new Review(loc, summary, body, date, imgId, userId, rating);
                    reviews.add(r);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void getSingleUsersReviews(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("reviews");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reviews.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String loc = null;
                    if(ds.hasChild(userId)){
                        loc = ds.getKey();
                        ds = ds.child(userId);
                        String summary = ds.child("summary").getValue().toString();
                        String body = ds.child("body").getValue().toString();
                        String date = ds.child("date").getValue().toString();
                        String imgId = null;
                        if(ds.child("image").getValue() !=null) {
                            imgId = ds.child("image").getValue().toString();
                        }
                        float rating = Float.parseFloat(ds.child("rating").getValue().toString());
                        Review r = new Review(loc, summary, body, date, imgId, userId, rating);
                        reviews.add(r);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
