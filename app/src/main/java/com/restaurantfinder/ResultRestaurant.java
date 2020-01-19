package com.restaurantfinder;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.restaurantfinder.model.Result;
import com.restaurantfinder.model.Route;
import com.restaurantfinder.model.RouteReceived;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ResultRestaurant extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_restaurant);

        RecyclerView allRestaurantRecycle = findViewById(R.id.all_restaurant_recycle);
        allRestaurantRecycle.setHasFixedSize(true);
        allRestaurantRecycle.setLayoutManager(new GridLayoutManager(ResultRestaurant.this, 2));
        RestaurantAdapter restaurantAdapter = new RestaurantAdapter(this);
        allRestaurantRecycle.setAdapter(restaurantAdapter);

    }


}


class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {
    public static ArrayList<Result> RESTAURANT_LIST;
    public static ArrayList<PlaceWithLatLonAndAddress> ORIGINAL;
    public static Map<Pair<PlaceWithLatLonAndAddress, Result>, Route> ROUTE_MATRIX = new HashMap<>();
    private Context context;
    private int requestCount = 0;

    public RestaurantAdapter(Context context) {

        this.context = context;
        if (ORIGINAL.size() > 0) {
            for (PlaceWithLatLonAndAddress it : ORIGINAL) {
                for (Result result : RESTAURANT_LIST) {
                    RequestSingleton.getInstance(context).addToRequestQueue(getRouteRequest(it, result));
                }

            }
        }
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

        StringBuilder builder = new StringBuilder().append("Time needed by public transportation:");
        if (ORIGINAL.size() > 0) {
            for (PlaceWithLatLonAndAddress it : ORIGINAL) {
//                RequestSingleton.getInstance(context).addToRequestQueue(getRouteRequest(it,item));
                Route route = ROUTE_MATRIX.get(Pair.create(it, item));
                if (route != null) {
                    builder.append(" ").append(route.getLegs().get(0).getDuration().getText()).append(";");
                } else {
                    builder.append(" ").append("cannot go to there by public transportation;");
                }
            }
            holder.duration.setText(builder.deleteCharAt(builder.length() - 1));
        }



    }


    @Override
    public int getItemCount() {
        return RESTAURANT_LIST.size();
    }

    private String getRouteUrl(PlaceWithLatLonAndAddress origin, Result dest) {
        return "https://maps.googleapis.com/maps/api/directions/json?" + "origin=" + origin.lat + "," + origin.lon +
                "&destination=" + dest.getGeometry().getLocation().getLat() + "," + dest.getGeometry().getLocation().getLng() +
                "&mode=" + "transit" +
                "&key=" + context.getString(R.string.google_maps_key);
    }

    private JsonObjectRequest getRouteRequest(final PlaceWithLatLonAndAddress origin, final Result dest) {
        return new JsonObjectRequest(Request.Method.GET, getRouteUrl(origin, dest), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                RouteReceived routeReceived = new Gson().fromJson(new JsonParser().parse(response.toString()), RouteReceived.class);
                List<Route> routes = routeReceived.getRoutes();

                if (routes.size() > 0) {
                    ROUTE_MATRIX.put(new Pair<PlaceWithLatLonAndAddress, Result>(origin, dest), routes.get(0));
                } else {
                    ROUTE_MATRIX.put(new Pair<PlaceWithLatLonAndAddress, Result>(origin, dest), null);
                }

                int idx = RESTAURANT_LIST.indexOf(dest);
                notifyItemChanged(idx);
                requestCount++;
                if (requestCount == ORIGINAL.size() * RESTAURANT_LIST.size()) {

                    Collections.sort(RestaurantAdapter.RESTAURANT_LIST, new Comparator<Result>() {
                        @Override
                        public int compare(Result o1, Result o2) {
                            int sum1 = 0, sum2 = 0, notGo1 = 0, notGo2 = 0;
                            for (PlaceWithLatLonAndAddress it : RestaurantAdapter.ORIGINAL) {
                                Route route1 = RestaurantAdapter.ROUTE_MATRIX.get(Pair.create(it, o1));
                                Route route2 = RestaurantAdapter.ROUTE_MATRIX.get(Pair.create(it, o2));
                                if (route1 == null) {
                                    notGo1++;
                                } else {
                                    sum1 += route1.getLegs().get(0).getDuration().getValue();
                                }
                                if (route2 == null) {
                                    notGo2++;
                                } else {
                                    sum2 += route2.getLegs().get(0).getDuration().getValue();
                                }

                            }
                            if (notGo1 != notGo2) {
                                return notGo1 - notGo2;
                            } else {
                                return sum1 - sum2;
                            }
                        }
                    });
                    notifyItemRangeChanged(0, requestCount);
                }
                Log.i(TAG, "onRouteResponse: " + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: ", error);
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        ImageButton clearItem;
        RatingBar ratingBar;
        TextView duration;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.restaurant_name);
            clearItem = itemView.findViewById(R.id.clear_item);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            duration = itemView.findViewById(R.id.duration);
        }
    }
}
