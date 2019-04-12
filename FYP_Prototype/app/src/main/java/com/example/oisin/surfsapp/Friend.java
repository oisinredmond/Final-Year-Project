/*
 -----------------------------------------------
 Oisin Redmond - C15492202 - DT228/4
 Final Year Project - SurfsApp
 -----------------------------------------------
*/


package com.example.oisin.surfsapp;

public class Friend {

    public String getName() {
        return name;
    }

    public void setImageId(String imageUrl) {
        this.imageId = imageUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getImageId() {
        return imageId;
    }

    public String getId() { return id; }

    private String id;
    private String name;
    private String email;
    private String imageId;

    public Friend(String i, String n, String e, String u){
        this.id = i;
        this.name = n;
        this.email = e;
        this.imageId = u;
    }
}
