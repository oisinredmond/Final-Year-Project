package com.example.oisin.fyp_prototype;

import com.google.firebase.database.DatabaseReference;

public class DatabaseConnection {

    static DatabaseReference databaseRef;

    public DatabaseConnection(DatabaseReference dbRef){
        databaseRef = dbRef;
    }
}
