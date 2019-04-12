package com.example.oisin.surfsapp;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class NavigationLocationListener extends MapLocationListener{

    private ArrayList<RouteStep> steps;
    private int i;
    private double distance;
    private boolean replacedFragment;
    private boolean isTurn;
    private boolean passed;
    private RouteStep currentStep;
    private String distanceText;

    public NavigationLocationListener(Context ctx, Route r){
        super(ctx);
        replacedFragment =  false;
        isTurn = false;
        steps = r.getSteps();
        i = 0;
    }

    @Override
    public void onLocationChanged(Location location) {

        super.onLocationChanged(location);
        float bearing = lastLocation.getBearing();
        MapsActivity.map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
                .target(userLatLng).zoom(18).bearing(bearing).tilt(50).build()));

        currentStep = steps.get(i);
        distance = checkDistance(currentStep.getEnd());

        distanceText = Integer.toString((int)distance) + " m";
        if(distance >= 1000){
            double distanceKm = Math.round((distance/1000)*10)/10.0;
            distanceText = Float.toString((float)distanceKm) + " km";
        }

        ((MapsActivity)ctx).setCurrentDistance(distanceText);

        if(currentStep.getManeuver() != null){
            if(!currentStep.getManeuver().equals("straight")){
                isTurn = true;
            }else{
                isTurn = false;
            }
        }

        if(isTurn && checkDistance(currentStep.getStart()) < 10){
            passed = true;
        }

        if(checkDistance(currentStep.getEnd()) <= 70 && !replacedFragment){
            i++;
            replacedFragment = true;
            currentStep = steps.get(i);
            ((MapsActivity)ctx).replaceDirectionFragment(currentStep.getHtmlInstructions(),currentStep.getDuration(),
                    currentStep.getDistance(),currentStep.getManeuver());
        }

        if(i >0) {
            if (replacedFragment && checkDistance(steps.get(i - 1).getEnd())>15){
                replacedFragment = false;
            }
        }

        if(passed && !currentStep.getHtmlInstructions().contains("Continue")){
            distance =  checkDistance(currentStep.getStart());
            if(distance > 15){
                i++;
                currentStep = steps.get(i);
                ((MapsActivity)ctx).replaceDirectionFragment(currentStep.getHtmlInstructions(),currentStep.getDuration(),
                        currentStep.getDistance(),currentStep.getManeuver());
                replacedFragment = false;
                passed = false;
            }
        }
        else if(passed && currentStep.getHtmlInstructions().contains("Continue")){
            distance =  checkDistance(currentStep.getStart());
            if(distance > 15) {
                ((MapsActivity) ctx).replaceDirectionFragment(currentStep.getHtmlInstructions(), currentStep.getDuration(),
                        currentStep.getDistance(), "straight");
                replacedFragment = false;
                passed = false;
            }
        }
    }

    public float checkDistance(LatLng target) {
        float[] distance = new float[1];
        Location.distanceBetween(userLatLng.latitude, userLatLng.longitude, target.latitude, target.longitude, distance);
        return distance[0];
    }
}
