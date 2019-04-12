package com.example.oisin.surfsapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private ArrayList<String> imageUrls;
    private Context ctx;
    private String locationName;

    public class GalleryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView mImageView;
        private String url;

        private GalleryViewHolder(View itemView){
            super(itemView);
            mImageView = itemView.findViewById(R.id.gallery_image);
            mImageView.setOnClickListener(this);
        }

        private void setItem(String u){
            url = u;
            FirebaseStorage.getInstance().getReference().child("images/reviews/"+url).
                    getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(ctx).load(uri).into(mImageView);
                }
            });
        }

        @Override
        public void onClick(View view) {
            Intent i = new Intent(ctx, SingleImageActivity.class);
            i.putExtra("imageUrl",url);
            i.putExtra("locationName", locationName);
            view.getContext().startActivity(i);
        }
    }


    public GalleryAdapter(Context c, ArrayList<String> i, String loc){
        this.ctx = c;
        this.imageUrls = i;
        this.locationName = loc;
    }

    @Override
    public GalleryAdapter.GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(ctx).inflate(R.layout.recycler_gallery_image, parent, false);
        return new GalleryAdapter.GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GalleryAdapter.GalleryViewHolder holder, int position){
        String imageUrl = imageUrls.get(position);
        holder.setItem(imageUrl);
    }

    @Override
    public int getItemCount(){
        return imageUrls.size();
    }
}
