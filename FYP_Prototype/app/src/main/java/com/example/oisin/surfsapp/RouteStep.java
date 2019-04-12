package com.example.oisin.surfsapp;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.List;

public class RouteStep {

    private String htmlInstructions;
    private String distance;
    private String duration;
    private String encodedPolyLine;
    private String maneuver;
    private LatLng start;
    private LatLng end;

    public RouteStep(String html, String dist, String dur, String man, String poly, LatLng start, LatLng end){
        this.htmlInstructions = html;
        this.distance = dist;
        this.duration = dur;
        this.maneuver = man;
        this.encodedPolyLine = poly;
        this.start = start;
        this.end = end;
    }

    public String getHtmlInstructions() {
        return htmlInstructions;
    }

    public String getDistance() {
        return distance;
    }

    public String getDuration() {
        return duration;
    }

    public List<LatLng> getPolyLine() {
        List<LatLng> p = PolyUtil.decode(encodedPolyLine);
        return p;
    }

    public String getManeuver() {
        return maneuver;
    }

    public LatLng getStart() {
        return start;
    }

    public LatLng getEnd() {
        return end;
    }
}
