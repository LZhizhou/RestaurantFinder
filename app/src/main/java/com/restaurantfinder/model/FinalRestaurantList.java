package com.restaurantfinder.model;

public class FinalRestaurantList {
    private static final FinalRestaurantList ourInstance = new FinalRestaurantList();

    private FinalRestaurantList() {
    }

    public static FinalRestaurantList getInstance() {
        return ourInstance;
    }
}
