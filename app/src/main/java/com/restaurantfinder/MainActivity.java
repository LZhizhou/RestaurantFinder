package com.restaurantfinder;

import android.app.ProgressDialog;
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
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.restaurantfinder.model.ReceivedPack;
import com.restaurantfinder.model.Result;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.restaurantfinder.RestaurantAdapter.RESTAURANT_LIST;

public class MainActivity extends AppCompatActivity {
    public final static int START_MAP = 0x07;
    public final static int RETURN_FROM_MAP = 0x08;
    private RecyclerView locationRecycleView;
    private List<PlaceWIthLatLonAndAddress> allLocations = new ArrayList<>();
    private LocationAdapter locationAdapter;
    //    private ArrayList<List<Result>> resultsList;
    private int queueCount = 0;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
//        startActivity(intent);
        initView();
        initRecycle();
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Finding restaurants");
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

            }

        }

    }

    private String getUrl(PlaceWIthLatLonAndAddress location){
        return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" + "location=" + location.lat + "," + location.lon +
                "&radius=" + 5000 +
                "&types=" + "restaurant" +
                "&key=" + getString(R.string.google_maps_key);
    }
    private  JsonObjectRequest request(String url){
        return new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ReceivedPack receivedPack = new Gson().fromJson(new JsonParser().parse(response.toString()), ReceivedPack.class);
                for (Result it : receivedPack.getResults()) {
                    if (!RESTAURANT_LIST.contains(it)) {
                        RESTAURANT_LIST.add(it);
                    }
                }
                queueCount--;
                if (queueCount == 0) {
                    progressDialog.dismiss();
                    Intent intent = new Intent(MainActivity.this, ResultRestaurant.class);
                    startActivity(intent);
                }
                Log.i(TAG, "onResponse: " + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: ", error);
            }
        });
    }

    public void searchRestaurant(View view) {
        if (allLocations != null && allLocations.size() > 0) {
            RESTAURANT_LIST = new ArrayList<>();
            progressDialog.show();
            for (PlaceWIthLatLonAndAddress place : allLocations) {
                queueCount++;
                RequestSingleton.getInstance(this).addToRequestQueue(request(getUrl(place)));
            }
        }

    }
}
