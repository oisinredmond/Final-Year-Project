/*
 -----------------------------------------------
 Oisin Redmond - C15492202 - DT228/4
 Final Year Project - SurfsApp
 -----------------------------------------------
*/

package com.example.oisin.surfsapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.ButtCap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraMoveStartedListener {

    Dialog loginDialog;
    Dialog registerDialog;
    Dialog textSearchDialog;
    ImageView accountButton;
    ImageView cancelNavigation;
    RelativeLayout mapOverlay;
    GoogleApiClient googleApiClient;

    LocationFragmentMenu menuFragment;
    NavigationMenuFragment navMenuFragment;
    NavigationDirectionFragment navDirFragment;
    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft;

    static GoogleMap map;
    static LatLng userLatLng;
    static Location lastLocation;
    LocationManager locationManager;
    MapLocationListener locationListener;
    String provider;
    LocationsConnection locationsDb;
    static ArrayList<LocationObject> locations = new ArrayList<>();
    Marker focusedMarker;
    int searchRadius = 30000;
    Route route;

    FirebaseAuth auth;
    FirebaseDatabase db;
    FirebaseUser currentUser;
    private static final int FINE_LOCATION_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        setContentView(R.layout.activity_maps);
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton findLocations = findViewById(R.id.findLocs);
        FloatingActionButton textSearchLocations = findViewById(R.id.searchButton);
        ImageView searchSettings = findViewById(R.id.search_settings);
        mapOverlay = findViewById(R.id.map_overlay_root);
        accountButton = findViewById(R.id.account_button);
        cancelNavigation = findViewById(R.id.cancel_nav);
        cancelNavigation.setVisibility(View.INVISIBLE);
        cancelNavigation.setClickable(false);

        checkUser();

        locationsDb = new LocationsConnection(db.getReference().child("locations"));
        locationsDb.loadLocations();
        loginDialog = new Dialog(this);
        registerDialog = new Dialog(this);

        if (checkLocationPermission()) {
            buildGoogleApiClient();
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            locationListener = new MapLocationListener(this);
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            provider = locationManager.getBestProvider(criteria, true);
            locationManager.requestLocationUpdates(provider, 1000, 0, locationListener);
            lastLocation = locationManager.getLastKnownLocation(provider);
            userLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        }

        accountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (auth.getCurrentUser() != null) {
                    Intent i = new Intent(MapsActivity.this, AccountActivity.class);
                    startActivity(i);
                } else {
                    loginDialog.setContentView(R.layout.dialog_login);
                    loginDialog.show();

                    final TextView email = loginDialog.findViewById(R.id.email);
                    final TextView password = loginDialog.findViewById(R.id.password);
                    final Button login = loginDialog.findViewById(R.id.login);
                    final Button register = loginDialog.findViewById(R.id.register);

                    login.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            login(email.getText().toString(), password.getText().toString());
                        }
                    });

                    register.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            loginDialog.hide();
                            registerDialog.setContentView(R.layout.dialog_register);
                            registerDialog.show();
                            register();
                        }
                    });
                }
            }
        });

        findLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fm.findFragmentByTag("navMenu")!=null){
                    ft = fm.beginTransaction();
                    ft.setCustomAnimations(R.anim.fragment_animation_enter,R.anim.fragment_animation_exit);
                    ft.remove(navMenuFragment);
                    ft.commit();
                }
                moveCamera(new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude()), searchRadius);
                getLocations();
            }
        });

        textSearchLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textSearch();
            }
        });

        searchSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog settingsDialog = new Dialog(MapsActivity.this);
                settingsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                settingsDialog.setContentView(R.layout.dialog_search_settings);

                final int[] radius = getResources().getIntArray(R.array.search_radius);
                RadioGroup rg = settingsDialog.findViewById(R.id.radio_group);
                Button submit = settingsDialog.findViewById(R.id.submit);

                for (int i = 0; i < radius.length; i++) {
                    RadioButton rb = new RadioButton(MapsActivity.this);
                    rb.setText(radius[i] + " km");
                    rg.addView(rb);
                }
                settingsDialog.show();

                rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        for (int j = 0; j < radioGroup.getChildCount(); j++) {
                            RadioButton btn = (RadioButton) radioGroup.getChildAt(j);
                            if (btn.getId() == i) {
                                searchRadius = radius[j] * 1000;
                            }
                        }
                    }
                });

                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        settingsDialog.dismiss();
                    }
                });
            }});
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setOnMarkerClickListener(this);

        // Moves camera to the users location when map is initialised
        if (lastLocation != null) {
            //userLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            moveCamera(userLatLng,13);
        }

        // Enabling location tracking given the users permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }
        else {
            if(checkLocationPermission()) {
                map.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker){
        focusedMarker = marker;
        LatLng markerLatLng = marker.getPosition();
        moveCamera(markerLatLng,20);
        marker.showInfoWindow();
        Bundle args = new Bundle();
        args.putString("markerName",marker.getTitle());
        args.putDouble("lat", userLatLng.latitude);
        args.putDouble("lon", userLatLng.longitude);
        menuFragment = new LocationFragmentMenu();
        menuFragment.setArguments(args);
        ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_animation_enter,R.anim.fragment_animation_exit);
        ft.replace(R.id.menu_placeholder, menuFragment);
        ft.commit();
        return true;
    }

    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onCameraMoveStarted(int i) {
        ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_animation_enter,R.anim.fragment_animation_exit);
        ft.remove(menuFragment);
        ft.commit();
        focusedMarker.hideInfoWindow();
    }

    @Override
    public void onResume(){
        super.onResume();
        checkUser();
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
    public void onBackPressed(){
        ft = fm.beginTransaction();
        for (Fragment fragment:getSupportFragmentManager().getFragments()) {
            if (fragment instanceof SupportMapFragment) {
                continue;
            }
            else if(fragment != null){
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
        }
        ft.commit();
    }

    public void getLocations(){
        locations = locationsDb.getLocations();
        map.clear();
        float[] dist = new float[2];
        for(int i=0; i<locations.size();i++){
            String name = locations.get(i).name;
            double lat = locations.get(i).latitude;
            double lon = locations.get(i).longitude;
            Location.distanceBetween(lat, lon, userLatLng.latitude, userLatLng.longitude, dist);
            if(dist[0] < searchRadius) {
                LatLng loc = new LatLng(lat, lon);
                setMarker(name, loc);
            }
        }
    }

    public Marker setMarker(String name, LatLng loc){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(loc);
        markerOptions.title(name);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        return map.addMarker(markerOptions);
    }

    public void moveCamera(LatLng p, float z){
        if(z < 200000){
            z = ((300000 - z) / 300000) * 11;
        }else{
            z = 6f;
        }
        map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(p).zoom(z).build()));
    }

    public void textSearch(){
        String[] names = new String[LocationsConnection.locations.size()];
        for(int i = 0; i < names.length; i++){
            names[i] = LocationsConnection.locations.get(i).name;
        }

        textSearchDialog =  new Dialog(MapsActivity.this);
        textSearchDialog.setContentView(R.layout.dialog_textsearch);
        textSearchDialog.show();
        AutoCompleteTextView searchBar = textSearchDialog.findViewById(R.id.searchBar);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(MapsActivity.this, android.R.layout.select_dialog_item, names);
        searchBar.setAdapter(adapter);
        searchBar.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                String name = parent.getItemAtPosition(position).toString();
                for(int i = 0; i < LocationsConnection.locations.size(); i++){
                    if(LocationsConnection.locations.get(i).name == name){
                        LatLng loc = new LatLng(LocationsConnection.locations.get(i).latitude,
                                LocationsConnection.locations.get(i).longitude);
                        map.clear();
                        Marker m = setMarker(name, loc);
                        m.showInfoWindow();
                        moveCamera(loc, 20);
                        textSearchDialog.dismiss();
                        Bundle args = new Bundle();
                        args.putString("markerName",name);
                        args.putDouble("lat", userLatLng.latitude);
                        args.putDouble("lon", userLatLng.longitude);
                        menuFragment = new LocationFragmentMenu();
                        menuFragment.setArguments(args);
                        ft = fm.beginTransaction();
                        ft.setCustomAnimations(R.anim.fragment_animation_enter,R.anim.fragment_animation_exit);
                        ft.replace(R.id.menu_placeholder, menuFragment);
                        ft.commit();
                        break;
                    }
                }
            }
        });
    }

    public void login(String e, String p){
        auth.signInWithEmailAndPassword(e, p).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MapsActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    loginDialog.dismiss();
                    currentUser = auth.getCurrentUser();
                    Intent i = new Intent(MapsActivity.this, AccountActivity.class);
                    startActivity(i);
                }else{
                    Toast.makeText(MapsActivity.this, "Login error", Toast.LENGTH_SHORT ).show();
                }
            }
        });
    }

    public void register(){

        final TextView name = registerDialog.findViewById(R.id.name);
        final TextView email = registerDialog.findViewById(R.id.email);
        final TextView password = registerDialog.findViewById(R.id.password);
        final TextView confirmPassword = registerDialog.findViewById(R.id.confirm_password);
        final Button submit = registerDialog.findViewById(R.id.register_submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String s_name = name.getText().toString();
                final String s_email = email.getText().toString();
                String s_password = password.getText().toString();
                String s_confirm = confirmPassword.getText().toString();

                if(TextUtils.isEmpty(s_email) || TextUtils.isEmpty(s_password) || TextUtils.isEmpty(s_confirm)){
                    Toast.makeText(MapsActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();

                    if(TextUtils.isEmpty(s_email)){
                        email.setBackgroundResource(R.drawable.edit_text_error);
                    }
                    if(TextUtils.isEmpty(s_name)) {
                        name.setBackgroundResource(R.drawable.edit_text_error);
                    }
                    if(TextUtils.isEmpty(s_password)) {
                        password.setBackgroundResource(R.drawable.edit_text_error);
                    }
                    if(TextUtils.isEmpty(s_confirm)){
                        confirmPassword.setBackgroundResource(R.drawable.edit_text_error);
                    }
                }
                else if(!s_password.equals(s_confirm)) {
                    Toast.makeText(MapsActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    password.setBackgroundResource(R.drawable.edit_text_error);
                    confirmPassword.setBackgroundResource(R.drawable.edit_text_error);
                }
                else{
                    auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(MapsActivity.this, "Registration success. You are now logged in.", Toast.LENGTH_SHORT).show();
                                        currentUser = auth.getCurrentUser();
                                        db.getReference().child("users").child(currentUser.getUid()).child("name").setValue(s_name);
                                        db.getReference().child("users").child(currentUser.getUid()).child("email").setValue(s_email);
                                        db.getReference().child("users").child(currentUser.getUid()).child("profileImage").setValue("default_profile_image.png");
                                        registerDialog.dismiss();

                                    } else {
                                        Toast.makeText(MapsActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    public void checkUser(){
        if(auth.getCurrentUser()!= null){
            db.getReference().child("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child("profileImage").getValue() != null){
                        String imgId = dataSnapshot.child("profileImage").getValue().toString();
                        FirebaseStorage.getInstance().getReference().child("images/profiles/" + imgId).getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Glide.with(MapsActivity.this).load(uri).into(accountButton);
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
        else{
            accountButton.setImageResource(R.drawable.ic_account_icon);
        }
    }

    public void initNavigation(LatLng currLoc, LatLng dest){
        map.clear();
        if(checkLocationPermission())
            lastLocation = locationManager.getLastKnownLocation(provider);
        setMarker(null, dest);
        route = new Route(this);
        route.getDirections(currLoc, dest);
    }

    public void setNavigationMenu(Route r){

        List<LatLng> polyLineOverviewList = r.getOverViewPolyLine();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for(LatLng l : polyLineOverviewList){
            boundsBuilder.include(l);
        }

        LatLngBounds latLngBounds = boundsBuilder.build();
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,200));
        PolylineOptions pl = new PolylineOptions();
        pl.clickable(true);
        pl.endCap(new ButtCap());
        pl.color(R.color.colorPrimary);

        for(LatLng l : polyLineOverviewList){
            pl.add(l);
        }

        map.addPolyline(pl);

        navMenuFragment = new NavigationMenuFragment();

        String startAddr[] = route.getStartAddress().split(",");
        String endAddr[] = route.getEndAddress().split(",");

        Bundle args = new Bundle();
        args.putStringArray("startAddr", startAddr);
        args.putStringArray("endAddr", endAddr);
        args.putString("durationTxt", route.getDurationString());
        args.putString("distanceTxt", route.getDistanceString());

        navMenuFragment.setArguments(args);
        ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_animation_enter,R.anim.fragment_animation_exit);
        ft.replace(R.id.menu_placeholder, navMenuFragment, "navMenu");
        ft.commit();
    }

    public void startNavigation(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        locationListener = new NavigationLocationListener(this, route);
        Toast.makeText(this, "Navigation Started", Toast.LENGTH_SHORT).show();

        mapOverlay.setVisibility(View.INVISIBLE);
        mapOverlay.setClickable(false);
        cancelNavigation.setVisibility(View.VISIBLE);
        cancelNavigation.setClickable(true);

        if(checkLocationPermission()) {
            locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
            lastLocation = locationManager.getLastKnownLocation(provider);
        }

        userLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        float bearing = lastLocation.getBearing();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
                .target(userLatLng).zoom(16).bearing(bearing).tilt(40).build()));

        map.clear();
        for(RouteStep s : route.getSteps()){
            List<LatLng> polyLineList = s.getPolyLine();
            PolylineOptions pl = new PolylineOptions();
            pl.clickable(true);
            pl.endCap(new ButtCap());
            pl.color(R.color.colorPrimary);
            pl.addAll(polyLineList);
            map.addPolyline(pl);
        }

        RouteStep step = route.getSteps().get(0);
        replaceDirectionFragment(step.getHtmlInstructions(), step.getDuration(), step.getDistance(),
                null);

        cancelNavigation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(MapsActivity.this, "Navigation Cancelled", Toast.LENGTH_SHORT).show();
                locationManager.removeUpdates(locationListener);
                locationListener = new MapLocationListener(MapsActivity.this);
                mapOverlay.setVisibility(View.VISIBLE);
                mapOverlay.setClickable(true);
                map.clear();
                locationListener = new MapLocationListener(MapsActivity.this);
                if(checkLocationPermission()){
                    locationManager.requestLocationUpdates(provider, 3000, 0, locationListener);
                }
                setNavigationMenu(route);
            }
        });
    }

    public void replaceDirectionFragment(String html, String duration, String distance, String maneuver){

        navDirFragment = new NavigationDirectionFragment();
        Bundle args = new Bundle();
        args.putString("directionsHtml", html);
        args.putString("durationText", duration);
        args.putString("distanceText", distance);
        args.putString("maneuver",maneuver);
        navDirFragment.setArguments(args);

        ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_animation_enter,R.anim.fragment_animation_exit);
        ft.replace(R.id.menu_placeholder, navDirFragment);
        ft.commit();
    }

    public void setCurrentDistance(String s){
        TextView t = navDirFragment.getView().findViewById(R.id.distance_text);
        t.setText(s);
    }

}
