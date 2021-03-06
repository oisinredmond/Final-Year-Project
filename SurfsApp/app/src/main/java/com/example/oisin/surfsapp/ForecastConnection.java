/*
 -----------------------------------------------
 Oisin Redmond - C15492202 - DT228/4
 Final Year Project - SurfsApp
 -----------------------------------------------
*/


package com.example.oisin.surfsapp;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ForecastConnection extends LocationsConnection{

    public ArrayList<ForecastDay> forecastDays = new ArrayList<>(); // Used to store days of forecast for each location
    public DatabaseReference weatherRef;

    public ForecastConnection(DatabaseReference db, String name){
        super(db);
        weatherRef = databaseRef.child(name).child("weather");
        getDay(name,weatherRef);
    }

    // Uses a datasnapshot of forecast data to extract information and move it into a ForecastDay object
    public void getDay(final String name, final DatabaseReference wRef){

        wRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Loops through the days for a locations weather data
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ForecastDay f = new ForecastDay();
                    f.setName(name);
                    f.setDate(ds.child("date").getValue().toString());
                    f.setMaxTemp(Float.parseFloat(ds.child("maxtempC").getValue().toString()));
                    f.setMinTemp(Float.parseFloat(ds.child("mintempC").getValue().toString()));

                    float[] waveHeight = new float[8];
                    float[] precipitation = new float[8];
                    float[] pressure = new float[8];
                    float[] cloudCover = new float[8];
                    float[] humidity = new float[8];
                    String[] swellDir = new String[8];
                    float[] swellDirDeg = new float[8];
                    float[] swellHeight = new float[8];
                    float[] swellPeriod = new float[8];
                    float[] feelsLikeTemp = new float[8];
                    float[] temp = new float[8];
                    float[] waterTemp = new float[8];
                    float[] windSpeed = new float[8];
                    float[] windDirDeg = new float[8];
                    String[] windDir = new String[8];
                    int i = 0;

                    // Loops through each hour in a day of forecast data
                    for(DataSnapshot ds2 : ds.child("hourly").getChildren()) {
                        feelsLikeTemp[i] = (Float.parseFloat(ds2.child("FeelsLikeC").getValue().toString()));
                        waveHeight[i] = (Float.parseFloat(ds2.child("sigHeight_m").getValue().toString()));
                        pressure[i] = (Float.parseFloat(ds2.child("pressure").getValue().toString()));
                        precipitation[i] = (Float.parseFloat(ds2.child("precipMM").getValue().toString()));
                        swellDir[i] = (ds2.child("swellDir16Point").getValue().toString());
                        swellDirDeg[i] = (Float.parseFloat(ds2.child("swellDir").getValue().toString()));
                        swellHeight[i] = (Float.parseFloat(ds2.child("swellHeight_m").getValue().toString()));
                        swellPeriod[i] = (Float.parseFloat(ds2.child("swellPeriod_secs").getValue().toString()));
                        temp[i] = (Float.parseFloat(ds2.child("tempC").getValue().toString()));
                        waterTemp[i] = (Float.parseFloat(ds2.child("waterTemp_C").getValue().toString()));
                        windSpeed[i] = (Float.parseFloat(ds2.child("windspeedKmph").getValue().toString()));
                        windDirDeg[i] = (Float.parseFloat(ds2.child("winddirDegree").getValue().toString()));
                        windDir[i] = (ds2.child("winddir16Point").getValue().toString());
                        cloudCover[i] = (Float.parseFloat(ds2.child("cloudcover").getValue().toString()));
                        humidity[i] = (Float.parseFloat(ds2.child("humidity").getValue().toString()));
                        i++;
                    }
                    f.setWaveHeight(waveHeight);
                    f.setPrecipitation(precipitation);
                    f.setPressure(pressure);
                    f.setSwellDir(swellDir);
                    f.setSwellDirDeg(swellDirDeg);
                    f.setSwellHeight(swellHeight);
                    f.setSwellPeriod(swellPeriod);
                    f.setFeelsLikeTemp(feelsLikeTemp);
                    f.setTemp(temp);
                    f.setWaterTemp(waterTemp);
                    f.setWindSpeed(windSpeed);
                    f.setWindDir(windDir);
                    f.setWindDirDeg(windDirDeg);
                    f.setCloudCover(cloudCover);
                    forecastDays.add(f);
                }
                ViewForecastActivity.getForecast();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
