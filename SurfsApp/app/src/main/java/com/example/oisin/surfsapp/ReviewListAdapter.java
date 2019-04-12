package com.example.oisin.surfsapp;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.ReviewHolder>{

    private ArrayList<Review> reviews;
    private Context ctx;

    public class ReviewHolder extends RecyclerView.ViewHolder {

        private TextView locationName,summary, body, date;
        private AppCompatRatingBar rating;
        private ImageView img, userImg;
        private CardView userImgCard;

        public ReviewHolder(View itemView) {
            super(itemView);
            locationName = itemView.findViewById(R.id.title);
            summary = itemView.findViewById(R.id.summary);
            body = itemView.findViewById(R.id.body);
            date = itemView.findViewById(R.id.date);
            rating = itemView.findViewById(R.id.rating);
            img = itemView.findViewById(R.id.review_photo);
            userImg = itemView.findViewById(R.id.profile_img);
            userImgCard = itemView.findViewById(R.id.profile_img_card);
        }

        public void setItems(Review review) {
            userImgCard.setVisibility(View.GONE);
            if(review.getLocationName() == null)
            {
                userImgCard.setVisibility(View.VISIBLE);
                FirebaseDatabase.getInstance().getReference("users").child(review.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        locationName.setText(dataSnapshot.child("name").getValue().toString());
                        String profileImg = dataSnapshot.child("profileImage").getValue().toString();
                        FirebaseStorage.getInstance().getReference().child("images/profiles/" + profileImg).getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        System.out.println(uri);
                                        Glide.with(ctx).load(uri).into(userImg);
                                    }
                                });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            else{
                locationName.setText(review.getLocationName());
            }
            summary.setText(review.getSummary());
            body.setText(review.getBody());
            date.setText(review.getDate());
            rating.setNumStars((int) Math.ceil(review.getRating()));
            rating.setRating(review.getRating());
            String imgId = review.getImgId();
            if(imgId != null) {
                FirebaseStorage.getInstance().getReference().child("images/reviews/" + imgId).getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                System.out.println(uri);
                                Glide.with(ctx).load(uri).into(img);
                            }
                        });
            }
        }
    }

    public ReviewListAdapter(Context ctx, ArrayList<Review> r){
        this.reviews = r;
        this.ctx = ctx;
    }

    @Override
    public ReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.recycler_review_row, parent, false);
        return new ReviewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewHolder holder, int pos){
        Review r = reviews.get(pos);
        holder.setItems(r);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }
}
