package com.example.oisin.surfsapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class NavigationDirectionFragment extends Fragment {

    TextView directionsText;
    TextView distanceText;
    TextView durationText;
    ImageView directionIcon;
    Bundle args;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nav_direction, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        args = getArguments();
        directionsText = view.findViewById(R.id.directions_text);
        distanceText = view.findViewById(R.id.distance_text);
        durationText = view.findViewById(R.id.duration_text);
        directionIcon = view.findViewById(R.id.direction_icon);

        directionsText.setText(Html.fromHtml(args.getString("directionsHtml"),Html.FROM_HTML_MODE_COMPACT));
        distanceText.setText(args.getString("distanceText"));
        durationText.setText(args.getString("durationText"));

        String maneuver = args.getString("maneuver");

        if(maneuver != null) {
            if (args.getString("maneuver").equals("turn-right")) {
                directionIcon.setImageResource(R.drawable.ic_arrow_right);
            } else if (args.getString("maneuver").equals("turn-left")) {
                directionIcon.setImageResource(R.drawable.ic_arrow_left);
            } else if (args.getString("maneuver").equals("straight")) {
                directionIcon.setImageResource(R.drawable.ic_arrow_up);
            }
        }
        else{
            directionIcon.setImageResource(R.drawable.ic_arrow_up);
        }
    }
}
