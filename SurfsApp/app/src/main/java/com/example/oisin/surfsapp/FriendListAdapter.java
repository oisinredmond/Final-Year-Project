/*
 -----------------------------------------------
 Oisin Redmond - C15492202 - DT228/4
 Final Year Project - SurfsApp
 -----------------------------------------------
*/


package com.example.oisin.surfsapp;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendHolder>{

    private ArrayList<Friend> friends;
    private Context ctx;

    public class FriendHolder extends RecyclerView.ViewHolder {

        private RelativeLayout rowLayout;
        private TextView name,email;
        private ImageView img;

        public FriendHolder(View itemView) {
            super(itemView);
            rowLayout = itemView.findViewById(R.id.friend_row_layout);
            name = itemView.findViewById(R.id.friend_name);
            email = itemView.findViewById(R.id.friend_email);
            img = itemView.findViewById(R.id.friend_profile_image);
        }

        public void setItems(final Friend friend) {
            name.setText(friend.getName());
            email.setText(friend.getEmail());
            String imgId = friend.getImageId();
            if(imgId != null) {
                FirebaseStorage.getInstance().getReference().child("images/profiles/" + imgId).getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(ctx).load(uri).into(img);
                            }
                        });
            }
            else{
                img.setImageResource(R.drawable.ic_account_icon);
            }

            rowLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((FriendsActivity)ctx).showFriend(friend);
                }
            });
        }
    }

    public FriendListAdapter(Context ctx, ArrayList<Friend> f){
        this.friends = f;
        this.ctx = ctx;
    }

    @Override
    public FriendHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.recycler_friend_row, parent, false);
        return new FriendHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendHolder holder, int pos){
        Friend f = friends.get(pos);
        holder.setItems(f);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }
}
