package com.example.oisin.fyp_prototype;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class LocationFragmentMenu extends Fragment{

    Button forecast;
    Intent i;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        return inflater.inflate(R.layout.location_fragment,parent,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        forecast = view.findViewById(R.id.getForecast);

        forecast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = getArguments();
                String name = args.get("markerName").toString();
                i = new Intent(getActivity(),ViewForecastActivity.class);
                i.putExtra("markerName",name);
                startActivity(i);
            }
        });
    }
}
