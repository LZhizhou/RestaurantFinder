package com.restaurantfinder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class ResultRestaurant extends AppCompatActivity {
    private static ArrayList<Place> RESTAURANTS = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_restaurant);

        RecyclerView allRestaurantRecycle = findViewById(R.id.all_restaurant_recycle);
        allRestaurantRecycle.setHasFixedSize(true);
        allRestaurantRecycle.setLayoutManager(new LinearLayoutManager(this));
        RestaurantAdapter restaurantAdapter = new RestaurantAdapter(RESTAURANTS);
        allRestaurantRecycle.setAdapter(restaurantAdapter);

    }

}


class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {
    private ArrayList<Place> restaurantList;

    public RestaurantAdapter(ArrayList<Place> restaurantList) {
        this.restaurantList = restaurantList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_restaurant, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.nameText.setText(restaurantList.get(position).getName());
        holder.clearItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restaurantList.remove(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        MaterialButton clearItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.restaurant_name);
            clearItem = itemView.findViewById(R.id.clear_item);
        }
    }
}
