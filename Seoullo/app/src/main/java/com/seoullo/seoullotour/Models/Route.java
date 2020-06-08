package com.seoullo.seoullotour.Models;

import java.io.Serializable;
import java.util.ArrayList;

public class Route implements Cloneable, Serializable {

    private String departureTime;
    private int duration;
    private int distance;

    private ArrayList<String> pathArray;      //only lng, lat : [126.9857256,37.5504693] -> 파싱작업해야함
    private ArrayList<String> guideArray;       //[0] : "동국대학교앞에서 '장충단로' 방면으로 우회전"

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }
    public void setDistance(int distance) {
        this.distance = distance;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
    public void setPathArray(ArrayList<String> path) {
        this.pathArray = (ArrayList<String>) path.clone();
    }
    public void setGuideArray(ArrayList<String> guide) {
        this.guideArray = (ArrayList<String>) guide.clone();
    }

    public String getDepartureTime() {
        return this.departureTime;
    }
    public int getDuration() {
        return this.duration;
    }
    public int getDistance() {
        return this.distance;
    }
    public ArrayList<String> getPathArray() {
        return this.pathArray;
    }
    public ArrayList<String> getGuideArray() {
        return this.guideArray;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
