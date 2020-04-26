package com.seoullo.seoullotour.Models;

//TODO: 구글에서 주는 json의 형태와 다름 다시 리팩토링해줘야함 04/06 -> 응 필요없어~ 04/06 모닝
public class Place {
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
