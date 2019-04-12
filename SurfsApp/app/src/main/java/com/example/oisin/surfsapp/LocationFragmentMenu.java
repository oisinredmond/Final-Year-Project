/*
 -----------------------------------------------
 Oisin Redmond - C15492202 - DT228/4
 Final Year Project - SurfsApp
 -----------------------------------------------
*/


package com.example.oisin.surfsapp;

import android.content.Intent;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class LocationFragmentMenu extends Fragment{

    LatLng currLoc;
    LatLng dest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_location,parent,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        CardView forecast = view.findViewById(R.id.forecastButton);
        CardView directions = view.findViewById(R.id.directionsButton);
        TextView title = view.findViewById(R.id.location_title);
        TextView distance = view.findViewById(R.id.distance);

        Bundle args = getArguments();
        final String name = args.get("markerName").toString();
        title.setText(name);

        currLoc = new LatLng((double)args.get("lat"),(double)args.get("lon"));
        dest = null;

        for(LocationObject l : MapsActivity.locations){
            if(name.equals(l.name)){
                dest = new LatLng(l.latitude, l.longitude);
            }
        }

        float res[] = new float[1];

        if(dest != null) {
            Location.distanceBetween(currLoc.latitude, currLoc.longitude, dest.latitude, dest.longitude, res);
        }

        double distanceKm = Math.round((res[0]/1000)*10)/10.0;
        distance.setText(Double.toString(distanceKm) + " km");

        forecast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(),ViewForecastActivity.class);
                i.putExtra("markerName",name);
                startActivity(i);
            }
        });

        directions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MapsActivity)getContext()).initNavigation(currLoc, dest);
            }
        });
    }
}
