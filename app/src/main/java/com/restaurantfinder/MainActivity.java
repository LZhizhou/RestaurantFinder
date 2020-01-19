package com.restaurantfinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

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
                allLocations.add(new PlaceWIthLatLonAndAddress(bundle.getDouble("Lat"), bundle.getDouble("Lon"), bundle.getString("Address")));
                locationAdapter.notifyItemRangeChanged(0, allLocations.size());
            }

        }

    }
}
