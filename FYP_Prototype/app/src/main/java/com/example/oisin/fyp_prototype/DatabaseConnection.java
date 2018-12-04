/*
 -----------------------------------------------
 Oisin Redmond - C15492202 - DT228/4
 Final Year Project Interim Prototype - SurfsApp
 -----------------------------------------------
*/


package com.example.oisin.fyp_prototype;

import com.google.firebase.database.DatabaseReference;

public class DatabaseConnection {

    static DatabaseReference databaseRef;

    public DatabaseConnection(DatabaseReference dbRef){
        databaseRef = dbRef;
    }
}
