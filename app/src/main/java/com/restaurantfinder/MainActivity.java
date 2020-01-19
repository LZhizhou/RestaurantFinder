package com.restaurantfinder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MainActivity extends AppCompatActivity {
    public final static int START_MAP = 0x07;
    public final static int RETURN_FROM_MAP = 0x08;
    private RecyclerView locationRecycleView;
    private List<PlaceWIthLatLonAndAddress> allLocations = new ArrayList<>();
    private LocationAdapter locationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
//        startActivity(intent);
        initView();
        initRecycle();
    }

    private void initView(){
        locationRecycleView = findViewById(R.id.all_location_recycle);

    }

    private void initRecycle(){
        locationRecycleView.setLayoutManager(new LinearLayoutManager(this));

        locationAdapter = new LocationAdapter(MainActivity.this, allLocations, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivityForResult(intent, START_MAP);
            }
        });
        locationRecycleView.setAdapter(locationAdapter);
        locationRecycleView.setItemAnimator(new DefaultItemAnimator());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == START_MAP && resultCode == RETURN_FROM_MAP) {

            Bundle bundle = data != null ? data.getExtras() : null;
            if (bundle != null) {
                PlaceWIthLatLonAndAddress place = new PlaceWIthLatLonAndAddress(bundle.getDouble("Lat"), bundle.getDouble("Lon"), bundle.getString("Address"));
                allLocations.add(place);
                locationAdapter.notifyItemRangeChanged(0, allLocations.size());
                RequestSingleton.getInstance(this).addToRequestQueue(request(getUrl(place)));
            }

        }

    }

    private String getUrl(PlaceWIthLatLonAndAddress location){
        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        stringBuilder.append("location=").append(location.lat).append(",").append(location.lon);
        stringBuilder.append("&radius=").append(150);
        stringBuilder.append("&types=").append("restaurant");
        stringBuilder.append("&key=" + getString(R.string.google_maps_key));
        return  stringBuilder.toString();
    }
    private  JsonObjectRequest request(String url){
        return new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "onResponse: " + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: ", error);
            }
        });
    }
}
