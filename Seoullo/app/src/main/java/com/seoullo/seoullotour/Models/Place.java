package com.seoullo.seoullotour.Models;

import java.io.Serializable;

public class Place implements Serializable {
    //TODO: 넣기 -> vicinity
    private double latitude;
    private double longitude;
    private String name;
    private String vicinity;
    private String photoReference;

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
}
