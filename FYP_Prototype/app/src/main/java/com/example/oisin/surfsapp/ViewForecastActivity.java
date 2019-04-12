/*
 -----------------------------------------------
 Oisin Redmond - C15492202 - DT228/4
 Final Year Project - SurfsApp
 -----------------------------------------------
*/
package com.example.oisin.surfsapp;

import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ViewForecastActivity extends FragmentActivity {

    static ArrayList<ForecastDay> forecastDays = new ArrayList<>();
    static ForecastConnection fc;
    DatabaseReference db;
    static String[] dates = new String[7];
    static String[] daysOfWeek = new String[9];
    static String[] timeStrings = {"3am","6am","9am","12am","3pm","6pm","9pm","12pm"};

    float[] waveHeights = new float[8], swellPeriods = new float[8], swellHeights = new float[8], windSpeeds = new float[8], temps = new float[8], pressures = new float[8], windDirsDeg= new float[8], precipitation = new float[8], swellDirsDeg = new float[8];
    String[] windDirs = new String[8], swellDirs =  new String[8];

    String name;
    RadioGroup heatRainRadio;
    RadioGroup heatRainTimeRadio;
    ImageView currWeatherIcon;
    ImageView expandArrow;
    ImageView currWindDirIcon;
    TextView currWindDir;
    TextView currWindSpeed;
    TextView currTemp;
    TextView currWaveHeight;
    TextView currPrecip;
    TextView currPressure;
    TextView windTitle;
    BarChart heatRainChart;
    LineChart heatRainChartWeek;
    CombinedChart waveSwellChart;
    static Spinner dateMenu;
    BottomNavigationView navBar;
    FragmentManager fm = getSupportFragmentManager();
    String valueFormatString = "";
    FragmentTransaction ft;
    ReviewsFragment reviewsFragment;
    GalleryFragment galleryFragment;
    RelativeLayout expandForecast;
    CardView forecastChart;
    ForecastDay currentDay;
    int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        db = FirebaseDatabase.getInstance().getReference().child("locations");
        name = getIntent().getStringExtra("markerName");
        fc = new ForecastConnection(db, name);
        dateMenu = findViewById(R.id.dateSpinner);
        currWeatherIcon = findViewById(R.id.currentWeatherIcon);
        currTemp = findViewById(R.id.currentTemp);
        currWaveHeight = findViewById(R.id.currentWaveHeight);
        currPrecip = findViewById(R.id.currentPrecip);
        currPressure = findViewById(R.id.currentPressure);
        currWindDirIcon = findViewById(R.id.windDirIcon);
        currWindSpeed = findViewById(R.id.currWindSpeed);
        currWindDir = findViewById(R.id.currWindDir);
        heatRainChart = findViewById(R.id.generalChart);
        heatRainChartWeek = findViewById(R.id.generalChartWeek);
        waveSwellChart = findViewById(R.id.waveSwellChart);
        expandForecast = findViewById(R.id.expandForecast);
        forecastChart = findViewById(R.id.tempChart);
        windTitle = findViewById(R.id.windTitle);
        heatRainRadio = findViewById(R.id.heatPrecipRadio);
        heatRainTimeRadio = findViewById(R.id.todayWeekRadio);
        final TextView title = findViewById(R.id.toolbarTitle);
        title.setText(name + " - Forecast");
        navBar = findViewById(R.id.navigation_view);

        expandForecast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandArrow = findViewById(R.id.expandArrow);
                if(forecastChart.getVisibility()== View.GONE){
                    expandArrow.setImageResource(R.drawable.baseline_expand_less_24);
                    forecastChart.setVisibility(View.VISIBLE);
                }
                else if(forecastChart.getVisibility()== View.VISIBLE){
                    expandArrow.setImageResource(R.drawable.baseline_expand_more_24);
                    forecastChart.setVisibility(View.GONE);
                }
            }
        });

        dateMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                pos = position;
                setViews();
                setHeatRainChart();
                setHeatRainChartWeek();
                setWaveSwellChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        navBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Bundle args = new Bundle();
                args.putBoolean("singleUser",false);
                args.putString("locationName",name);
                args.putString("friendId", null);
                args.putString("friendName","");

                if(item.getItemId() == R.id.reviews){
                    findViewById(R.id.forecast).setClickable(true);
                    findViewById(R.id.photos).setClickable(true);
                    findViewById(R.id.reviews).setClickable(false);
                    reviewsFragment = new ReviewsFragment();
                    reviewsFragment.setArguments(args);
                    ft = fm.beginTransaction();
                    ft.replace(R.id.fragmentPlaceholder, reviewsFragment);
                    ft.commit();
                }
                else if(item.getItemId() == R.id.photos){
                    findViewById(R.id.forecast).setClickable(true);
                    findViewById(R.id.reviews).setClickable(true);
                    findViewById(R.id.photos).setClickable(false);
                    galleryFragment = new GalleryFragment();
                    galleryFragment.setArguments(args);
                    ft = fm.beginTransaction();
                    ft.replace(R.id.fragmentPlaceholder, galleryFragment);
                    ft.commit();
                }
                else if(item.getItemId() == R.id.forecast){
                    findViewById(R.id.photos).setClickable(true);
                    findViewById(R.id.reviews).setClickable(true);
                    findViewById(R.id.forecast).setClickable(false);
                    title.setText(name + " - Forecast");
                    for (Fragment fragment:getSupportFragmentManager().getFragments()) {
                        ft = fm.beginTransaction();
                        ft.remove(fragment);
                        ft.commit();
                    }
                }
                return false;
            }
        });

        heatRainRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (((RadioButton) findViewById(R.id.todayRadio)).isChecked()) {
                    setHeatRainChart();
                }
                else if((((RadioButton) findViewById(R.id.weekRadio)).isChecked())){
                    setHeatRainChartWeek();
                }
            }
        });

        heatRainTimeRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.todayRadio){
                    heatRainChartWeek.setVisibility(View.GONE);
                    setHeatRainChart();
                    heatRainChart.setVisibility(View.VISIBLE);
                }
                else if(i == R.id.weekRadio){
                    heatRainChart.setVisibility(View.GONE);
                    setHeatRainChartWeek();
                    heatRainChartWeek.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    // Gets each forecast day for the location and sets the dates for the dropdown menu
    public static void getForecast() {
        forecastDays = fc.forecastDays;
        for(int i =0; i< forecastDays.size(); i++) {
            SimpleDateFormat df_in = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            SimpleDateFormat df_out = new SimpleDateFormat("EEEE, dd MMM", java.util.Locale.getDefault());
            SimpleDateFormat df_dayOfWeek = new SimpleDateFormat("EEE", java.util.Locale.getDefault());
            try {
                Date d = df_in.parse(forecastDays.get(i).getDate());
                dates[i] = df_out.format(d);
                daysOfWeek[i+1] = df_dayOfWeek.format(d);
            }catch (ParseException e){
                e.printStackTrace();
            }
        }
        setAdapter();
    }

    // Creates a dropdown menu for the user to choose a day in the week
    public static void setAdapter(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MyApplication.getAppContext(), R.layout.spinner_item, dates);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateMenu.setAdapter(adapter);
    }

    // Sets the text views and graphs using forecast data
    public void setViews(){

        int timeIndex = 3;
        if(pos == 0){
            Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            hour = (int)Math.ceil(hour/3.0) * 3;
            timeIndex = hour / 3;
            if(timeIndex == 8){
                timeIndex = 0;
            }
        }

        currentDay = forecastDays.get(pos);
        waveHeights = currentDay.getWaveHeight();
        swellHeights = currentDay.getSwellHeight();
        windSpeeds = currentDay.getWindSpeed();
        temps = currentDay.getTemp();
        windDirs = currentDay.getWindDir();
        windDirsDeg = currentDay.getWindDirDeg();
        precipitation = currentDay.getPrecipitation();
        swellDirsDeg = currentDay.getSwellDirDeg();
        swellDirs = currentDay.getSwellDir();
        pressures = currentDay.getPressure();
        swellPeriods = currentDay.getSwellPeriod();

        currTemp.setText(Integer.toString((int)temps[timeIndex])+"°C");
        currWaveHeight.setText(Float.toString(waveHeights[timeIndex])+" m");
        currPrecip.setText(Float.toString(precipitation[timeIndex])+" mm");
        currPressure.setText(Integer.toString((int)pressures[timeIndex])+" hPa");

        currWindDirIcon.setRotation(currentDay.getWindDirDeg()[timeIndex]);
        currWindSpeed.setText(Integer.toString((int)windSpeeds[timeIndex])+" km/h");
        currWindDir.setText(Integer.toString((int)windDirsDeg[timeIndex]) + "° " + windDirs[timeIndex]);

        float clouds = currentDay.getCloudCover()[timeIndex];
        float humidity = currentDay.getCloudCover()[timeIndex];

        if(humidity > 60){
            currWeatherIcon.setImageResource(R.drawable.ic_wi_rain);
        }
        else if(clouds > 60){
            currWeatherIcon.setImageResource(R.drawable.ic_wi_cloudy);
        }
        else if(clouds > 25){
            currWeatherIcon.setImageResource(R.drawable.ic_wi_day_cloudy);
        }
        else{
            currWeatherIcon.setImageResource(R.drawable.ic_wi_day_sunny);
        }

        for(int i = 0; i<= 7; i++){

            if(i <= 6) {
                String windTime = "windTimeText" + Integer.toString(i + 1);
                String windSpeed = "windSpeed" + Integer.toString(i + 1);
                String windDir = "windDir" + Integer.toString(i + 1);
                String windDirText = "windDirText" + Integer.toString(i + 1);

                int timeId = getResources().getIdentifier(windTime, "id", getPackageName());
                int speedId = getResources().getIdentifier(windSpeed, "id", getPackageName());
                int dirId = getResources().getIdentifier(windDir, "id", getPackageName());
                int dirTextId = getResources().getIdentifier(windDirText, "id", getPackageName());

                TextView v_windTime = findViewById(timeId);
                TextView v_windSpeed = findViewById(speedId);
                TextView v_dirText = findViewById(dirTextId);
                ImageView v_dir = findViewById(dirId);

                v_dirText.setTextColor(getColor(R.color.orange));
                v_windTime.setText(timeStrings[i]);
                v_dirText.setText((int) windDirsDeg[i] + "° " + windDirs[i]);
                v_windSpeed.setText((int) windSpeeds[i] + " km/h");
                v_dir.setRotation(windDirsDeg[i]);
            }
        }

        TextView v_windTime = findViewById(R.id.windTimeText8);
        TextView v_windSpeed = findViewById(R.id.windSpeed8);
        TextView v_dirText = findViewById(R.id.windDirText8);
        ImageView v_dir = findViewById(R.id.windDir8);

        v_dirText.setTextColor(getColor(R.color.orange));
        v_windTime.setText(timeStrings[7]);
        v_dirText.setText((int)forecastDays.get(pos+1).getWindDirDeg()[0] + "° " + forecastDays.get(pos+1).getWindDir()[0]);
        v_windSpeed.setText((int) forecastDays.get(pos+1).getWindSpeed()[0] + " km/h");
        v_dir.setRotation(forecastDays.get(pos+1).getWindDirDeg()[0]);
    }

    public void setHeatRainChart(){

        List<BarEntry> tempEntries = new ArrayList<>();
        List<BarEntry> precipEntries = new ArrayList<>();

        for(int i = 0; i<= 6; i++){
            tempEntries.add(new BarEntry(i,(int)temps[i+1]));
            precipEntries.add(new BarEntry(i,precipitation[i+1]));
        }

        int gradientStart = 0;
        int gradientEnd = 0;
        if(pos != 6) {
            tempEntries.add(new BarEntry(7, (int) forecastDays.get(pos + 1).getTemp()[0]));
            precipEntries.add(new BarEntry(7, forecastDays.get(pos + 1).getPrecipitation()[0]));
        }
        else{
            tempEntries.add(new BarEntry(7,(int)temps[6]));
            precipEntries.add(new BarEntry(7,precipitation[6]));
        }

        BarDataSet dataSet = new BarDataSet(tempEntries, "");

        int y = (int)Math.ceil((double) dataSet.getYMax());
        if(y >= 5) {
            y = (y - (y % 5)) + 5;
        }
        else {
            y = 5;
        }

        if(((RadioButton)findViewById(R.id.heatRadio)).isChecked()){
            dataSet.setValueTextSize(11);
            valueFormatString = "°C";
            gradientStart = getColor(R.color.colorAccent);
            gradientEnd = getColor(R.color.matte_red);
        }
        else if(((RadioButton)findViewById(R.id.precipRadio)).isChecked()){
            dataSet =  new BarDataSet(precipEntries, "");
            valueFormatString = "mm";
            dataSet.setValueTextSize(10);
            gradientStart = getColor(R.color.colorAccent);
            gradientEnd = getColor(R.color.colorPrimaryDark);
        }

        dataSet.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                if (((RadioButton)findViewById(R.id.precipRadio)).isChecked()) {
                    if(value <=0f){
                        return "";
                    }else {
                        return Float.toString(value) + ViewForecastActivity.this.valueFormatString;
                    }
                }else{
                    return Integer.toString((int)value) + ViewForecastActivity.this.valueFormatString;
                }
            }
        });

        BarData data = new BarData(dataSet);
        data.setValueTextColor(getColor(R.color.orange));
        data.setBarWidth(0.8f);

        heatRainChart.getXAxis().setDrawGridLines(false);
        heatRainChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        Paint mPaint = heatRainChart.getRenderer().getPaintRender();
        mPaint.setShader(new LinearGradient(0,heatRainChart.getTranslationY() + heatRainChart.getHeight(),0,heatRainChart.getTranslationY(),gradientStart,gradientEnd, Shader.TileMode.CLAMP));

        heatRainChart.setFitBars(true);
        heatRainChart.getLegend().setEnabled(false);
        heatRainChart.getAxisLeft().setAxisMaximum(y);
        heatRainChart.getAxisLeft().setAxisMinimum(0);
        heatRainChart.getAxisLeft().setTextSize(10);
        heatRainChart.getAxisRight().setDrawLabels(false);
        heatRainChart.getAxisRight().setDrawGridLines(false);
        heatRainChart.animateY(1000);
        heatRainChart.setDrawGridBackground(false);
        heatRainChart.setDrawBorders(true);
        heatRainChart.setDragXEnabled(true);
        heatRainChart.setDescription(null);
        heatRainChart.setAutoScaleMinMaxEnabled(false);
        heatRainChart.setDragEnabled(true);
        heatRainChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(timeStrings));
        heatRainChart.setData(data);
        heatRainChart.invalidate();
    }

    public void setHeatRainChartWeek(){
        List<Entry> heatEntries = new ArrayList<>();
        List<Entry> precipEntries = new ArrayList<>();
        List<Entry> selctedEntries = new ArrayList<>();

        int gradient = 0;

        float startHeat = forecastDays.get(0).getTemp()[0];
        float endHeat = forecastDays.get(6).getTemp()[7];
        float startPrecip = forecastDays.get(0).getPrecipitation()[0];
        float endPrecip = forecastDays.get(6).getPrecipitation()[7];
        daysOfWeek[0] = "";
        daysOfWeek[8] = "";

        heatEntries.add(new Entry(0, startHeat));
        precipEntries.add(new Entry(0, startPrecip));
        for(int i = 1; i< 8; i++){
            heatEntries.add(new Entry(i, (int)forecastDays.get(i-1).getMaxTemp()));
            float totalRain = 0;
            for(float f : forecastDays.get(i-1).getPrecipitation()){
                totalRain += f;
            }
            precipEntries.add(new Entry(i, (int)totalRain));
        }

        heatEntries.add(new Entry(8, endHeat));
        precipEntries.add(new Entry(8, endPrecip));

        if(((RadioButton)findViewById(R.id.heatRadio)).isChecked()){
            valueFormatString = "°C";
            selctedEntries = heatEntries;
            gradient = R.drawable.temperature_gradient;
        }
        else if(((RadioButton)findViewById(R.id.precipRadio)).isChecked()){
            valueFormatString = " mm";
            selctedEntries = precipEntries;
            gradient = R.drawable.rain_gradient;
        }

        LineDataSet lineDataSet = new LineDataSet(selctedEntries, "");
        lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSet.setCubicIntensity(0.05f);
        lineDataSet.setLineWidth(2f);
        lineDataSet.setFillDrawable(getDrawable(gradient));
        lineDataSet.setDrawFilled(true);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                if(entry.getX() == 0 || entry.getX() == 8){
                    return "";
                }
                else{
                    return Integer.toString((int)value) + ViewForecastActivity.this.valueFormatString;
                }
            }
        });

        LineData data = new LineData(lineDataSet);
        data.setValueTextSize(10);

        heatRainChartWeek.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        int y = Math.round(lineDataSet.getYMax());
        if(y > 5){
            y = (y - (y % 5)) + 5;
        } else {
            y = 5;
        }
        heatRainChartWeek.getLegend().setEnabled(false);
        heatRainChartWeek.getAxisLeft().setAxisMaximum(y);
        heatRainChartWeek.getAxisLeft().setAxisMinimum(0);
        heatRainChartWeek.getAxisLeft().setTextSize(10);
        heatRainChartWeek.getAxisLeft().setDrawGridLines(false);
        heatRainChartWeek.getAxisRight().setDrawLabels(false);
        heatRainChartWeek.getAxisRight().setDrawGridLines(false);
        heatRainChartWeek.setDrawGridBackground(false);
        heatRainChartWeek.setDrawBorders(false);
        heatRainChartWeek.setDragXEnabled(true);
        heatRainChartWeek.setDescription(null);
        heatRainChartWeek.setAutoScaleMinMaxEnabled(false);
        heatRainChartWeek.getXAxis().setValueFormatter(new IndexAxisValueFormatter(daysOfWeek));
        heatRainChartWeek.setData(data);
        heatRainChartWeek.invalidate();
    }

    public void setWaveSwellChart(){

        List<BarEntry> swellEntries = new ArrayList<>();
        List<Entry> swellPeriodEntries = new ArrayList<>();
        String timesWave[] = {"","6am","9am","12am","3pm","6pm","9pm",""};

        swellEntries.add(new BarEntry(0, 0));
        swellPeriodEntries.add(new Entry(0,0));
        for(int i = 1; i<= 6; i++){
            swellEntries.add(new BarEntry(i,swellHeights[i]));
            swellPeriodEntries.add(new Entry(i, swellPeriods[i]));
        }
        swellEntries.add(new BarEntry(7, 0));
        swellPeriodEntries.add(new Entry(7,0));

        BarDataSet swellDataSet = new BarDataSet(swellEntries,"Height (meters)");
        swellDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        swellDataSet.setGradientColor(getColor(R.color.colorPrimary),getColor(R.color.colorPrimaryDark));

        LineDataSet swellPeriodDataSet = new LineDataSet(swellPeriodEntries, "Period (seconds)");
        swellPeriodDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        swellPeriodDataSet.setCubicIntensity(0.05f);
        swellPeriodDataSet.setLineWidth(2f);
        swellPeriodDataSet.setColor(getColor(R.color.orange));
        swellPeriodDataSet.setDrawFilled(false);
        swellPeriodDataSet.setDrawCircles(true);
        swellPeriodDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

        final LineData swellPeriodData = new LineData(swellPeriodDataSet);
        swellPeriodData.setValueTextColor(getColor(R.color.orange));
        BarData swellData = new BarData(swellDataSet);
        swellData.setBarWidth(0.7f);
        swellData.setValueTextSize(10);
        final CombinedData combinedData = new CombinedData();
        combinedData.setData(swellData);
        combinedData.setData(swellPeriodData);

        swellPeriodData.setValueTextSize(10);
        int y = Math.round(swellPeriodData.getYMax());
        int y2 = Math.round(swellData.getYMax());
        if(y > 5){
            y = (y - (y % 5)) + 5;
        } else {
            y = 5;
        }
        if(y2 > 5){
            y2 = (y2 - (y2 % 5)) + 5;
        } else {
            y2 = 5;
        }
        waveSwellChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR,  CombinedChart.DrawOrder.LINE
        });

        combinedData.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                if (entry.getX() == 0 || entry.getX() == 7) {
                    return "";
                } else if (entry instanceof  BarEntry) {
                    return Float.toString(value) + " m";
                }
                else {
                    return Float.toString(value) + " s";
                }
            }
        });

        waveSwellChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        waveSwellChart.getXAxis().setDrawGridLines(false);
        waveSwellChart.getLegend().setEnabled(true);
        waveSwellChart.getAxisLeft().setAxisMaximum(y2);
        waveSwellChart.getAxisLeft().setAxisMinimum(0);
        waveSwellChart.getAxisLeft().setTextSize(10);
        waveSwellChart.getAxisLeft().setDrawGridLines(true);
        waveSwellChart.getAxisRight().setDrawGridLines(false);
        waveSwellChart.getAxisRight().setAxisMaximum(y);
        waveSwellChart.getAxisRight().setAxisMinimum(0);
        waveSwellChart.setDrawGridBackground(false);
        waveSwellChart.setDrawBorders(false);
        waveSwellChart.setDragXEnabled(true);
        waveSwellChart.setDescription(null);
        waveSwellChart.setAutoScaleMinMaxEnabled(false);
        waveSwellChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(timesWave));
        waveSwellChart.setData(combinedData);
        waveSwellChart.invalidate();
    }

    @Override
    public void onBackPressed(){
        finish();
    }
}