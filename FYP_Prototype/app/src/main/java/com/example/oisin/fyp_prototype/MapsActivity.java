package com.example.oisin.fyp_prototype;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        LocationListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraMoveStartedListener {

    double latitude;
    double longitude;
    int radius = 30000;
    ArrayList<LocationObject> locations = new ArrayList<>();
    FloatingActionButton findLocations;
    LocationFragmentMenu menuFragment;
    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft;
    GoogleMap map;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LatLng latLng;
    DatabaseReference db;
    LocationsConnection locationsDb;
    Intent i;
    String locationName;
    private static final int FINE_LOCATION_PERMISSION_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_maps);
        android.support.v7.widget.Toolbar myToolbar = findViewById(R.id.toolbar);
        findLocations = findViewById(R.id.findLocs);

        loadLocations();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if(checkLocationPermission()) {
            Criteria criteria = new Criteria();
            LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            lastLocation = manager.getLastKnownLocation(String.valueOf(manager.getBestProvider(criteria, true)));
            latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            buildGoogleApiClient();
        }

        findLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocations();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setOnMarkerClickListener(this);
        map.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                if(menuFragment!=null) {
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.setCustomAnimations(R.anim.fragment_animation_exit,R.anim.fragment_animation_exit);
                    ft.remove(menuFragment);
                    ft.commit();
                }
            }
        });

        if (lastLocation != null) {
            final double currentLatitude = lastLocation.getLatitude();
            final double currentLongitude = lastLocation.getLongitude();
            latLng = new LatLng(currentLatitude,currentLongitude);
            moveCamera(latLng,13);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }
        else {
            map.setMyLocationEnabled(true);
        }
    }

    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_LOCATION_PERMISSION_REQUEST);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        moveCamera(latLng,10);
    }

    @Override
    public boolean onMarkerClick(final Marker marker){
        Bundle args = new Bundle();
        args.putString("markerName",marker.getTitle());
        locationName = marker.getTitle();
        LatLng markerLatLng = marker.getPosition();
        moveCamera(markerLatLng,15);
        marker.showInfoWindow();
        menuFragment = new LocationFragmentMenu();
        menuFragment.setArguments(args);
        ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_animation_enter,R.anim.fragment_animation_exit);
        ft.replace(R.id.menu_placeholder,menuFragment);
        ft.commit();
        return true;
    }

    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    public void loadLocations(){
        db = FirebaseDatabase.getInstance().getReference().child("locations");
        locationsDb = new LocationsConnection(db);
        locationsDb.loadLocations();
    }

    public void getLocations(){
        locations = locationsDb.getLocations();
        map.clear();
        float[] dist = new float[2];
        for(int i=0; i<locations.size();i++){
            String name = locations.get(i).name;
            double lat = locations.get(i).latitude;
            double lon = locations.get(i).longitude;
            Location.distanceBetween(locations.get(i).latitude,locations.get(i).longitude,lastLocation.getLatitude(),lastLocation.getLongitude(),dist);
            if(dist[0] < radius) {
                MarkerOptions markerOptions = new MarkerOptions();
                LatLng latLng = new LatLng(lat, lon);
                markerOptions.position(latLng);
                markerOptions.title(name);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                map.addMarker(markerOptions);
            }
        }
        moveCamera(latLng,10);
    }

    public void moveCamera(LatLng p, float z){
        map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(p).zoom(z).build()));
    }

    @Override
    public void onCameraMoveStarted(int i) {
        ft = fm.beginTransaction();
        ft.remove(menuFragment);
        ft.commit();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

}
