package com.example.oisin.surfsapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NavigationMenuFragment extends Fragment {

    RelativeLayout addrContainer;
    CardView startNavButton;
    TextView startAddressLine1;
    TextView startAddressLine2;
    TextView endAddressLine1;
    TextView endAddressLine2;
    TextView time;
    TextView dist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_nav_menu,parent,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        Bundle args = getArguments();

        addrContainer = view.findViewById(R.id.address_container);
        startNavButton = view.findViewById(R.id.startNavButton);
        startAddressLine1 = view.findViewById(R.id.start_address_line1);
        startAddressLine2 = view.findViewById(R.id.start_address_line2);
        endAddressLine1 = view.findViewById(R.id.end_address_line1);
        endAddressLine2 = view.findViewById(R.id.end_address_line2);
        time = view.findViewById(R.id.time_text);
        dist = view.findViewById(R.id.distance_text);

        String startAddr[] = args.getStringArray("startAddr");
        String endAddr[] = args.getStringArray("endAddr");

        startAddressLine1.setText(startAddr[0].trim());
        startAddressLine2.setText(startAddr[1].trim());
        endAddressLine1.setText(endAddr[0].trim());
        endAddressLine2.setText(endAddr[1].trim());
        time.setText(args.getString("durationTxt"));
        dist.setText(args.getString("distanceTxt"));

        startNavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MapsActivity)getContext()).startNavigation();
            }
        });
    }
}
