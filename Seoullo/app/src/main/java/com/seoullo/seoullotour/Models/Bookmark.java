package com.seoullo.seoullotour.Models;

public class Bookmark {

    private String user_id;
    private String photo_id;

    public Bookmark() {
    }

    public Bookmark(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPhoto_id() { return photo_id; }

    public String setPhoto_id(String photo_id) { this.photo_id = photo_id};

    @Override
    public String toString() {
        return super.toString();
    }
}
