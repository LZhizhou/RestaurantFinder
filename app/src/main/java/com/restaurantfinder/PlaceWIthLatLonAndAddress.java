package com.restaurantfinder;

import com.google.android.gms.maps.model.LatLng;

public class PlaceWIthLatLonAndAddress {
    double lat, lon;
    String address;

    public PlaceWIthLatLonAndAddress(double lat, double lon, String address) {
        this.lat = lat;
        this.lon = lon;
        this.address = address;
    }

    public PlaceWIthLatLonAndAddress(LatLng latLng, String address) {
        this.lat = latLng.latitude;
        this.lon = latLng.longitude;
        this.address = address;
    }
}
