/*
 -----------------------------------------------
 Oisin Redmond - C15492202 - DT228/4
 Final Year Project - SurfsApp
 -----------------------------------------------
*/
package com.example.oisin.surfsapp;

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
