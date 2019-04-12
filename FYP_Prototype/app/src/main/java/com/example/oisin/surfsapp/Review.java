package com.example.oisin.surfsapp;

public class Review {

    private String locationName;
    private String summary;
    private String body;
    private String date;
    private String imgId;

    public String getUserId() {
        return userId;
    }

    private String userId;
    private float rating;

    public Review(String name, String summary, String body, String date, String imgId, String userId, float rating){
        this.locationName = name;
        this.summary = summary;
        this.body = body;
        this.date = date;
        this.imgId = imgId;
        this.rating = rating;
        this.userId = userId;
    }

    public String getLocationName() {return locationName;}

    public String getSummary() {
        return summary;
    }

    public String getBody() {
        return body;
    }

    public String getDate() {
        return date;
    }

    public String getImgId() {
        return imgId;
    }

    public float getRating() {
        return rating;
    }
}
