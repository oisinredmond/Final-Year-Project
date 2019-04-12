package com.example.oisin.fyp_prototype;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FriendsFragment extends Fragment {

    FirebaseUser currentUser;
    Button addFriend;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View view =  inflater.inflate(R.layout.friends_fragment, parent, false);
        Bundle args = getArguments();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        addFriend = view.findViewById(R.id.add_friend);
        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
