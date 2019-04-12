package com.example.oisin.surfsapp;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.util.ArrayList;

public class FriendsActivity extends FragmentActivity {

    DatabaseReference ref;
    FirebaseUser currentUser;
    FirebaseStorage storage;
    FloatingActionButton addFriend;
    Dialog addFriendDialog;
    Dialog friendOptions;
    ArrayList<Friend> friends;
    ArrayList<String> friendIds;
    RecyclerView recyclerView;
    FriendListAdapter adapter;
    FragmentManager fm =  getSupportFragmentManager();
    FragmentTransaction ft;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        ref = FirebaseDatabase.getInstance().getReference().child("users");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
        recyclerView = findViewById(R.id.friend_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendIds = new ArrayList<>();
        friends = new ArrayList<>();
        adapter = new FriendListAdapter(this, friends);
        recyclerView.setAdapter(adapter);

        getFriendIds();
        addFriend = findViewById(R.id.add_friend);
        addFriendDialog = new Dialog(this);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend();
            }
        });
    }

    public void addFriend(){
        addFriendDialog.setContentView(R.layout.dialog_add_friend);
        addFriendDialog.show();
        ImageView submit = addFriendDialog.findViewById(R.id.submit);
        final TextView email = addFriendDialog.findViewById(R.id.email);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String s_email = email.getText().toString();
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            if(ds.child("email").getValue().equals(s_email)){
                                ref.child(currentUser.getUid()).child("friends").push()
                                        .setValue(ds.getKey()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getApplicationContext(),s_email + " added.",Toast.LENGTH_SHORT).show();
                                        addFriendDialog.dismiss();
                                        getFriendIds();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(),"User not found.", Toast.LENGTH_SHORT).show();
                                        addFriendDialog.dismiss();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
        });
    }

    public void getFriendIds(){
        friendIds.clear();
        ref.child(currentUser.getUid()).child("friends")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String id = ds.getValue().toString();
                    friendIds.add(id);
                }
                getFriends();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getFriends(){
        friends.clear();
        for(String id : friendIds){
            ref.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String id = dataSnapshot.getKey();
                    String name = dataSnapshot.child("name").getValue().toString();
                    String email = dataSnapshot.child("email").getValue().toString();
                    String imgId = dataSnapshot.child("profileImage").getValue().toString();
                    System.out.println(name + email + imgId);
                    Friend f = new Friend(id, name, email, imgId);
                    friends.add(f);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void showFriend(Friend f){
        friendOptions = new Dialog(this);
        friendOptions.setContentView(R.layout.dialog_friend_options);

        final String id = f.getId();
        final String friendName = f.getName();
        TextView name = friendOptions.findViewById(R.id.friend_name);
        TextView email = friendOptions.findViewById(R.id.friend_email);
        CardView friendReviews = friendOptions.findViewById(R.id.reviews_card);
        CardView friendRemove = friendOptions.findViewById(R.id.remove_friend_card);
        final ImageView pic = friendOptions.findViewById(R.id.profile_pic);

        name.setText(friendName);
        email.setText(f.getEmail());
        storage.getReference().child("images/profiles/" + f.getImageId()).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(FriendsActivity.this).load(uri).into(pic);
                    }
                });

        friendOptions.show();

        friendReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReviewsFragment reviewsFragment = new ReviewsFragment();
                Bundle args = new Bundle();
                args.putBoolean("singleUser",true);
                args.putString("friendId",id);
                args.putString("friendName", friendName);
                reviewsFragment.setArguments(args);
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_placeholder, reviewsFragment);
                ft.addToBackStack("reviewsFragment");
                friendOptions.dismiss();
                ft.commit();
            }
        });

        friendRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ref.child(currentUser.getUid()).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            if(ds.getValue().toString().equals(id)){
                                System.out.println(ds.getKey());
                                ref.child(currentUser.getUid()).child("friends").child(ds.getKey())
                                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(FriendsActivity.this,"User was removed from friends list",
                                                Toast.LENGTH_SHORT).show();
                                        friendOptions.dismiss();
                                        getFriendIds();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed(){
        finish();
    }
}
