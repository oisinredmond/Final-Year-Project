package com.example.oisin.fyp_prototype;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ForecastConnection extends DatabaseConnection{

    public ArrayList<ForecastDay> forecastDays = new ArrayList<>();
    public DatabaseReference weatherRef;

    public ForecastConnection(DatabaseReference db, String name){
        super(db);
        weatherRef = databaseRef.child(name).child("weather");
        getDay(name,weatherRef);
    }

    public void getDay(final String name, final DatabaseReference wRef){

        wRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ForecastDay f = new ForecastDay();
                    f.setName(name);
                    f.setDate(ds.child("date").getValue().toString());
                    f.setMaxTemp(Float.parseFloat(ds.child("maxtempC").getValue().toString()));
                    f.setMinTemp(Float.parseFloat(ds.child("mintempC").getValue().toString()));

                    float[] waveHeight = new float[8];
                    float[] precipitation = new float[8];
                    float[] pressure = new float[8];
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
