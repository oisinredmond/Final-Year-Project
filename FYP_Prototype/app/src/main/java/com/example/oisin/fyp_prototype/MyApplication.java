/*
 -----------------------------------------------
 Oisin Redmond - C15492202 - DT228/4
 Final Year Project Interim Prototype - SurfsApp
 -----------------------------------------------

 This class is used to provide static methods
 with the application context.*/

package com.example.oisin.fyp_prototype;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
