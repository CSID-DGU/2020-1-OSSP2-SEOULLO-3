package com.seoullo.seoullotour.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Photo implements Parcelable, Serializable {

    private String caption;
    private String date_created;
    private String image_path;
    private String photo_id;
    private String user_id;
    private String tags;
    private String image_name;
    private List<Like> likes;
    private int likeCount;
    private List<Comment> comments;
    private String location;    //added 0516 00:16
    private ArrayList<Place> places;

    public Photo() {
    }

    public Photo(String image_name, String caption, String date_created, String image_path, String photo_id, String user_id, String tags,
                 List<Like> likes, int likeCount, List<Comment> comments, String location, ArrayList<Place> places) {
        this.image_name = image_name;
        this.caption = caption;
        this.date_created = date_created;
        this.image_path = image_path;
        this.photo_id = photo_id;
        this.user_id = user_id;
        this.tags = tags;
        this.likes = likes;
        this.likeCount = likeCount;
        this.comments = comments;
        this.location = location;
        this.places = (ArrayList<Place>)places.clone();
    }

    protected Photo(Parcel in) {
        image_name = in.readString();
        caption = in.readString();
        date_created = in.readString();
        image_path = in.readString();
        photo_id = in.readString();
        user_id = in.readString();
        tags = in.readString();
        location = in.readString();
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getImage_path() {
        return image_path;
    }
    public String getImage_name() {
        return image_name;
    }
    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }
    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public String getPhoto_id() {
        return photo_id;
    }

    public void setPhoto_id(String photo_id) {
        this.photo_id = photo_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public int getLikeCount() { return this.likeCount; }

    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int addLikeCount() { return this.likeCount + 1; }

    public int subtractLikeCount() { return this.likeCount - 1; }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    //added 0516 00:00
    public void setLocation(String location) { this.location = location; }

    public String getLocation() { return location; }

    public void setPLaces(ArrayList<Place> places) { this.places = (ArrayList<Place>) places.clone(); }

    public ArrayList<Place> getPlaces() { return places; }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(image_name);
        dest.writeString(caption);
        dest.writeString(date_created);
        dest.writeString(image_path);
        dest.writeString(photo_id);
        dest.writeString(user_id);
        dest.writeString(tags);
        dest.writeString(location);
    }
}
