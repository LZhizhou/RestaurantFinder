package com.restaurantfinder;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMapClickListener {

    private static final String TAG = "onMap";
    private static final int DEFAULT_ZOOM = 15;
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0x0011;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private PlaceWIthLatLonAndAddress resLocation;
    private LatLng mDefaultLocation;
    private Marker mapMaker;
    private AutocompleteSupportFragment autoSearch;
    private Geocoder geocoder;
    private View mapView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Initialize Places.
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(),getString(R.string.google_maps_key));
        }
        geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());


        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mDefaultLocation =new LatLng(-34, 151);

        getLocationPermission();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        // Initialize the AutocompleteSupportFragment.
        autoSearch = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.location_auto_search);

        mapView = mapFragment.getView();


// Set up a PlaceSelectionListener to handle the response.
        if (autoSearch != null) {
            // Specify the types of place data to return.
            autoSearch.setPlaceFields(Arrays.asList(Place.Field.ID,
                    Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.ADDRESS));
            autoSearch.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {

                    // TODO: Get info about the selected place.
                    LatLng res = place.getLatLng();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(res, DEFAULT_ZOOM));
                    if (res != null) {
                        mapMaker.setPosition(res);
                        resLocation = new PlaceWIthLatLonAndAddress(res, place.getName());
                    }

                    //Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                }

                @Override
                public void onError(@NonNull Status status) {
                    // TODO: Handle the error.
                    //Log.i(TAG, "An error occurred: " + status);
                }


            });
        }
        MaterialButton confirmButton = findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putDouble("Lat", resLocation.lat);
                bundle.putDouble("Lon", resLocation.lon);
                bundle.putString("Address", resLocation.address);
                intent.putExtras(bundle);
                setResult(MainActivity.RETURN_FROM_MAP, intent);
                finish();
            }
        });
    }


    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

/*        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
        updateLocationUI();
        getDeviceLocation();


        mMap.setOnMyLocationButtonClickListener(this);

        mMap.setOnMapClickListener(this);

        mMap.getUiSettings().setMapToolbarEnabled(false);


        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            LatLng currentLocation;

                            if (mLastKnownLocation==null){
                                currentLocation= mDefaultLocation;
                            }else{
                                currentLocation = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            }
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    currentLocation, DEFAULT_ZOOM));
                            if (mapMaker==null){
                                mapMaker = mMap.addMarker(new MarkerOptions().position(currentLocation));
                            }
                            else{
                                mapMaker.setPosition(currentLocation);
                            }
                            resLocation = new PlaceWIthLatLonAndAddress(currentLocation, getAddressFromLatLon(currentLocation));
/*                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putDouble("Latitude", mLastKnownLocation.getLatitude());
                            bundle.putDouble("Longitude", mLastKnownLocation.getLongitude());
                            intent.putExtras(bundle);
                            setResult(0x10, intent);*/
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }


    @Override
    public void onMapClick(LatLng latLng) {
        if (mapMaker == null) {
            mapMaker = mMap.addMarker(new MarkerOptions().position(latLng));
        } else {
            mapMaker.setPosition(latLng);


        }
        String address = getAddressFromLatLon(latLng);
        autoSearch.setText(address);
        resLocation = new PlaceWIthLatLonAndAddress(latLng, address);
    }

    private String getAddressFromLatLon(LatLng latLng) {
        List<Address> addresses = null;
        String errorMessage = "";
        StringBuilder builder = new StringBuilder();
        try {
            addresses = geocoder.getFromLocation(
                    latLng.latitude,
                    latLng.longitude, 1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            //errorMessage = getString(R.string.service_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            //errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + latLng.latitude +
                    ", Longitude = " +
                    latLng.longitude, illegalArgumentException);
        }
        if (addresses != null && addresses.size() > 0) {

            Address address = addresses.get(0);


//                        ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
//                        for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
//                            addressFragments.add(address.getAddressLine(i));
//                        }
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                builder.append(address.getAddressLine(i));
            }
            Log.i(TAG, "onClickMap: " + builder.toString());

        }
        return builder.toString();
    }
}