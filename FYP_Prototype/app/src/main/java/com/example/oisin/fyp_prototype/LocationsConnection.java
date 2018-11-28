package com.example.oisin.fyp_prototype;

import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LocationsConnection extends DatabaseConnection {

    private ArrayList<LocationObject> locations = new ArrayList<>();

    public ArrayList<LocationObject> getLocations() {
        return locations;
    }

    public LocationsConnection(DatabaseReference db){
        super(db);
    }

    public void loadLocations() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String name = snapshot.getKey();
                    double lon = (double) snapshot.child("coordinates").child("0").getValue();
                    double lat = (double) snapshot.child("coordinates").child("1").getValue();
                    LocationObject l = new LocationObject(name, lat, lon);
                    locations.add(l);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}