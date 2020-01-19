package com.restaurantfinder;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.restaurantfinder.model.Result;

import java.util.ArrayList;

public class ResultRestaurant extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_restaurant);

        RecyclerView allRestaurantRecycle = findViewById(R.id.all_restaurant_recycle);
        allRestaurantRecycle.setHasFixedSize(true);
        allRestaurantRecycle.setLayoutManager(new LinearLayoutManager(this));
        RestaurantAdapter restaurantAdapter = new RestaurantAdapter(this);
        allRestaurantRecycle.setAdapter(restaurantAdapter);

    }


}


class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {
    public static ArrayList<Result> RESTAURANT_LIST;
    private Context context;

    public RestaurantAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_restaurant, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Result item = RESTAURANT_LIST.get(position);
        holder.nameText.setText(item.getName());
        holder.clearItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int idx = RESTAURANT_LIST.indexOf(item);
                RESTAURANT_LIST.remove(item);
                notifyItemRemoved(idx);

            }
        });
        double rate = RESTAURANT_LIST.get(position).getRating();
        if (rate != 0) {
            holder.ratingBar.setRating((float) rate);
        }

    }

    @Override
    public int getItemCount() {
        return RESTAURANT_LIST.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        ImageButton clearItem;
        RatingBar ratingBar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.restaurant_name);
            clearItem = itemView.findViewById(R.id.clear_item);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
