/*
 -----------------------------------------------
 Oisin Redmond - C15492202 - DT228/4
 Final Year Project Interim Prototype - SurfsApp
 -----------------------------------------------

 This class is used as a plain old java object to represent
 information for a particular location: name and coordinates.*/

package com.example.oisin.fyp_prototype;

public class LocationObject {
    public String name;
    public double latitude;
    public double longitude;

    public LocationObject(String n, double lat, double lon){
        name = n;
        latitude = lat;
        longitude = lon;
    }
}
