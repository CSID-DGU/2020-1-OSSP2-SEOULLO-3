package com.seoullo.seoullotour.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class Bookmark implements Parcelable, Serializable {

    private String user_id;
    private String photo_id;
    private String image_name;
    private String location;
    private String caption;
    private int likeCount;
    private ArrayList<Double> latlng;

    public Bookmark() {
    }

    protected Bookmark(Parcel in) {
        user_id = in.readString();
        photo_id = in.readString();
        image_name = in.readString();
        location = in.readString();
        caption = in.readString();
        likeCount = in.readInt();
    }

    public static final Creator<Bookmark> CREATOR = new Creator<Bookmark>() {
        @Override
        public Bookmark createFromParcel(Parcel in) {
            return new Bookmark(in);
        }

        @Override
        public Bookmark[] newArray(int size) {
            return new Bookmark[size];
        }
    };

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPhoto_id() { return photo_id; }

    public void setPhoto_id(String photo_id) { this.photo_id = photo_id; }

    public String getImage_name() { return image_name; }

    public void setImage_name(String image_name) { this.image_name = image_name; }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Integer getLikeCount() { return likeCount; }

    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public String getLocation() { return location; }

    public void setLocation(String location) { this.location = location; }

    public void setLatlng(ArrayList<Double> latlng) {
        this.latlng = (ArrayList<Double>) latlng;
    }

    public ArrayList<Double> getLatlng() {
        return this.latlng;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(image_name);
        dest.writeString(caption);
        dest.writeString(photo_id);
        dest.writeString(user_id);
        dest.writeString(location);
    }
}
