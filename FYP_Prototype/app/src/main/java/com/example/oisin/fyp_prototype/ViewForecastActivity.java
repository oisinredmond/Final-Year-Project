/*
 -----------------------------------------------
 Oisin Redmond - C15492202 - DT228/4
 Final Year Project Interim Prototype - SurfsApp
 -----------------------------------------------

 This class is used to display forecast data of the chosen location.*/

package com.example.oisin.fyp_prototype;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ViewForecastActivity extends AppCompatActivity {

    static ArrayList<ForecastDay> forecastDays = new ArrayList<>();
    static ForecastConnection fc;
    DatabaseReference db;
    static String[] dates =  {"1","2","3","4","5","6","7"};
    static float[] times = {0,300,600,900,1200,1500,1800,2100};

    float[] waveHeights = new float[8], swellHeights = new float[8], windSpeeds = new float[8], temps = new float[8], windDirsDeg= new float[8], precipitation = new float[8], swellDirsDeg = new float[8];
    String[] windDirs = new String[8], swellDirs =  new String[8];

    LineChart waveChart;
    LineChart swellChart;
    static Spinner dateMenu;
    static ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        FirebaseApp.initializeApp(this);
        db = FirebaseDatabase.getInstance().getReference().child("locations");
        String name = getIntent().getStringExtra("markerName");
        fc = new ForecastConnection(db, name);
        dateMenu = findViewById(R.id.dateSpinner);
        waveChart = findViewById(R.id.waveChart);
        swellChart = findViewById(R.id.swellChart);
        TextView forecastName = findViewById(R.id.forecastName);
        forecastName.setText(name);
        pb = findViewById(R.id.progressBar);
        pb.setVisibility(View.VISIBLE);

        dateMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                setViews(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    // Gets each forecast day for the location and sets the dates for the dropdown menu
    public static void getForecast() {
        forecastDays = fc.forecastDays;
        for(int i =0; i< dates.length; i++) {
            dates[i] = forecastDays.get(i).getDate();
        }
        setAdapter();
        pb.setVisibility(View.INVISIBLE);
    }

    // Creates a dropdown menu for the user to choose a day in the week
    public static void setAdapter(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MyApplication.getAppContext(), android.R.layout.simple_spinner_item, dates);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        dateMenu.setAdapter(adapter);
    }

    // Sets the text views and graphs using forecast data
    public void setViews(int pos){
        ForecastDay currentDay = forecastDays.get(pos);
        List<Entry> waveEntries = new ArrayList<>();
        List<Entry> swellEntries = new ArrayList<>();

        TextView windSpeed1 = findViewById(R.id.windSpeed1);
        TextView windSpeed2 = findViewById(R.id.windSpeed2);
        TextView windSpeed3 = findViewById(R.id.windSpeed3);
        ImageView windDir1 = findViewById(R.id.windDir1);
        ImageView windDir2 = findViewById(R.id.windDir2);
        ImageView windDir3 = findViewById(R.id.windDir3);

        windDir1.setRotation(currentDay.getWindDirDeg()[2]);
        windDir2.setRotation(currentDay.getWindDirDeg()[4]);
        windDir3.setRotation(currentDay.getWindDirDeg()[6]);

        String windSpd1 = currentDay.getWindSpeed()[2] + " kmph";
        String windSpd2 = currentDay.getWindSpeed()[4] + " kmph";
        String windSpd3 = currentDay.getWindSpeed()[6] + " kmph";
        windSpeed1.setText(windSpd1);
        windSpeed2.setText(windSpd2);
        windSpeed3.setText(windSpd3);

        float maxTemp = currentDay.getMaxTemp();
        float minTemp = currentDay.getMinTemp();

        waveHeights = currentDay.getWaveHeight();
        swellHeights = currentDay.getSwellHeight();
        windSpeeds = currentDay.getWindSpeed();
        temps = currentDay.getTemp();
        windDirs = currentDay.getWindDir();
        windDirsDeg = currentDay.getWindDirDeg();
        precipitation = currentDay.getPrecipitation();
        swellDirsDeg = currentDay.getSwellDirDeg();
        swellDirs = currentDay.getSwellDir();

        for(int i = 0; i< 7; i++){
            waveEntries.add(new Entry(times[i],waveHeights[i]));
            swellEntries.add(new Entry(times[i],swellHeights[i]));
        }

        // Creating Line Graphs for wave and swell heights
        LineDataSet waveDataSet =  new LineDataSet(waveEntries, "Wave Heights");
        waveDataSet.setColor(android.graphics.Color.argb(255,56,211,238));
        waveDataSet.setValueTextColor(android.graphics.Color.argb(1,109,109,109));
        LineData waveLineData = new LineData(waveDataSet);
        waveChart.setData(waveLineData);
        waveChart.notifyDataSetChanged();
        waveChart.invalidate();

        LineDataSet swellDataSet =  new LineDataSet(swellEntries, "Swell Heights");
        swellDataSet.setColor(android.graphics.Color.argb(255,56,211,238));
        swellDataSet.setValueTextColor(android.graphics.Color.argb(1,109,109,109));
        LineData swellLineData = new LineData(swellDataSet);
        swellChart.setData(swellLineData);
        swellChart.notifyDataSetChanged();
        swellChart.invalidate();
    }
}