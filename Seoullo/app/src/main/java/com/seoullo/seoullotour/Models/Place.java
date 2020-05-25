package com.seoullo.seoullotour.Models;

import java.io.Serializable;
import java.util.ArrayList;

public class Place implements Serializable {

    private double latitude;
    private double longitude;
    private String name;
    private String vicinity;
    private String photoReference;
    //added 0526
    private ArrayList<String> type;

    public Place() { }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() { return this.longitude; }

    public String getName() {
        return this.name;
    }

    public String getVicinity() { return this.vicinity; }

    public String getPhotoReference() { return this.photoReference; }

    public ArrayList<String> getType() { return this.type; }

    public void setLatitude(double lat) {
        this.latitude = lat;
    }

    public void setLongitude(double lon) {
        this.longitude = lon;
    }

    public void setName(String n) {
        this.name = n;
    }

    public void setVicinity(String v) { this.vicinity = v; }

    public void setPhotoReference(String p) { this.photoReference = p; }

    public void setType(ArrayList<String> t) { this.type = (ArrayList<String>)t.clone(); }
}
