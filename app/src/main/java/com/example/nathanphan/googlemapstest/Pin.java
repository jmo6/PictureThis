package com.example.nathanphan.googlemapstest;

/**
 * Created by Nathan Phan on 5/28/2017.
 */

public class Pin {
    private String id;
    private double lat;
    private double lng;
    private String title;

    public Pin(){

    }

    public Pin(String id, double lat, double lng, String title) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getTitle() {
        return title;
    }
}
