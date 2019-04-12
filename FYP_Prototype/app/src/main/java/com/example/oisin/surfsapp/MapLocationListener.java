/*
 -----------------------------------------------
 Oisin Redmond - C15492202 - DT228/4
 Final Year Project - SurfsApp
 -----------------------------------------------
*/

package com.example.oisin.surfsapp;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

public class MapLocationListener implements LocationListener{

    public Location lastLocation;
    public LatLng userLatLng;
    public Context ctx;

    public MapLocationListener(Context ctx){
        this.ctx = ctx;
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        userLatLng = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
        MapsActivity.userLatLng = userLatLng;
        MapsActivity.lastLocation = lastLocation;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) { }

    @Override
    public void onProviderEnabled(String s) { }

    @Override
    public void onProviderDisabled(String s) { }
}
