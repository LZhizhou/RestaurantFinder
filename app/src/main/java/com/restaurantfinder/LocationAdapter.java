package com.restaurantfinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {
    private Context context;
    private List<PlaceWIthLatLonAndAddress> locationList;
    private View.OnClickListener onClickListener;

    public LocationAdapter(Context context, List<PlaceWIthLatLonAndAddress> locationList, View.OnClickListener onClickListener) {
        this.context = context;
        this.locationList = locationList;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(
                context).inflate(R.layout.one_location, parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        if (position < locationList.size()) {
            holder.locationText.setText(locationList.get(position).address);
            holder.locationText.setClickable(false);
            holder.deleteLocationButton.setVisibility(View.VISIBLE);
            holder.deleteLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeItem(position);
                }
            });
        } else {

            holder.locationText.setText("Click to add another location");
            holder.locationText.setOnClickListener(onClickListener);
            holder.deleteLocationButton.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return locationList.size() + 1;
    }

    public void removeItem(int position){
        locationList.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView locationText;
        MaterialButton deleteLocationButton;

        public ViewHolder(View view) {
            super(view);
            locationText = view.findViewById(R.id.location_text);
            deleteLocationButton = view.findViewById(R.id.delete_location);

        }
    }

}
