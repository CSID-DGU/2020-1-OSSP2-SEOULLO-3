package com.instagramclone.Models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ContentDTO implements Serializable {

    public String location;
    public String explain;
    public String imageUrl;
    public String imageName;
    public String uid;
    public String userId;
    public String timestamp;
    public int favoriteCount = 0;
    public int bookmarkCount = 0;
    public Map<String, Boolean> favorites = new HashMap<>();
    public Map<String, Boolean> bookmarks = new HashMap<>();
    public Map<String, Comment> comments;

    public ContentDTO(){ }
//    protected ContentDTO(Parcel in) {
//        explain = in.readString();
//        imageUrl = in.readString();
//        imageName = in.readString();
//        uid = in.readString();
//        userId = in.readString();
//        timestamp = in.readString();
//        favoriteCount = in.readInt();
//    }
//
//    public static final Creator<ContentDTO> CREATOR = new Creator<ContentDTO>() {
//        @Override
//        public ContentDTO createFromParcel(Parcel in) {
//            return new ContentDTO(in);
//        }
//
//        @Override
//        public ContentDTO[] newArray(int size) {
//            return new ContentDTO[size];
//        }
//    };
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(explain);
//        dest.writeString(imageUrl);
//        dest.writeString(imageName);
//        dest.writeString(uid);
//        dest.writeString(userId);
//        dest.writeString(timestamp);
//        dest.writeInt(favoriteCount);
//    }

    public static class Comment {
        public String timestamp;
        public String uid;
        public String userId;
        public String comment;
    }

    @Override
    public String toString() {
        return "uid = " + uid + " , userid = " + userId;
    }
}
