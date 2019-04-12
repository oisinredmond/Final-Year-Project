package com.example.oisin.surfsapp;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Route {

    private JSONObject directions;
    private JSONObject route;
    private ArrayList<RouteStep> steps;
    private String routeSummary;
    private String encodedOverviewPolyLine;
    private String durationString;
    private String distanceString;
    private String startAddress;
    private String endAddress;
    private int duration;
    private int distance;
    Context ctx;

    public ArrayList<RouteStep> getSteps() {
        return steps;
    }

    public List<LatLng> getOverViewPolyLine() {
        List<LatLng> p = PolyUtil.decode(encodedOverviewPolyLine);
        return p;
    }

    public String getDurationString() {
        return durationString;
    }

    public String getDistanceString() {
        return distanceString;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public int getDuration() {
        return duration;
    }

    public int getDistance() {
        return distance;
    }

    public Route(Context ctx){
        this.ctx = ctx;
        directions = new JSONObject();
        route = new JSONObject();
        steps = new ArrayList<>();
    }

    public void parseJson(){
        try {
            route = directions.getJSONArray("routes").getJSONObject(0);
            routeSummary = route.getString("summary");
            encodedOverviewPolyLine = route.getJSONObject("overview_polyline").getString("points");

            JSONObject leg = route.getJSONArray("legs").getJSONObject(0);
            JSONArray stepsArray = leg.getJSONArray("steps");

            duration = leg.getJSONObject("duration").getInt("value");
            durationString = leg.getJSONObject("duration").getString("text");
            distance = leg.getJSONObject("distance").getInt("value");
            distanceString = leg.getJSONObject("distance").getString("text");
            startAddress = leg.getString("start_address");
            endAddress = leg.getString("end_address");

            for(int i=0; i<stepsArray.length(); i++){
                JSONObject step = stepsArray.getJSONObject(i);
                String htmlInstructions = step.getString("html_instructions");
                String distance = step.getJSONObject("distance").getString("text");
                String duration = step.getJSONObject("duration").getString("text");
                String encodedPolyLine = step.getJSONObject("polyline").getString("points");
                String maneuver = null;

                if(step.has("maneuver")) {
                    maneuver = step.getString("maneuver");
                }

                LatLng start = new LatLng(step.getJSONObject("start_location").getDouble("lat"),
                        step.getJSONObject("start_location").getDouble("lng"));
                LatLng end = new LatLng(step.getJSONObject("end_location").getDouble("lat"),
                        step.getJSONObject("end_location").getDouble("lng"));
                RouteStep rs = new RouteStep(htmlInstructions, distance, duration, maneuver, encodedPolyLine, start, end);
                steps.add(rs);
            }

            ((MapsActivity)ctx).setNavigationMenu(this);
        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void getDirections(LatLng start, LatLng end){
        DecimalFormat df = new DecimalFormat("#.######");

        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");
        urlString.append(df.format(start.latitude));
        urlString.append(",");
        urlString.append(df.format(start.longitude));
        urlString.append("&destination=");
        urlString.append(df.format(end.latitude));
        urlString.append(",");
        urlString.append(df.format(end.longitude));
        urlString.append("&region=ie");
        urlString.append("&key=AIzaSyActfixU-EN0yyTTZTHs1IE6LVG652OrUQ");

        RequestQueue requestQueue = Volley.newRequestQueue(ctx);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                urlString.toString(),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        directions = response;
                        parseJson();
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        error.printStackTrace();
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }
}
