package com.restaurantfinder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.restaurantfinder.model.Step;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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


    public void randomSelect(View view) {

        int ranInt = new Random().nextInt(RestaurantAdapter.RESTAURANT_LIST.size());
        Result item = RestaurantAdapter.RESTAURANT_LIST.get(ranInt);
        Uri gmmUri = Uri.parse("geo:" + item.getGeometry().getLocation().getLat() + "," + item.getGeometry().getLocation().getLng() +
                "?q=" + item.getName() + "," + item.getVicinity());

        Intent intent = new Intent(Intent.ACTION_VIEW, gmmUri);
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);

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
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmUri = Uri.parse("geo:" + item.getGeometry().getLocation().getLat() + "," + item.getGeometry().getLocation().getLng() +
                        "?q=" + item.getName() + "," + item.getVicinity());

                Intent intent = new Intent(Intent.ACTION_VIEW, gmmUri);
                intent.setPackage("com.google.android.apps.maps");
                context.startActivity(intent);

            }
        });

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
                    int transfer = 0;
                    builder.append(" ").append(route.getLegs().get(0).getDuration().getText()).append(",");
                    for (Step step : route.getLegs().get(0).getSteps()) {
                        if ("TRANSIT".equals(step.getTravelMode())) {
                            transfer += 1;

                        }
                    }
                    builder.append(" ").append(transfer).append(" buses;");
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

                    transferSort();
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

    private void transferSort() {
        Collections.sort(RestaurantAdapter.RESTAURANT_LIST, new Comparator<Result>() {
            @Override
            public int compare(Result o1, Result o2) {
                int transfer1 = 0, transfer2 = 0, notGo1 = 0, notGo2 = 0, sum1 = 0, sum2 = 0;
                for (PlaceWithLatLonAndAddress it : RestaurantAdapter.ORIGINAL) {
                    Route route1 = RestaurantAdapter.ROUTE_MATRIX.get(Pair.create(it, o1));
                    Route route2 = RestaurantAdapter.ROUTE_MATRIX.get(Pair.create(it, o2));
                    if (route1 == null) {
                        notGo1++;
                    } else {
                        sum1 += route1.getLegs().get(0).getDuration().getValue();
                        for (Step step : route1.getLegs().get(0).getSteps()) {
                            if ("TRANSIT".equals(step.getTravelMode())) {
                                transfer1 += 1;
                            }
                        }
                    }
                    if (route2 == null) {
                        notGo2++;
                    } else {
                        sum2 += route2.getLegs().get(0).getDuration().getValue();
                        for (Step step : route2.getLegs().get(0).getSteps()) {
                            if ("TRANSIT".equals(step.getTravelMode())) {
                                transfer2 += 1;
                            }
                        }
                    }

                }
                if (notGo1 != notGo2) {
                    return notGo1 - notGo2;
                } else if (transfer1 != transfer2) {
                    return transfer1 - transfer2;
                } else {
                    return sum1 - sum2;
                }
            }
        });
    }

    private void timeSort() {
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
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView nameText;
        ImageButton clearItem;
        RatingBar ratingBar;
        TextView duration;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            nameText = itemView.findViewById(R.id.restaurant_name);
            clearItem = itemView.findViewById(R.id.clear_item);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            duration = itemView.findViewById(R.id.duration);
        }
    }

}
