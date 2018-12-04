/*
 -----------------------------------------------
 Oisin Redmond - C15492202 - DT228/4
 Final Year Project Interim Prototype - SurfsApp
 -----------------------------------------------
*/

package com.example.oisin.fyp_prototype;

// This class is used to store forecast data for a day in a particular location
// Includes arrays of length representative of information for 3 hour intervals in each day
public class ForecastDay {

    private String name;
    private String date;
    private float maxTemp;
    private float minTemp;
    private float[] waveHeight = new float[8];
    private float[] precipitation = new float[8];
    private float[] pressure = new float[8];
    private String[] swellDir = new String[8];
    private float[] swellDirDeg = new float[8];
    private float[] swellHeight = new float[8];
    private float[] swellPeriod = new float[8];
    private float[] feelsLikeTemp = new float[8];
    private float[] temp = new float[8];
    private float[] waterTemp = new float[8];
    private float[] windSpeed = new float[8];
    private float[] windDirDeg = new float[8];
    private String[] windDir = new String[8];


    public float[] getWaveHeight() {
        return waveHeight;
    }

    public void setWaveHeight(float[] waveHeight) {
        this.waveHeight = waveHeight;
    }

    public float[] getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(float[] precipitation) {
        this.precipitation = precipitation;
    }

    public float[] getPressure() {
        return pressure;
    }

    public void setPressure(float[] pressure) {
        this.pressure = pressure;
    }

    public String[] getSwellDir() {
        return swellDir;
    }

    public void setSwellDir(String[] swellDir) {
        this.swellDir = swellDir;
    }

    public float[] getSwellDirDeg() {
        return swellDirDeg;
    }

    public void setSwellDirDeg(float[] swellDirDeg) {
        this.swellDirDeg = swellDirDeg;
    }

    public float[] getSwellHeight() {
        return swellHeight;
    }

    public void setSwellHeight(float[] swellHeight) {
        this.swellHeight = swellHeight;
    }

    public float[] getSwellPeriod() {
        return swellPeriod;
    }

    public void setSwellPeriod(float[] swellPeriod) {
        this.swellPeriod = swellPeriod;
    }

    public float[] getFeelsLikeTemp() {
        return feelsLikeTemp;
    }

    public void setFeelsLikeTemp(float[] feelsLikeTemp) {
        this.feelsLikeTemp = feelsLikeTemp;
    }

    public float[] getTemp() {
        return temp;
    }

    public void setTemp(float[] temp) {
        this.temp = temp;
    }

    public float[] getWaterTemp() {
        return waterTemp;
    }

    public void setWaterTemp(float[] waterTemp) {
        this.waterTemp = waterTemp;
    }

    public float[] getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(float[] windSpeed) {
        this.windSpeed = windSpeed;
    }

    public float[] getWindDirDeg() {
        return windDirDeg;
    }

    public void setWindDirDeg(float[] windDirDeg) {
        this.windDirDeg = windDirDeg;
    }

    public String[] getWindDir() {
        return windDir;
    }

    public void setWindDir(String[] windDir) {
        this.windDir = windDir;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setMaxTemp(float maxTemp) {
        this.maxTemp = maxTemp;
    }

    public void setMinTemp(float minTemp) {
        this.minTemp = minTemp;
    }

    public ForecastDay(){ }

    public String getName(){
        return name;
    }

    public String getDate(){
        return date;
    }

    public float getMaxTemp() {
        return maxTemp;
    }

    public float getMinTemp() {
        return minTemp;
    }
}
